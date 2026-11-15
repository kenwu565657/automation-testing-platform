#!/bin/sh
set -eu
topics="
test.execution.request
test.run.cancelled
engine.assignment
engine.status
test.run.started
test.run.completed
test.step.completed
test.case.created
test.case.updated
"
for topic in $topics; do
  /opt/kafka/bin/kafka-topics.sh \
    --bootstrap-server kafka:29092 \
    --create --if-not-exists \
    --topic "$topic" \
    --partitions 1 \
    --replication-factor 1
done
echo "kafka topics ready"
