# Valdifly

Author, run, and analyze automated tests.

## Test coverage

These badges show module-wide line and branch coverage from the latest successful
default-branch GitHub Actions run. `N/A` means coverage is unavailable, not zero.
Detailed reports are available as [Actions artifacts](https://github.com/kenwu565657/automation-testing-platform/actions).
For local coverage and GitHub Pages setup, see [docs/README.md](docs/README.md).

| Module | Lines | Branches |
|---|---|---|
| Admin | ![Admin lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/admin-service-lines.json) | ![Admin branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/admin-service-branches.json) |
| Coordinator | ![Coordinator lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-coordinator-service-lines.json) | ![Coordinator branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-coordinator-service-branches.json) |
| Gateway | ![Gateway lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/gateway-service-lines.json) | ![Gateway branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/gateway-service-branches.json) |
| Report | ![Report lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/report-service-lines.json) | ![Report branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/report-service-branches.json) |
| Engine core | ![Engine core lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-core-lines.json) | ![Engine core branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-core-branches.json) |
| API engine | ![API engine lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-api-lines.json) | ![API engine branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-api-branches.json) |
| Mobile engine | ![Mobile engine lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-mobile-lines.json) | ![Mobile engine branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-mobile-branches.json) |
| Shared domain | ![Shared domain lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-domain-lines.json) | ![Shared domain branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-domain-branches.json) |
| Shared infrastructure | ![Shared infrastructure lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-infrastructure-lines.json) | ![Shared infrastructure branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-infrastructure-branches.json) |
| Shared Spring | ![Shared Spring lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-spring-lines.json) | ![Shared Spring branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-spring-branches.json) |
| Shared utilities | ![Shared utilities lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-utils-lines.json) | ![Shared utilities branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-utils-branches.json) |
| Shared Vert.x | ![Shared Vert.x lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-vertx-lines.json) | ![Shared Vert.x branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-vertx-branches.json) |
| Web | ![Web lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/platform-web-lines.json) | ![Web branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/platform-web-branches.json) |
| Playwright engine | ![Playwright lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-playwright-lines.json) | ![Playwright branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-playwright-branches.json) |
| k6 engine | ![k6 lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-k6-lines.json) | ![k6 branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/engine-k6-branches.json) |
| Shared TypeScript engine | ![TypeScript engine lines](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-typescript-engine-lines.json) | ![TypeScript engine branches](https://img.shields.io/endpoint?url=https://kenwu565657.github.io/automation-testing-platform/badges/shared-typescript-engine-branches.json) |

Nginx on port 8080 is the only browser entry. It forwards `/api`, `/ws`, and `/actuator` to the gateway. The gateway routes by Compose service name. A run goes from admin through Kafka to the coordinator, then to one engine. The report service reads the result.

```mermaid
flowchart TB
  browser["Browser · platform-web"]
  nginx["Nginx :8080"]
  gw["platform-gateway :18080<br/>Spring Cloud Gateway"]

  subgraph core["Spring Boot"]
    admin["platform-admin :8081"]
    coord["platform-coordinator :8084"]
  end

  report["platform-report :8083<br/>Ktor"]
  agent["platform-agent<br/>FastAPI · LangGraph"]

  subgraph engines["Engines"]
    mobile["engines/mobile · Appium"]
    api["engines/api · REST Assured"]
    pw["engines/playwright"]
    k6["engines/k6"]
  end

  subgraph farm["device-farm"]
    host["host-agent · Quarkus"]
    broker["session-broker · Vert.x"]
    video["video-relay · Netty"]
    appium["Appium :4723"]
  end

  subgraph data["Data — host ports"]
    pg[("PostgreSQL :5432")]
    kafka[["Kafka :9093"]]
    redis[("Redis :6379")]
    es[("Elasticsearch :9200")]
    s3[("S3 / Floci :4566")]
    zk[("ZooKeeper :2181<br/>ElasticJob only")]
  end

  subgraph observe["Observability tools — host ports"]
    collector["OTel collector :4317 / :4318"]
    tempo["Tempo :3200"]
    prom["Prometheus :9091"]
    alert["Alertmanager :9094"]
    graf["Grafana :3001"]
  end

  browser --> nginx
  nginx -->|"/api · /ws · /actuator"| gw
  gw --> admin
  gw --> report
  gw --> agent
  gw --> broker
  gw --> video

  admin --> pg
  admin --> kafka
  admin --> redis
  admin --> s3
  admin --> zk
  kafka --> coord
  coord --> pg
  coord --> redis
  coord --> mobile
  coord --> api
  coord --> pw
  coord --> k6
  mobile --> kafka
  api --> kafka
  pw --> kafka
  k6 --> kafka
  mobile --> appium
  kafka --> report
  report --> es
  report --> redis
  report --> s3

  agent -->|"tools, user JWT"| admin
  host --> admin
  host -->|"TCP frames"| video
  host --> appium

  gw --> collector
  admin --> collector
  coord --> collector
  report --> collector
  mobile --> collector
  api --> collector
  collector --> tempo
  prom -->|scrape| gw
  prom -->|scrape| admin
  prom -->|scrape| report
  prom --> alert
  graf --> tempo
  graf --> prom
```

## Repository

`platform-agent`, `device-farm`, `deployment/kubernetes`, `deployment/terraform`, and `deployment/ansible` are added when those files exist.

```text
valdifly/
├── platform-web/
├── platform-gateway/
├── platform-admin/
├── platform-coordinator/
├── platform-report/
├── platform-agent/
├── engines/
│   ├── core/
│   ├── mobile/
│   ├── api/
│   ├── playwright/
│   └── k6/
├── device-farm/
│   ├── host-agent/
│   ├── session-broker/
│   └── video-relay/
├── contracts/
│   ├── engine/
│   └── platform-api/
├── shared/
│   ├── java/
│   │   ├── domain/
│   │   ├── infrastructure/
│   │   ├── spring/
│   │   └── utils/
│   └── typescript/
│       └── engine/
├── deployment/
│   ├── docker-compose/
│   ├── kubernetes/
│   ├── terraform/
│   └── ansible/
└── docs/
    ├── architecture/
    ├── standards/
    └── uml/
```

One Gradle build covers the JVM modules. One npm workspace covers `platform-web`, `engines/playwright`, `engines/k6`, and `shared/typescript/engine`. JDK 21.

## Read next

| Topic | Where |
|---|---|
| Scope, services, and stack | [docs/architecture/README.md](docs/architecture/README.md) |
| Local development and test workflow | [docs/README.md](docs/README.md) |
| Domain model | [shared/java/domain/README.md](shared/java/domain/README.md) |
| Coding standards | [docs/standards/CODING_STANDARDS.md](docs/standards/CODING_STANDARDS.md) |
| Aggregate rules | [docs/standards/DDD_STANDARDS.md](docs/standards/DDD_STANDARDS.md) |
| Engines | [engines/README.md](engines/README.md) |
| HTTP contract | [contracts/platform-api/README.md](contracts/platform-api/README.md) |
| Local API synchronization and Postman import | [docs/standards/API_WORKFLOW.md](docs/standards/API_WORKFLOW.md) |
| Run locally | [deployment/docker-compose/README.md](deployment/docker-compose/README.md) |

## Run

```shell
cd deployment/docker-compose
docker compose up -d
```

`./gradlew test` and `npm test` run from the repo root. Use JDK 21.


## Seed Data
```shell
npm run dev:seed
npm run dev:seed -- --delete # delete seed data
```
| Record | Value |
|---|---|
| User | dev-seed@example.com / dev-seed-pass |
| Organization | Dev Seed |
| Project | Playground |
| Environment | Local, with baseUrl set to http://localhost:3000 |