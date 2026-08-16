# V3.0 Codegen Entrypoint

For every TASK:
1. `00-master/MASTER-SPEC-V3.0.md`
2. `00-master/V3.0-FROZEN-CONTRACT.md`
3. `01-architecture/DATABASE-OWNERSHIP-FROZEN.md`
4. `11-codegen/SERVICE-TABLE-OWNERSHIP.yaml`
5. `11-codegen/OPENAPI-OPERATION-REGISTRY.yaml`
6. `11-codegen/API-COMMAND-MAPPING.yaml`
7. `11-codegen/EVENT-OWNERSHIP-MATRIX.yaml`
8. `11-codegen/TRANSACTION-LOCK-MATRIX.yaml`
9. `11-codegen/IDEMPOTENCY-MATRIX.yaml`
10. `11-codegen/SHARDING-ROUTING-FROZEN.yaml`
11. relevant Domain / DDL / OpenAPI / Event schemas
12. `08-tests/**`
13. corresponding `tasks/TASK-xxx.md`
14. corresponding `14-task-bundles/TASK-xxx/CONTEXT.md`

Generated code must include a `SPEC-IMPLEMENTATION-MAP.md` linking code modules/classes/tests back to contracts.
