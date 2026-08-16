#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
python3 scripts/validate_static.py
python3 scripts/check_jdbc_placeholders.py
python3 scripts/capacity_model.py
./scripts/domain_harness.sh
./scripts/finance_harness.sh
./scripts/operation_harness.sh
./scripts/product_harness.sh
./scripts/openapi_harness.sh
./mvnw -B -ntp clean verify
(
  cd admin-web
  npm install
  npm run build
)
(
  cd merchant-web
  npm install
  npm run build
)
mkdir -p build/simulator
javac --release 21 -d build/simulator device-simulator/src/main/java/com/example/evcharging/simulator/DeviceSimulator.java
echo "VERIFY=PASS"
