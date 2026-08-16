# Module boundaries

## Framework
Reusable technical primitives only. It must not know Trade/Product/Payment business rules.

## System
Platform-neutral RBAC/dictionary/config/audit. It does not own merchant business membership.

## Business services
Each service owns one bounded context and its persistence model. In framework v1.0 business services have no Maven dependencies on each other.

## Cross-domain later
Use stable application contracts/internal APIs/events when a concrete TASK is implemented. Never share Mapper/Repository classes across services.
