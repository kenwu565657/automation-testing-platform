# Platform HTTP contract

Checked-in copy of the Stage 1 admin API. Live springdoc stays on admin (`http://localhost:8081/swagger-ui.html`, `/v3/api-docs`).

> **Stale after the envelope migration (2026-10):** every bodied 2xx response is now wrapped in
> `RestfulApiResponse { code, message, data, traceId }`; the payload field names are unchanged and
> 204s stay bodyless. The checked-in spec below predates the wrapper — regenerate after booting admin:
>
> Use `/v3/api-docs.yaml` for YAML; `/v3/api-docs` returns JSON. The local workflow described
> in [the API workflow guide](../../docs/standards/API_WORKFLOW.md) saves validated JSON separately
> and does not overwrite this existing specification or Postman collection.
> The frontend client already unwraps `data` (`adminClient.ts`), so it needs no regeneration.

| Consumer | Path |
|---|---|
| OpenAPI 3 | `platform-v1.yaml` |
| Postman | `platform-v1.postman_collection.json` |
| Frontend | `platform-web/src/api/adminClient.ts` |

Gateway public URL: `http://localhost:8080/api/admin` + `/api/v1/...`.

`POST /api/v1/user`, `POST /api/v1/auth/login`, and `POST /api/v1/auth/refresh` are open. Every other call sends `Authorization: Bearer <accessToken>`. AccessGuard still authorizes the JWT `sub` (user id) against IAM. Organizations use `organization:Get` / `Update` / `Delete` / `ManageMembers` / `CreateProject`. Creating a project requires an organization and `organization:CreateProject`.

`GET /api/v1/test-case/{id}` returns full latest-definition steps (params, assertions, Gherkin overlay). List endpoints stay catalog-only.

`POST /api/v1/test-case/generate` returns `{ persisted: false, draft }` and does not write the catalog. Uses the current user's saved key from `PUT /api/v1/ai-settings`, or `PLATFORM_AI_*` if they have none.

`POST /api/v1/test-case/import` returns `{ persisted: false, pageObjects, cases, elementBindings }` from pasted Selenium/Appium Java. Persist page objects first, remap draft ids, then create cases.

`POST /api/v1/page-object/{id}/heal` promotes a working locator.

Farm: `POST /api/v1/device-slot` registers inventory. Triggering a run leases a slot that matches the target's `deviceProfileId`. Engine uses the leased URL. `POST /api/v1/device-slot/{id}/release` clears a stuck lease.

