# SPEC 8.0 Progress — Product MVP

Status: `foundation-rc-product-mvp`

## Scope

SPEC 8.0 maps to the existing Product phases:

- W30-W33 / 20 person-days — Admin + Merchant
- W34-W37 / 20 person-days — Driver UniApp

Total Product MVP budget: **40 person-days / 8 weeks**.

The Production V1 baseline remains:

**50 weeks / 250 person-days**

## RC assets completed

### Security

- signed access token
- AccessPrincipal
- surface-role enforcement
- permission annotation/interceptor infrastructure
- DataScope model
- station-scoped merchant Asset/Core queries
- fail-closed merchant projections for unsupported station scope
- internal service identity for Feign
- secure default: dev identity headers disabled
- secure default: demo user bootstrap disabled

### Admin

- product login
- real dashboard
- system/RBAC page
- existing domain pages integrated into one authenticated shell

### Merchant

- login
- overview
- station view
- order view
- payment view
- settlement view
- operation view

### Driver

- public station discovery
- station/connector detail
- login
- start/stop charging
- order list
- payment initiation
- account/logout

### Technician

- migrated from development headers to bearer-token login

## Remaining Product MVP hardening

- real npm/UniApp builds
- browser route/error/loading polish
- proper role/permission management CRUD
- password reset/change
- token revocation/refresh
- Merchant station projections in Payment/Finance/Operation
- map SDK
- live WebSocket consumption in Driver app
- UX acceptance testing
