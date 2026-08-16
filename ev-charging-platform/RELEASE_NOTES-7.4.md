# SPEC 7.4 Charging Hardening

## Scope

SPEC 7.4 hardens the first charging vertical slice instead of adding unrelated domains.

### Added

- Versioned time-of-use billing model.
- Peak / flat / valley pricing periods.
- Immutable billing snapshot with station timezone and pricing periods.
- Pure deterministic `TimeOfUseBillingEngine`.
- Meter-boundary interpolation for price-period splits.
- `charging_segment` and `charging_billing_result` persistence.
- Golden tests for cross-period and cross-midnight billing.
- Session recovery scan and `QUERY_TRANSACTION` command.
- Stateful simulator support for transaction recovery.
- WebSocket realtime charging stream.
- Admin Charger / Connector management page.
- Admin live charging session page.
- Billing configuration admin API.

## Important fixes

- Removed duplicate `charging:` YAML root keys in `charging-core/application.yml`.
- Recovered `CHARGING_STARTED` no longer overwrites an already-known initial meter.
- Billing no longer assumes one fixed energy/service price for the entire session.
- Repeated stop/recovery events remain protected by Inbox + unique order/session constraint.

## Release status

`foundation-rc-charging-hardening`

The package still requires the external-runtime release gate before it can be called
`foundation-verified`.

## Additional hardening

- Added Redis-backed `deviceId -> gatewayId|connectionToken` route leases.
- Core routes commands to `gateway.{gatewayId}` and each IoT instance owns `ev.device.command.gateway-{gatewayId}`.
- Disconnect uses compare-and-delete semantics so an old connection cannot remove the new cross-gateway lease.
- External runtime verification (Maven dependency resolution, Docker/Testcontainers, live Nacos/Kafka/RabbitMQ/Netty/browser E2E) remains gated by the execution environment and is not marked PASS.
