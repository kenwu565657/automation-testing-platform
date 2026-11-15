import assert from "node:assert/strict";
import { readFile } from "node:fs/promises";
import { test } from "node:test";
import { badge, formatMetric, jvmModules, markdown, nodeModules, readNodeMetric, validMetric } from "./coverage.mjs";

test("coverage percentages use covered and total, not a rounded input percentage", () => {
  assert.equal(formatMetric(readNodeMetric({ total: 3, covered: 1 }, "lines")), "33.3%");
  assert.equal(readNodeMetric({ total: 0, covered: 0 }, "branches"), null);
  assert.throws(() => readNodeMetric({ total: 3, covered: 4 }, "lines"), /Invalid/);
  assert.throws(() => validMetric(101, "lines"), /Invalid/);
});

test("badges and summaries distinguish zero from unavailable", () => {
  assert.deepEqual(badge(null, "lines"), {
    schemaVersion: 1, label: "lines", message: "N/A", color: "lightgrey",
  });
  assert.equal(badge(0, "lines").message, "0.0%");
  assert.equal(badge(80, "lines").color, "brightgreen");
  const summary = markdown([{ id: "example", lines: 0, branches: null, note: "No branches" }]);
  assert.match(summary, /\| example \| 0\.0% \| N\/A \| No branches \|/);
});

test("README has a badge for each metric of each module", async () => {
  const readme = await readFile(new URL("../README.md", import.meta.url), "utf8");
  for (const id of [...jvmModules, ...nodeModules.map((module) => module.id)]) {
    for (const metric of ["lines", "branches"]) {
      assert.ok(readme.includes(`/badges/${id}-${metric}.json`), `Missing ${id} ${metric} badge`);
    }
  }
});
