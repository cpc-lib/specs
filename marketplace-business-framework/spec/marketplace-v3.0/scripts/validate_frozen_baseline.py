from pathlib import Path
import yaml, json, re, sys, collections

root=Path(__file__).resolve().parents[1]
errors=[]

# ShardingSphere YAML uses custom tags such as !SHARDING.
class SpecYamlLoader(yaml.SafeLoader):
    pass

def _unknown_tag(loader, tag_suffix, node):
    if isinstance(node, yaml.MappingNode):
        return loader.construct_mapping(node, deep=True)
    if isinstance(node, yaml.SequenceNode):
        return loader.construct_sequence(node, deep=True)
    return loader.construct_scalar(node)

SpecYamlLoader.add_multi_constructor("!", _unknown_tag)

# YAML/JSON
for p in root.rglob("*.yaml"):
    try:
        yaml.load(p.read_text(encoding="utf-8"), Loader=SpecYamlLoader)
    except Exception as e:
        errors.append(f"YAML parse {p.relative_to(root)}: {e}")
for p in root.rglob("*.json"):
    try:
        json.loads(p.read_text(encoding="utf-8"))
    except Exception as e:
        errors.append(f"JSON parse {p.relative_to(root)}: {e}")

# Flyway version uniqueness and CREATE TABLE uniqueness
create_map=collections.defaultdict(list)
migration_count=0
for d in sorted((root/"03-database/flyway").iterdir()):
    if not d.is_dir(): continue
    versions=collections.defaultdict(list)
    for p in d.glob("V*__*.sql"):
        migration_count+=1
        m=re.match(r"V([^_]+)__",p.name)
        if m: versions[m.group(1)].append(p.name)
        txt=p.read_text(encoding="utf-8")
        for t in re.findall(r"CREATE\s+TABLE\s+([a-zA-Z0-9_]+)",txt,re.I):
            create_map[t.lower()].append(str(p.relative_to(root)))
    for v,fs in versions.items():
        if len(fs)>1:
            errors.append(f"Duplicate Flyway version {d.name}:V{v}: {fs}")
for t,fs in create_map.items():
    if len(fs)>1:
        errors.append(f"Duplicate CREATE TABLE {t}: {fs}")

# Final column model including ALTER ADD COLUMN.
columns=collections.defaultdict(set)
for p in (root/"03-database/flyway").rglob("*.sql"):
    txt=p.read_text(encoding="utf-8")
    for m in re.finditer(r"CREATE\s+TABLE\s+([a-zA-Z0-9_]+)\s*\((.*?)\);",txt,re.I|re.S):
        table=m.group(1).lower()
        for line in m.group(2).splitlines():
            line=line.strip().rstrip(",")
            if not line or line.upper().startswith(("PRIMARY KEY","UNIQUE KEY","KEY ","CONSTRAINT","FOREIGN KEY","CHECK ")):
                continue
            mm=re.match(r"`?([a-zA-Z0-9_]+)`?\s+",line)
            if mm: columns[table].add(mm.group(1).lower())
    for m in re.finditer(r"ALTER\s+TABLE\s+([a-zA-Z0-9_]+)(.*?);",txt,re.I|re.S):
        table=m.group(1).lower()
        body=m.group(2)
        for c in re.findall(r"ADD\s+COLUMN\s+`?([a-zA-Z0-9_]+)`?",body,re.I):
            columns[table].add(c.lower())

# OpenAPI
ops=set()
op_count=0
for p in (root/"04-openapi").glob("*.yaml"):
    doc=yaml.safe_load(p.read_text(encoding="utf-8"))
    if p.name.endswith("AUDIT.md"): continue
    if str((doc.get("info") or {}).get("version"))!="3.0":
        errors.append(f"OpenAPI version !=3.0: {p.name}")
    for path,item in (doc.get("paths") or {}).items():
        for method,op in item.items():
            if method.lower() not in {"get","post","put","patch","delete"}: continue
            op_count+=1
            oid=op.get("operationId")
            if not oid: errors.append(f"Missing operationId {p.name}:{method}:{path}")
            elif oid in ops: errors.append(f"Duplicate operationId {oid}")
            else: ops.add(oid)
            vars=set(re.findall(r"\{([^}]+)\}",path))
            declared={x.get("name") for x in op.get("parameters",[]) if x.get("in")=="path"}
            if vars-declared:
                errors.append(f"Missing path params {p.name}:{path}:{sorted(vars-declared)}")
            for ext in ("x-owner-service","x-command-query","x-auth-scope","x-idempotency-required"):
                if ext not in op: errors.append(f"Missing {ext}: {oid}")
            resp=op.get("responses") or {}
            success=next((v for k,v in resp.items() if str(k).startswith("2")),None)
            if not success or "content" not in success:
                errors.append(f"Missing structured success response: {oid}")
            for code in ("400","409","500"):
                if code not in resp: errors.append(f"Missing {code} response: {oid}")
            if op.get("x-idempotency-required"):
                params=op.get("parameters") or []
                if not any(x.get("name")=="Idempotency-Key" and x.get("in")=="header" for x in params):
                    errors.append(f"Missing Idempotency-Key header: {oid}")

# Events
reg=yaml.safe_load((root/"05-events/event-registry.yaml").read_text(encoding="utf-8"))
event_count=0
for e in reg.get("events",[]):
    event_count+=1
    sp=root/"05-events"/e["schema"]
    if not sp.exists():
        errors.append(f"Missing event schema: {e['eventType']} -> {e['schema']}")
        continue
    schema=json.loads(sp.read_text(encoding="utf-8"))
    const=(((schema.get("properties") or {}).get("eventType") or {}).get("const"))
    if const!=e["eventType"]:
        errors.append(f"Event const mismatch: {e['eventType']} != {const}")

ownership=yaml.safe_load((root/"11-codegen/EVENT-OWNERSHIP-MATRIX.yaml").read_text(encoding="utf-8"))
for e in reg.get("events",[]):
    entry=(ownership.get("events") or {}).get(e["eventType"])
    if not entry or entry.get("producerService") in (None,"SPEC-REVIEW"):
        errors.append(f"Missing event producer owner: {e['eventType']}")

# Sharding route keys
sharding=yaml.safe_load((root/"11-codegen/SHARDING-ROUTING-FROZEN.yaml").read_text(encoding="utf-8"))
for family,cfg in (sharding.get("families") or {}).items():
    rk=cfg["routeKey"]
    route_cols=[x.strip() for x in rk.split("+")]
    for table in cfg.get("bindingTables",[]):
        if table.lower() not in columns:
            errors.append(f"Sharding table not found: {family}:{table}")
            continue
        for c in route_cols:
            if c.lower() not in columns[table.lower()]:
                errors.append(f"Missing route key {c}: {family}:{table}")

# Tasks
tasks=list((root/"tasks").glob("TASK-*.md"))
if len(tasks)!=38: errors.append(f"Expected 38 tasks, found {len(tasks)}")
for p in tasks:
    txt=p.read_text(encoding="utf-8")
    if "MASTER-SPEC-V3.0.md" not in txt:
        errors.append(f"Task missing current MASTER: {p.name}")
    for old in ("MASTER-SPEC-V2.0.md","MASTER-SPEC-V2.1.md","MASTER-SPEC-V2.2.md","MASTER-SPEC-V2.3.md","MASTER-SPEC-V2.4.md","MASTER-SPEC-V2.5.md","MASTER-SPEC-V2.6.md"):
        if old in txt:
            errors.append(f"Task old MASTER ref {p.name}: {old}")

# Required frozen artifacts
required=[
"00-master/MASTER-SPEC-V3.0.md",
"00-master/V3.0-FROZEN-CONTRACT.md",
"01-architecture/DATABASE-OWNERSHIP-FROZEN.md",
"03-database/flyway/outbox/TEMPLATE-NOT-GLOBAL.md",
"11-codegen/SERVICE-TABLE-OWNERSHIP.yaml",
"11-codegen/MIGRATION-REGISTRY.yaml",
"11-codegen/OPENAPI-OPERATION-REGISTRY.yaml",
"11-codegen/API-COMMAND-MAPPING.yaml",
"11-codegen/EVENT-OWNERSHIP-MATRIX.yaml",
"11-codegen/SHARDING-ROUTING-FROZEN.yaml",
"11-codegen/E2E-TRACEABILITY-V3.yaml",
"13-acceptance/RELEASE-GATES-V3.0.md"
]
for rel in required:
    if not (root/rel).exists(): errors.append(f"Missing frozen artifact: {rel}")

if errors:
    print("\n".join(errors))
    sys.exit(1)
print(f"PASS: {op_count} API operations, {len(create_map)} DDL tables, {migration_count} Flyway migrations, {event_count} event schemas, {len(tasks)} tasks")
