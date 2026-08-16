from pathlib import Path
import yaml,json,re,sys
root=Path(__file__).resolve().parents[1]
errors=[]; ops=set(); tables={}
class IgnoreUnknownTagLoader(yaml.SafeLoader): pass
def _unknown(loader, tag_suffix, node):
    if isinstance(node,yaml.MappingNode): return loader.construct_mapping(node)
    if isinstance(node,yaml.SequenceNode): return loader.construct_sequence(node)
    return loader.construct_scalar(node)
IgnoreUnknownTagLoader.add_multi_constructor('!',_unknown)
for p in root.rglob('*.yaml'):
    try:
        txt=p.read_text(encoding='utf-8')
        yaml.load(txt,Loader=IgnoreUnknownTagLoader) if 'shardingsphere' in str(p) else yaml.safe_load(txt)
    except Exception as e: errors.append(f'YAML {p}: {e}')
for p in root.rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: errors.append(f'JSON {p}: {e}')
for p in (root/'04-openapi').glob('*.yaml'):
    d=yaml.safe_load(p.read_text(encoding='utf-8')) or {}
    if not str(d.get('openapi','')).startswith('3.'): errors.append(f'OpenAPI version missing {p}')
    for path,item in (d.get('paths') or {}).items():
        for method,op in item.items():
            if method.lower() not in {'get','post','put','patch','delete'}: continue
            oid=op.get('operationId')
            if not oid: errors.append(f'Missing operationId {p}:{path}:{method}')
            elif oid in ops: errors.append(f'Duplicate operationId {oid}')
            else: ops.add(oid)
            vars=set(re.findall(r'\{([^}]+)\}',path)); declared={x.get('name') for x in op.get('parameters',[]) if x.get('in')=='path'}
            if vars-declared: errors.append(f'Missing path params {vars-declared} {p}:{path}:{method}')
for p in (root/'03-database/flyway').rglob('*.sql'):
    txt=p.read_text(encoding='utf-8')
    for t in re.findall(r'CREATE\s+TABLE\s+([a-zA-Z0-9_]+)',txt,re.I): tables.setdefault(t.lower(),[]).append(str(p.relative_to(root)))
for t,fs in tables.items():
    if len(fs)>1: errors.append(f'Duplicate CREATE TABLE {t}: {fs}')
tasks=list((root/'tasks').glob('TASK-*.md'))
if len(tasks)!=38: errors.append(f'Expected 38 tasks, got {len(tasks)}')
for p in tasks:
    txt=p.read_text(encoding='utf-8')
    if 'MASTER-SPEC-V2.6.md' not in txt: errors.append(f'Missing current MASTER ref in {p.name}')
    if 'MASTER-SPEC-V2.3.md' in txt or 'MASTER-SPEC-V2.2.md' in txt or 'MASTER-SPEC-V2.1.md' in txt or 'MASTER-SPEC-V2.0.md' in txt: errors.append(f'Old MASTER ref in {p.name}')
required=['00-master/MASTER-SPEC-V2.6.md','11-codegen/FINANCIAL-INVARIANTS.yaml','11-codegen/FUNDS-FLOW-MATRIX.yaml','11-codegen/REFUND-REVERSAL-MATRIX.yaml','11-codegen/ACCOUNTING-POSTING-MATRIX.yaml','11-codegen/RECONCILIATION-MATRIX.yaml','11-codegen/FULFILLMENT-QUANTITY-INVARIANTS.yaml','11-codegen/AFTERSALE-DECISION-MATRIX.yaml','11-codegen/RETURN-STOCK-DISPOSITION-MATRIX.yaml','11-codegen/PRICING-PIPELINE.yaml','11-codegen/PROMOTION-COMPATIBILITY-MATRIX.yaml','11-codegen/PROMOTION-QUOTA-INVARIANTS.yaml','11-codegen/PRODUCT-PUBLISH-CHECKLIST.yaml','11-codegen/FLASH-SALE-CONSISTENCY-MATRIX.yaml','13-acceptance/RELEASE-GATES-V2.6.md']
for r in required:
    if not (root/r).exists(): errors.append(f'Missing required {r}')
# Event registry schemas
reg=yaml.safe_load((root/'05-events/event-registry.yaml').read_text(encoding='utf-8'))
for e in reg.get('events',[]):
    sp=root/'05-events'/e['schema']
    if not sp.exists(): errors.append(f'Missing event schema {e["eventType"]}: {sp}')
if errors:
    print('\n'.join(errors)); sys.exit(1)
print(f'PASS: {len(ops)} API operations, {len(tables)} DDL tables, {len(list((root/"05-events/schemas").glob("*.json")))} event schemas, {len(tasks)} tasks')
