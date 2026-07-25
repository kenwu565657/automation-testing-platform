# report-service

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/kotlin-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                               | Description                                                 |
|----------------------------------------------------|-------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing-default) | Allows to define structured routes and associated handlers. |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                                    | Description                                                          |
|-----------------------------------------|----------------------------------------------------------------------|
| `./gradlew test`                        | Run the tests                                                        |
| `./gradlew build`                       | Build everything                                                     |
| `./gradlew buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `./gradlew buildImage`                  | Build the docker image to use with the fat JAR                       |
| `./gradlew publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `./gradlew run`                         | Run the server                                                       |
| `./gradlew runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

REST API Summary
Method	Path	Description
GET	/info	Service health/info
GET	/api/v1/reports/{executionId}	Get a single report by execution ID
GET	/api/v1/reports/test-case/{testCaseId}?limit=20	Get recent reports for a test case
GET	/api/v1/reports/search?testCaseName=&status=&testType=&platform=&from=&to=&page=&size=	Full-text search with filters
GET	/api/v1/summary?from=&to=	Aggregated summary (pass/fail counts, avg duration)
GET	/api/v1/trends/{testCaseId}?days=30	Daily pass/fail trend for a test case
GET	/api/v1/flaky-tests?minExecutions=5&maxPassRate=0.8	Detect flaky tests
SSE	/api/v1/executions/{executionId}/live?interval=1000	Real-time execution tracking stream

Engine Service (Vert.x)                         Report Service (Ktor)
─────────────────────                           ────────────────────

Step completes
│
├─ Redis: save execution state ──────────────→ LiveExecutionTracker
│                                                  │
│                                                  └─ SSE /live endpoint
│                                                       │
└─ Kafka: test-step.completed ──────────────→ KafkaEventHandler
(logged only)

Execution completes
│
├─ Redis: final state ─────────────────────→ LiveExecutionTracker
│                                                (stream ends)
│
└─ Kafka: test-execution.completed ────────→ KafkaEventHandler
│
▼
ReportAggregationService
│
▼
ElasticsearchReportRepository
│
▼
Elasticsearch (indexed)
│
▼
REST API queries
(search, summary, trends, flaky)
