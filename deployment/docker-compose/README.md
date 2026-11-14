# Docker Local Development

## Commands

```bash
cd deployment/docker-compose

# ── Daily dev: infra + Nginx. Run the gateway on :18080 ──
docker compose up -d

# ── Add Kibana, Kafka UI, RedisInsight, ZooNavigator, ElasticJob UI, S3Manager, Prometheus, Grafana, Tempo, OTel collector ──
docker compose --profile tools up -d

# ── Full stack in Docker ──
docker compose --profile services up -d

# ── Everything ──
docker compose --profile services --profile tools up -d

# ── Stop ──
docker compose --profile services --profile tools down

# ── Nuclear ──
docker compose down -v
```

## Ports

| Service         | Port   |
|-----------------|--------|
| Nginx           | 8080   |
| Gateway         | 18080  |
| Admin (Spring)  | 8081   |
| Mobile engine   | 8082   |
| API engine      | 8085   |
| Report (Ktor)   | 8083   |
| Coordinator     | 8084   |
| PostgreSQL      | 5432   |
| Kafka           | 9093   |
| Elasticsearch   | 9200   |
| Redis           | 6379   |
| ZooKeeper       | 2181   |
| Floci (S3 API)  | 4566   |
| Kibana          | 5601   |
| Kafka UI        | 9090   |
| RedisInsight    | 5540   |
| ZooNavigator    | 9000   |
| ElasticJob UI   | 8088   |
| S3Manager       | 4567   |
| Prometheus      | 9091   |
| Alertmanager    | 9094   |
| Grafana         | 3001   |
| Tempo           | 3200   |
| OTel collector  | 4317 / 4318 |

Nginx starts with the infra stack and with `--profile services`. The frontend calls `:8080`. Nginx forwards `/api`, `/ws`, and `/actuator` to the gateway on `:18080`. Use that same port when the gateway runs in the IDE. Do not run the IDE process and the Compose gateway together. ZooKeeper must be up: admin always starts ElasticJob.

Create a user (`POST /api/v1/user` with `password`), then `POST /api/v1/auth/login` for a Bearer access token.

| Check | URL |
|---|---|
| Admin OpenAPI / Swagger | http://localhost:8081/swagger-ui.html |
| Postman collection | `contracts/platform-api/platform-v1.postman_collection.json` |
| Public gateway health | http://localhost:8080/actuator/health |
| Gateway direct | http://localhost:18080/actuator/health |
| Mobile engine metrics | http://localhost:8082/metrics |
| API engine metrics | http://localhost:8085/metrics |
| Report metrics | http://localhost:8083/metrics |

## Local S3 (Floci)

Floci starts with the rest of infra (`docker compose up -d`). Apps should use the AWS SDK with:

```bash
export AWS_ENDPOINT_URL=http://localhost:4566
export AWS_DEFAULT_REGION=us-east-1
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test
# path-style buckets
```

From another Compose service the host is `http://floci:4566`.

Create the artifacts bucket once:

```bash
aws --endpoint-url http://localhost:4566 s3 mb s3://valdifly-artifacts
aws --endpoint-url http://localhost:4566 s3 ls
```

Production uses real S3 and the same bucket naming. Do not add MinIO or LocalStack next to Floci.

## Metrics (`--profile tools`)

| Tool | URL | Login |
|---|---|---|
| Prometheus | http://localhost:9091 | — |
| Alertmanager | http://localhost:9094 | — |
| Grafana | http://localhost:3001 | `admin` / `admin` (anonymous Viewer also on) |
| Tempo | http://localhost:3200 | — |
| OTel collector | `localhost:4318` (OTLP HTTP), `:4317` (OTLP gRPC) | — |

Prometheus scrapes gateway/admin `/actuator/prometheus` and engine/report `/metrics` on the Compose network and on `host.docker.internal` for IDE processes. `ServiceDown` fires through Alertmanager after 1m. Grafana ships a **Spring services** dashboard (Valdifly folder) and a **Tempo** datasource for traces.

IDE processes export OTLP to `http://localhost:4318`. Compose services use `http://otel-collector:4318` (needs `--profile tools`). Kafka `correlationId` is the OTel trace id when the producer has an active span.

Admin JWT secret is `JWT_SECRET` in `.env` (HS256, ≥32 characters). The committed value is local-only. Create a user with a password, then `POST /api/v1/auth/login`; every other admin call uses `Authorization: Bearer`.

Users type their own API key in the web app (**AI settings**) or `PUT /api/v1/ai-settings`. Keys are encrypted with `PLATFORM_AI_SETTINGS_SECRET` (defaults to `JWT_SECRET`). `PLATFORM_AI_BASE_URL` / `API_KEY` / `MODEL` remain an optional company-wide fallback. Generate still returns a draft and does not persist.

GitHub Actions (`.github/workflows/ci.yml`) and Azure Pipelines (`.azure-pipelines/azure-pipelines.yml`) both run `./gradlew test` from the repo root.
```