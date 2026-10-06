#!/usr/bin/env bash
set -euo pipefail
BASE=${BASE_URL:-http://localhost:8080}
ROOT=$(cd "$(dirname "$0")" && pwd)

echo '1) Submit PulseForge sample'
curl -sS -X POST "$BASE/api/v1/ingestion/pulseforge" -H 'Content-Type: application/json' --data-binary @"$ROOT/src/test/resources/assessment/pulseforge_sample.json"; echo

echo '2) Submit ThermexWatch sample'
curl -sS -X POST "$BASE/api/v1/ingestion/thermexwatch" -H 'Content-Type: application/json' --data-binary @"$ROOT/src/test/resources/assessment/thermexwatch_sample.json"; echo

echo '3) Submit MaintaFlow sample'
curl -sS -X POST "$BASE/api/v1/ingestion/maintaflow" -H 'Content-Type: application/json' --data-binary @"$ROOT/src/test/resources/assessment/maintaflow_sample.json"; echo

echo 'Then use the returned ingestion IDs with GET /api/v1/ingestion/{id} and query machine views.'
