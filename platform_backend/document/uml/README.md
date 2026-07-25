# UML Diagrams

This folder contains PlantUML diagrams for the automation testing platform backend.

## Existing

- `database-design.puml` — ER-style database design and focused data-model diagrams.
- `test.puml` — PlantUML/Graphviz diagnostic file using `testdot`.

## Generated from source scan

- `service-architecture.puml` — High-level service architecture for gateway, admin, engine, report, shared modules, Kafka, Redis, PostgreSQL, and Elasticsearch.
- `execution-trigger-sequence.puml` — End-to-end sequence from execution trigger API to Kafka, Vert.x engine execution, Redis live state, and report indexing.
- `execution-state.puml` — State diagrams for `TestRun`, `TestCaseResult`, and engine live execution state.
- `testcase-authoring-sequence.puml` — Create/update test case flow through admin REST API, use cases, domain aggregate, persistence, and Kafka lifecycle event publishing.
- `report-live-and-query-sequence.puml` — Report-service SSE live tracking and historical report query flow.
- `kafka-topic-flow.puml` — Kafka producer/consumer topic map, including current topic naming mismatches found in code.

## Render locally

If PlantUML is installed:

```zsh
cd /Users/kenwu/tech/automation_testing_platform/platform_backend
plantuml document/uml/*.puml
```

For syntax checking only:

```zsh
cd /Users/kenwu/tech/automation_testing_platform/platform_backend
plantuml -checkonly document/uml/*.puml
```

