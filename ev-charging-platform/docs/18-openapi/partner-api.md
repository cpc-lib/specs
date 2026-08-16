# Partner API — SPEC 8.2

## Scopes

Current scopes:

- `station:read`
- `charging:write`
- `order:read`

## DataScope

Partner DataScope:

- `ALL`
- `STATION`

For `STATION`, explicit station IDs are mandatory.

The scope is enforced on:

- station list/detail;
- connector → station validation before remote charging start;
- order station ownership.

Knowing a valid connector code does not bypass Partner Station Scope.

## Partner user bridge

A third-party `externalUserId` is mapped to a stable local shadow `user_id`.

Mapping uniqueness:

`partnerId + externalUserId`

The shadow ID is used only as the charging ownership identity inside Core. It does not pretend to be a native IAM user.

## Remote charging

```text
Partner request
→ HMAC auth
→ charging:write
→ Connector authoritative Asset lookup
→ Partner Station Scope
→ Partner external user mapping
→ Internal SERVICE call
→ Core charging start
```

OpenAPI request IDs are namespaced with `partnerId` before reaching Core, preventing idempotency collisions between different Partners.

## Current API

- `GET /open-api/v1/stations`
- `GET /open-api/v1/stations/{stationId}`
- `POST /open-api/v1/charging/start`
- `POST /open-api/v1/charging/{sessionNo}/stop`
- `GET /open-api/v1/orders/{orderNo}`

Payment clearing between roaming partners is deliberately outside SPEC 8.2.
