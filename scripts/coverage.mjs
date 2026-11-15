import { mkdir, readFile, writeFile, appendFile } from "node:fs/promises";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
export const jvmModules = [
  "admin-service", "engine-coordinator-service", "gateway-service", "report-service",
  "engine-core", "engine-api", "engine-mobile",
  "shared-domain", "shared-infrastructure", "shared-spring", "shared-utils", "shared-vertx",
];
export const nodeModules = [
  { id: "platform-web", path: "platform-web" },
  { id: "engine-playwright", path: "engines/playwright" },
  { id: "engine-k6", path: "engines/k6" },
  { id: "shared-typescript-engine", path: "shared/typescript/engine" },
];

export function validMetric(value, label) {
  if (value === null) return null;
  if (typeof value !== "number" || !Number.isFinite(value) || value < 0 || value > 100) {
    throw new Error(`Invalid coverage value for ${label}: ${value}`);
  }
  return value;
}

export function readNodeMetric(metric, label) {
  if (!metric || !Number.isInteger(metric.total) || !Number.isInteger(metric.covered)
      || metric.total < 0 || metric.covered < 0 || metric.covered > metric.total) {
    throw new Error(`Invalid coverage counts for ${label}`);
  }
  return metric.total === 0 ? null : validMetric(metric.covered * 100 / metric.total, label);
}

export function formatMetric(value) {
  return value === null ? "N/A" : `${value.toFixed(1)}%`;
}

export function badge(value, label) {
  return {
    schemaVersion: 1,
    label,
    message: formatMetric(value),
    color: value === null ? "lightgrey" : value >= 80 ? "brightgreen" : value >= 60 ? "yellow" : "orange",
  };
}

async function json(path) {
  return JSON.parse(await readFile(join(root, path), "utf8"));
}

export async function collect() {
  const jvm = await json("build/reports/coverage/jvm.json");
  if (!Array.isArray(jvm)) throw new Error("Invalid JVM coverage summary");
  const ids = jvm.map((entry) => entry.id);
  if (new Set(ids).size !== ids.length
      || ids.length !== jvmModules.length
      || jvmModules.some((id) => !ids.includes(id))) {
    throw new Error("JVM module list differs from coverage.mjs; update the coverage manifest and README");
  }
  const rows = jvmModules.map((id) => {
    const entry = jvm.find((item) => item.id === id);
    return {
      id,
      lines: validMetric(entry.lines, `${id} lines`),
      branches: validMetric(entry.branches, `${id} branches`),
      note: entry.reason ?? entry.report ?? "",
    };
  });
  for (const module of nodeModules) {
    const pkg = await json(`${module.path}/package.json`);
    if (!pkg.scripts?.["test:coverage"]) {
      rows.push({ id: module.id, lines: null, branches: null,
        note: pkg.scripts?.test ? "Coverage not configured" : "No test script" });
      continue;
    }
    const report = await json(`${module.path}/coverage/coverage-summary.json`);
    rows.push({
      id: module.id,
      lines: readNodeMetric(report.total?.lines, `${module.id} lines`),
      branches: readNodeMetric(report.total?.branches, `${module.id} branches`),
      note: `${module.path}/coverage/index.html`,
    });
  }
  return rows;
}

export function markdown(rows) {
  return [
    "## Module test coverage",
    "",
    "| Module | Lines | Branches | Report / status |",
    "| --- | ---: | ---: | --- |",
    ...rows.map((row) => `| ${row.id} | ${formatMetric(row.lines)} | ${formatMetric(row.branches)} | ${row.note} |`),
    "",
    "N/A means no measurable data. Coverage is informational, not a threshold.",
    "",
  ].join("\n");
}

async function main() {
  const rows = await collect();
  const summary = markdown(rows);
  console.log(summary);
  if (process.env.GITHUB_STEP_SUMMARY) await appendFile(process.env.GITHUB_STEP_SUMMARY, summary);
  if (process.argv[2] === "--badges") {
    const directory = join(root, "build/coverage-pages/badges");
    await mkdir(directory, { recursive: true });
    for (const row of rows) {
      for (const metric of ["lines", "branches"]) {
        await writeFile(join(directory, `${row.id}-${metric}.json`),
          `${JSON.stringify(badge(row[metric], metric))}\n`);
      }
    }
    await writeFile(join(root, "build/coverage-pages/index.html"),
      "<!doctype html><html lang=\"en\"><title>Valdifly coverage</title><p>Coverage badges for the latest successful default-branch build. Detailed reports are available in the GitHub Actions artifacts.</p></html>\n");
  } else if (process.argv.length > 2) {
    throw new Error("Usage: node scripts/coverage.mjs [--badges]");
  }
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().catch((error) => {
    console.error(error.message);
    process.exitCode = 1;
  });
}
