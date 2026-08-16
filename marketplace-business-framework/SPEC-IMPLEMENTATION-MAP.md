# SPEC → Framework Implementation Map

## V3.0 frozen business boundaries
Mapped into 26 top-level Marketplace modules.

## Framework
- common → generic response/page/error/domain primitives
- web → validation/web exception conventions
- security → Platform/Merchant/Shop scope
- mybatis → MyBatis-Plus persistence base
- redis → Redis/Redisson access
- file → MinIO/FileId abstraction

## Business code
Each top-level Marketplace module contains:
`interfaces / application / domain / infrastructure`.

No concrete business aggregate behavior is fabricated at framework stage.
Use the corresponding `MODULE-SPEC.md` and Marketplace V3.0 SPEC when implementing a domain.
