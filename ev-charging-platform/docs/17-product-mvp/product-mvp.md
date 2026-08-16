# SPEC 8.0 — Product MVP

Status: `foundation-rc-product-mvp`

## Objective

Turn the accumulated domain vertical slices into usable product surfaces.

SPEC 8.0 intentionally does **not** add another major backend bounded context.

## Product surfaces

### Admin Web

- authentication
- dashboard
- station / charger / connector
- charging / billing
- payment / refund
- ledger / reconciliation / settlement / adjustment / invoice
- alarms / work orders / inspections / spare parts / notifications
- system user / role / DataScope visibility

### Merchant Web

- authentication
- operational overview
- scoped stations
- scoped charging/order view
- payment view
- settlement view
- alarm/work-order view

### Driver UniApp

- public station discovery
- station / connector detail
- login
- start charging
- charging progress polling
- stop charging
- order history
- payment initiation
- account/logout

### Technician UniApp

- authentication
- assigned maintenance work orders
- repair actions
- repair attachments
- inspection tasks
- spare-stock lookup

## Non-goals

- social login
- wallet/coupon/member tier
- full map SDK integration
- production OAuth authorization server
- production SMS/WeChat notification provider
- full regulatory UI
