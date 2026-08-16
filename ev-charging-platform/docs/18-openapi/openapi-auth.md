# OpenAPI Authentication — SPEC 8.2

## Headers

Every `/open-api/**` request uses:

- `X-App-Key`
- `X-Timestamp` — Unix epoch seconds
- `X-Nonce`
- `X-Signature-Version: v1`
- `X-Signature`
- `X-Request-Id` — recommended; server generates one if omitted

## Canonical request

```text
HTTP_METHOD
REQUEST_PATH
CANONICAL_QUERY
SHA256_HEX(BODY)
TIMESTAMP
NONCE
```

The final signature is:

`hex(HMAC-SHA256(appSecret, canonicalRequest))`

Query parameters are URL-decoded, sorted by key then value, RFC3986-encoded, and joined with `&`.

## Replay protection

- timestamp skew: 300 seconds by default
- nonce TTL: 600 seconds by default
- nonce is consumed only after the HMAC signature is valid
- Redis `SET NX` is the replay integrity guard

## Rate limit

A Redis atomic counter applies the Partner's configured requests/minute limit.

Rate limiting is server-side and cannot be changed by request headers.

## AppSecret storage

Partner secrets are encrypted at rest with AES-256-GCM.

The OpenAPI Master Key:

- is never returned through OpenAPI;
- must be supplied through deployment secret management;
- must be Base64 encoding of exactly 32 random bytes;
- must not use the development default in production.

AppSecret rotation immediately invalidates signatures made with the old secret.

## Request body limit

The OpenAPI authentication filter hashes the actual request body before controller deserialization.

Default maximum body size: 1 MiB.
