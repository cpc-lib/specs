# Admin Web Product MVP

## Navigation

1. Dashboard
2. Stations
3. Chargers & Connectors
4. Realtime Charging
5. Billing
6. Payments
7. Ledger
8. Reconciliation
9. Settlement
10. Adjustments
11. Invoices
12. Alarms
13. Maintenance
14. Inspections
15. Spare Parts
16. Notifications
17. System / RBAC

## Dashboard fan-out

The browser calls bounded-context dashboard APIs independently:

- Asset
- Core
- Payment
- Operation

There is no cross-service reporting SQL in the Gateway.

## Maker-checker

Finance approval pages no longer spoof `X-User-Id`.

Use two real administrator accounts for maker/checker testing.
