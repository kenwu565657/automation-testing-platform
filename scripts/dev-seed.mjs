const SEED = {
  email: "dev-seed@example.com",
  password: process.env.DEV_SEED_PASSWORD ?? "dev-seed-pass",
  displayName: "Dev Seed",
  organizationName: "Dev Seed",
  projectName: "Playground",
  environmentName: "Local",
};

export function parseArgs(argv) {
  const args = argv.slice(2);
  if (args.includes("--help") || args.includes("-h")) return { help: true };
  const deleteData = args.includes("--delete") || args.includes("delete");
  const baseFlag = args.find((arg) => arg.startsWith("--base-url="));
  const baseUrl = (baseFlag ? baseFlag.slice("--base-url=".length) : process.env.DEV_SEED_BASE_URL) || "http://localhost:8081";
  return { deleteData, baseUrl: baseUrl.replace(/\/$/, "") };
}

export function createApi(baseUrl, fetchImpl = fetch) {
  return {
    async request(method, path, { token, body } = {}) {
      const response = await fetchImpl(`${baseUrl}${path}`, {
        method,
        headers: {
          accept: "application/json",
          ...(body === undefined ? {} : { "content-type": "application/json" }),
          ...(token ? { authorization: `Bearer ${token}` } : {}),
        },
        body: body === undefined ? undefined : JSON.stringify(body),
      });
      const text = await response.text();
      const json = text ? JSON.parse(text) : null;
      if (response.status === 204) return null;
      if (response.status < 200 || response.status >= 300) {
        const error = new Error(`${method} ${path} failed: ${response.status} ${json?.message ?? response.statusText}`);
        error.status = response.status;
        error.body = json;
        throw error;
      }
      if (json && json.code !== 0) {
        throw new Error(`${method} ${path} failed: ${json.message}`);
      }
      return json?.data ?? null;
    },
  };
}

async function ensureUser(api) {
  try {
    const user = await api.request("POST", "/api/v1/user", {
      body: { email: SEED.email, displayName: SEED.displayName, password: SEED.password },
    });
    return { id: user.id, created: true };
  } catch (error) {
    if (error.status === 400 && String(error.body?.message ?? "").includes("already exists")) {
      return { created: false };
    }
    throw error;
  }
}

async function login(api) {
  const token = await api.request("POST", "/api/v1/auth/login", {
    body: { email: SEED.email, password: SEED.password },
  });
  if (!token?.accessToken) throw new Error("Login did not return an access token");
  return token.accessToken;
}

async function ensureNamed(items, name, create) {
  const existing = items.find((item) => item.name === name);
  if (existing) return { item: existing, created: false };
  return { item: await create(), created: true };
}

export async function seedCatalog(api) {
  const user = await ensureUser(api);
  const token = await login(api);
  const organizations = await api.request("GET", "/api/v1/organization", { token });
  const organization = await ensureNamed(organizations, SEED.organizationName, () =>
    api.request("POST", "/api/v1/organization", {
      token,
      body: { name: SEED.organizationName, description: "Created by scripts/dev-seed.mjs" },
    }));
  const projects = await api.request("GET", `/api/v1/project?organizationId=${encodeURIComponent(organization.item.id)}`, { token });
  const project = await ensureNamed(projects, SEED.projectName, () =>
    api.request("POST", "/api/v1/project", {
      token,
      body: { name: SEED.projectName, description: "Created by scripts/dev-seed.mjs", organizationId: organization.item.id },
    }));
  const environments = await api.request("GET", `/api/v1/environment?projectId=${encodeURIComponent(project.item.id)}`, { token });
  const environment = await ensureNamed(environments, SEED.environmentName, () =>
    api.request("POST", "/api/v1/environment", {
      token,
      body: { projectId: project.item.id, name: SEED.environmentName, variables: { baseUrl: "http://localhost:3000" } },
    }));
  return {
    email: SEED.email,
    password: SEED.password,
    userCreated: user.created,
    organizationId: organization.item.id,
    organizationCreated: organization.created,
    projectId: project.item.id,
    projectCreated: project.created,
    environmentId: environment.item.id,
    environmentCreated: environment.created,
  };
}

export async function deleteCatalog(api) {
  const token = await login(api);
  const organizations = (await api.request("GET", "/api/v1/organization", { token }))
    .filter((organization) => organization.name === SEED.organizationName);
  const removed = [];
  for (const organization of organizations) {
    const projects = await api.request("GET", `/api/v1/project?organizationId=${encodeURIComponent(organization.id)}`, { token });
    const seedProjects = projects.filter((project) => project.name === SEED.projectName);
    const kept = projects.filter((project) => project.name !== SEED.projectName);
    for (const project of seedProjects) {
      await api.request("DELETE", `/api/v1/project/${project.id}`, { token });
    }
    if (kept.length > 0) {
      throw new Error(`Organization ${organization.id} still has projects not created by this script: ${kept.map((project) => project.name).join(", ")}`);
    }
    await api.request("DELETE", `/api/v1/organization/${organization.id}`, { token });
    removed.push({ organizationId: organization.id, projectIds: seedProjects.map((project) => project.id) });
  }
  return { email: SEED.email, removed };
}

function printHelp() {
  console.log(`Usage: node scripts/dev-seed.mjs [--delete] [--base-url=http://localhost:8081]

Creates a local user, organization, project, and environment through the admin API.
--delete removes that organization after deleting its Playground project.
The user account stays, because admin has no delete-user API.
DEV_SEED_BASE_URL and DEV_SEED_PASSWORD override the defaults.`);
}

async function main() {
  const options = parseArgs(process.argv);
  if (options.help) {
    printHelp();
    return;
  }
  const api = createApi(options.baseUrl);
  if (options.deleteData) {
    const result = await deleteCatalog(api);
    if (result.removed.length === 0) console.log(`No ${SEED.organizationName} organization to delete.`);
    for (const item of result.removed) {
      console.log(`Deleted organization ${item.organizationId} and projects ${item.projectIds.join(", ") || "(none)"}`);
    }
    console.log(`Left user ${result.email} in place.`);
    return;
  }
  const created = await seedCatalog(api);
  console.log(`User ${created.email} ${created.userCreated ? "created" : "already exists"}`);
  console.log(`Password: ${created.password}`);
  console.log(`Organization ${created.organizationId} ${created.organizationCreated ? "created" : "already exists"}`);
  console.log(`Project ${created.projectId} ${created.projectCreated ? "created" : "already exists"}`);
  console.log(`Environment ${created.environmentId} ${created.environmentCreated ? "created" : "already exists"}`);
  console.log(`Sign in at the web app with ${created.email}.`);
}

if (process.argv[1] && process.argv[1].endsWith("dev-seed.mjs")) {
  main().catch((error) => {
    console.error(error.message);
    process.exitCode = 1;
  });
}
