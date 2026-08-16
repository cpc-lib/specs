# marketplace-invoice

## Responsibility
Invoice application, issue, red flush, reissue and delivery.

## Marketplace V3.0 concepts mapped here
- `InvoiceApplication`
- `InvoiceIssue`
- `RedFlush`
- `Reissue`
- `InvoiceDelivery`

## DDD skeleton
- `interfaces/rest`
- `application/command`
- `application/query`
- `application/service`
- `domain/model`
- `domain/repository`
- `domain/service`
- `domain/policy`
- `domain/event`
- `infrastructure/persistence`
- `infrastructure/config`

## Domain slices
- `domain/application`
- `domain/issue`
- `domain/redflush`
- `domain/reissue`
- `domain/delivery`

## Dependency rule
This business module may depend on `marketplace-framework` modules.
It MUST NOT add a direct Maven dependency on another Marketplace business module.

Cross-domain behavior must later use:
- explicit internal API,
- published application contract,
- or event/Saga integration required by the SPEC.

## Authoritative SPEC paths
- `spec/marketplace-v3.0/02-domain/invoice/`

- `spec/marketplace-v3.0/11-codegen/`
- `spec/marketplace-v3.0/08-tests/`
- `spec/marketplace-v3.0/tasks/`

Do not copy these source files into this module.
