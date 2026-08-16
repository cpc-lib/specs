# Merchant Web Product MVP

Merchant portal is read-oriented in SPEC 8.0.

## Pages

- Overview
- Stations
- Orders
- Payments
- Settlement
- Operation

## DataScope rule

`TENANT` merchant sees the tenant/operator portal view.

`STATION` merchant only receives station-scoped data where the local bounded context has enough projection data to enforce station scope.

If a service cannot enforce station scope locally, it returns forbidden rather than leaking tenant data.

Future projection hardening should add stationId/operatorId to Payment/Finance/Operation merchant read models.
