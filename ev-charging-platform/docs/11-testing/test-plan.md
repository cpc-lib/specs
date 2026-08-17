# SPEC 7.1 Test Plan

## Local
- XML/YAML/JSON parse
- Java 21 pure-domain compile
- Device Simulator compile
- static host-port collision check

## Developer/CI
- `cd backend && mvn clean verify`
- Testcontainers MySQL integration test
- `docker compose config`
- `docker compose up -d` health
- `npm ci && npm run build` after lockfiles are generated
- Simulator ↔ Netty E2E
