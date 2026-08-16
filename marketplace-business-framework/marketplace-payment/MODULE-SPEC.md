# marketplace-payment

## Responsibility
Payment order/attempt/transaction, provider adapters and refund money facts.

## Marketplace V3.0 concepts mapped here
- `PaymentOrder`
- `PaymentAttempt`
- `PaymentTransaction`
- `RefundOrder`
- `RefundTransaction`
- `UNKNOWN semantics`

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
- `domain/paymentorder`
- `domain/paymentattempt`
- `domain/paymenttransaction`
- `domain/refund`
- `domain/provider`
- `domain/callback`

## Dependency rule
This business module may depend on `marketplace-framework` modules.
It MUST NOT add a direct Maven dependency on another Marketplace business module.

Cross-domain behavior must later use:
- explicit internal API,
- published application contract,
- or event/Saga integration required by the SPEC.

## Authoritative SPEC paths
- `spec/marketplace-v3.0/02-domain/payment/`
- `spec/marketplace-v3.0/04-openapi/payment.yaml`
- `spec/marketplace-v3.0/03-database/flyway/payment/`

- `spec/marketplace-v3.0/11-codegen/`
- `spec/marketplace-v3.0/08-tests/`
- `spec/marketplace-v3.0/tasks/`

Do not copy these source files into this module.
