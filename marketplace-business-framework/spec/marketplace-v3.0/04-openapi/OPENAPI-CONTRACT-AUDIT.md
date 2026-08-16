# OpenAPI Contract Audit — V3.0

V3.0 standardizes every operation with:
- `operationId`
- `x-owner-service`
- `x-command-query`
- `x-auth-scope`
- `x-idempotency-required`
- structured 2xx response
- 400 / 409 / 500 error envelope

POST business commands require `Idempotency-Key` unless the operation is a provider callback/tracking/receipt type with provider-native deduplication.

Targeted missing bodies hardened:
- additional review
- offer publish request
- customer service case create
- IM conversation create
- notification preference update
- search reindex
- repair execution
- finance daily close command
- inventory reservation commit/release

`OPENAPI-OPERATION-REGISTRY.yaml` is the machine-readable codegen index.
