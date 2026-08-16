# Validation Report V3.0 Frozen Baseline

## Frozen validator
```text
PASS: 102 API operations, 159 DDL tables, 51 Flyway migrations, 93 event schemas, 38 tasks
Spreadsheet runtime warmup failed during python startup
Traceback (most recent call last):
  File "/tmp/tmp.yTcnQsZYiA/artifact_tool_v2-2.8.4/artifact_tool/patches/warm_spreadsheet_runtime_on_startup.py", line 26, in warm_spreadsheet_runtime_on_startup
  File "/tmp/tmp.yTcnQsZYiA/artifact_tool_v2-2.8.4/artifact_tool/spreadsheet_warmup.py", line 785, in warm_spreadsheet_runtime
  File "/tmp/tmp.yTcnQsZYiA/artifact_tool_v2-2.8.4/artifact_tool/spreadsheet_warmup.py", line 720, in _warm_feature_flows
  File "/tmp/tmp.yTcnQsZYiA/artifact_tool_v2-2.8.4/artifact_tool/spreadsheet_warmup.py", line 704, in _warm_collaboration_flows
  File "/tmp/tmp.yTcnQsZYiA/artifact_tool_v2-2.8.4/artifact_tool/generated/interface/models.py", line 30820, in hydrate_crdt_from_proto
  File "/tmp/tmp.yTcnQsZYiA/artifact_tool_v2-2.8.4/artifact_tool/rpc/remote.py", line 749, in __call__
  File "/tmp/tmp.yTcnQsZYiA/artifact_tool_v2-2.8.4/artifact_tool/rpc/client.py", line 150, in call
artifact_tool.rpc.client.RemoteError: hydrateCrdtFromProto requires an empty collaborative document.
```

## Status
PASS

## Validation scope
- YAML/JSON, including ShardingSphere custom YAML tags
- Flyway migration version uniqueness
- duplicate logical table creation
- OpenAPI 3.0 contracts / operation ownership / idempotency / error responses
- event registry/schema/producer ownership
- frozen sharding route-key availability
- 38 TASK freeze references
- required frozen registries and release gates

## Not yet claimed
This is structural/spec validation. It does not claim live MySQL migration execution,
real ShardingSphere startup, provider sandbox success, load testing, or generated Java test execution.
