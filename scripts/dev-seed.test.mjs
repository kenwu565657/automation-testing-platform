import assert from "node:assert/strict";
import { test } from "node:test";
import { createApi, deleteCatalog, parseArgs, seedCatalog } from "./dev-seed.mjs";

function fakeApi(handlers) {
  const calls = [];
  return {
    calls,
    async request(method, path, options = {}) {
      calls.push({ method, path, options });
      const handler = handlers.find((item) => item.method === method && item.path === path);
      if (!handler) throw new Error(`unexpected ${method} ${path}`);
      return handler.run(options);
    },
  };
}

test("parseArgs selects delete and base url", () => {
  assert.deepEqual(parseArgs(["node", "dev-seed.mjs"]), { deleteData: false, baseUrl: "http://localhost:8081" });
  assert.equal(parseArgs(["node", "dev-seed.mjs", "--delete"]).deleteData, true);
  assert.equal(parseArgs(["node", "dev-seed.mjs", "delete"]).deleteData, true);
  assert.equal(parseArgs(["node", "dev-seed.mjs", "--base-url=http://localhost:8080/api/admin/"]).baseUrl, "http://localhost:8080/api/admin");
});

test("seed creates the catalog and skips an existing user", async () => {
  const api = fakeApi([
    { method: "POST", path: "/api/v1/user", run() { const error = new Error("exists"); error.status = 400; error.body = { message: "User already exists: dev-seed@example.com" }; throw error; } },
    { method: "POST", path: "/api/v1/auth/login", run: () => ({ accessToken: "token" }) },
    { method: "GET", path: "/api/v1/organization", run: () => [] },
    { method: "POST", path: "/api/v1/organization", run: () => ({ id: "org-1", name: "Dev Seed" }) },
    { method: "GET", path: "/api/v1/project?organizationId=org-1", run: () => [] },
    { method: "POST", path: "/api/v1/project", run: () => ({ id: "project-1", name: "Playground" }) },
    { method: "GET", path: "/api/v1/environment?projectId=project-1", run: () => [] },
    { method: "POST", path: "/api/v1/environment", run: () => ({ id: "env-1", name: "Local" }) },
  ]);

  const created = await seedCatalog(api);

  assert.equal(created.userCreated, false);
  assert.equal(created.organizationCreated, true);
  assert.equal(created.projectId, "project-1");
  assert.equal(created.environmentId, "env-1");
  assert.equal(api.calls.find((call) => call.method === "POST" && call.path === "/api/v1/project").options.body.organizationId, "org-1");
});

test("delete removes the seed project before the organization", async () => {
  const api = fakeApi([
    { method: "POST", path: "/api/v1/auth/login", run: () => ({ accessToken: "token" }) },
    { method: "GET", path: "/api/v1/organization", run: () => [{ id: "org-1", name: "Dev Seed" }, { id: "org-2", name: "Other" }] },
    { method: "GET", path: "/api/v1/project?organizationId=org-1", run: () => [{ id: "project-1", name: "Playground" }] },
    { method: "DELETE", path: "/api/v1/project/project-1", run: () => null },
    { method: "DELETE", path: "/api/v1/organization/org-1", run: () => null },
  ]);

  const result = await deleteCatalog(api);

  assert.deepEqual(result.removed, [{ organizationId: "org-1", projectIds: ["project-1"] }]);
  const methods = api.calls.map((call) => `${call.method} ${call.path}`);
  assert.ok(methods.indexOf("DELETE /api/v1/project/project-1") < methods.indexOf("DELETE /api/v1/organization/org-1"));
});

test("delete refuses an organization that has other projects", async () => {
  const api = fakeApi([
    { method: "POST", path: "/api/v1/auth/login", run: () => ({ accessToken: "token" }) },
    { method: "GET", path: "/api/v1/organization", run: () => [{ id: "org-1", name: "Dev Seed" }] },
    { method: "GET", path: "/api/v1/project?organizationId=org-1", run: () => [{ id: "project-2", name: "Mine" }] },
  ]);

  await assert.rejects(deleteCatalog(api), /still has projects/);
});

test("createApi reads the response envelope", async () => {
  const api = createApi("http://localhost:8081", async () => ({
    status: 201,
    statusText: "Created",
    async text() { return JSON.stringify({ code: 0, message: "success", data: { id: "user-1" } }); },
  }));
  assert.deepEqual(await api.request("POST", "/api/v1/user", { body: { email: "a@b.co" } }), { id: "user-1" });
});
