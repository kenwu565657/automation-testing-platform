#!/bin/sh
set -eu
if curl -sf -o /dev/null "http://elasticsearch:9200/test-reports"; then
  echo "test-reports already exists"
  exit 0
fi
curl -sf -X PUT "http://elasticsearch:9200/test-reports" \
  -H "Content-Type: application/json" \
  --data-binary @/mapping/test-reports.json >/dev/null
echo "created test-reports"
