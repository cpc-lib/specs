#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
TENANT_ID="${TENANT_ID:-1}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123456}"
DRIVER_USER="${DRIVER_USER:-driver}"
DRIVER_PASSWORD="${DRIVER_PASSWORD:-driver123456}"

json_field() {
  python3 -c 'import json,sys; x=json.load(sys.stdin); v=x["data"]; 
for p in sys.argv[1].split("."): v=v[p]
print(v)' "$1"
}

login() {
  local user="$1" pass="$2"
  curl -fsS -H 'Content-Type: application/json' \
    -d "{\"tenantId\":${TENANT_ID},\"username\":\"${user}\",\"password\":\"${pass}\"}" \
    "${BASE_URL}/auth-api/v1/login"
}

ADMIN_LOGIN="$(login "$ADMIN_USER" "$ADMIN_PASSWORD")"
ADMIN_TOKEN="$(printf '%s' "$ADMIN_LOGIN" | json_field accessToken)"
ADMIN_REFRESH="$(printf '%s' "$ADMIN_LOGIN" | json_field refreshToken)"

ME_CODE="$(curl -sS -o /tmp/ev-me.json -w '%{http_code}' -H "Authorization: Bearer ${ADMIN_TOKEN}" "${BASE_URL}/auth-api/v1/me")"
test "$ME_CODE" = "200"

# Internal endpoint must never be anonymous.
INTERNAL_CODE="$(curl -sS -o /dev/null -w '%{http_code}' "${BASE_URL}/internal-api/v1/core/orders/NO/payment-snapshot")"
test "$INTERNAL_CODE" = "401"

DRIVER_LOGIN="$(login "$DRIVER_USER" "$DRIVER_PASSWORD")"
DRIVER_TOKEN="$(printf '%s' "$DRIVER_LOGIN" | json_field accessToken)"
ADMIN_DENY="$(curl -sS -o /dev/null -w '%{http_code}' -H "Authorization: Bearer ${DRIVER_TOKEN}" "${BASE_URL}/admin-api/v1/system/users")"
test "$ADMIN_DENY" = "403"

REFRESHED="$(curl -fsS -H 'Content-Type: application/json' -d "{\"refreshToken\":\"${ADMIN_REFRESH}\"}" "${BASE_URL}/auth-api/v1/refresh")"
NEW_TOKEN="$(printf '%s' "$REFRESHED" | json_field accessToken)"
NEW_REFRESH="$(printf '%s' "$REFRESHED" | json_field refreshToken)"

OLD_REFRESH_CODE="$(curl -sS -o /dev/null -w '%{http_code}' -H 'Content-Type: application/json' \
  -d "{\"refreshToken\":\"${ADMIN_REFRESH}\"}" "${BASE_URL}/auth-api/v1/refresh")"
test "$OLD_REFRESH_CODE" = "403"

curl -fsS -X POST -H "Authorization: Bearer ${NEW_TOKEN}" "${BASE_URL}/auth-api/v1/logout" >/dev/null
REVOKED_CODE="$(curl -sS -o /dev/null -w '%{http_code}' -H "Authorization: Bearer ${NEW_TOKEN}" "${BASE_URL}/auth-api/v1/me")"
test "$REVOKED_CODE" = "401"

echo "PRODUCT_E2E_SMOKE=PASS"
