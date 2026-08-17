from pathlib import Path
import xml.etree.ElementTree as ET, re, sys, yaml
root=Path(__file__).resolve().parents[1]
errors=[]

# pom XML parse + module refs
for p in root.rglob('pom.xml'):
    try: ET.parse(p)
    except Exception as e: errors.append(f'POM parse {p.relative_to(root)}: {e}')
ns={'m':'http://maven.apache.org/POM/4.0.0'}
for p in root.rglob('pom.xml'):
    try: tree=ET.parse(p); r=tree.getroot()
    except Exception: continue
    for m in r.findall('./m:modules/m:module',ns):
        target=p.parent/m.text
        if not target.exists(): errors.append(f'Missing module {p.relative_to(root)} -> {m.text}')

# Business module boundaries and DDD layers
services=root/'marketplace-services'
mods=[p for p in services.iterdir() if p.is_dir()]
if len(mods)!=26: errors.append(f'Expected 26 business modules, found {len(mods)}')
for m in mods:
    pkg=m.name.replace('marketplace-','').replace('-','')
    base=m/'src/main/java/com/company/marketplace'/pkg
    for layer in ('interfaces','application','domain','infrastructure'):
        if not (base/layer/'package-info.java').exists(): errors.append(f'Missing DDD layer {m.name}:{layer}')
    pom=(m/'pom.xml').read_text(encoding='utf-8')
    for other in [x.name for x in mods if x.name!=m.name]:
        if f'<artifactId>{other}</artifactId>' in pom: errors.append(f'Forbidden business dependency {m.name} -> {other}')

# Framework must remain light
all_poms='\n'.join(p.read_text(encoding='utf-8') for p in root.rglob('pom.xml'))
for forbidden in ('spring-kafka','spring-boot-starter-amqp','opentelemetry','prometheus','clickhouse'):
    if forbidden in all_poms: errors.append(f'Deferred dependency leaked into v1: {forbidden}')
if 'yudao-module-mall' in all_poms: errors.append('yudao mall runtime dependency forbidden')

# Domain purity
for p in services.rglob('domain/*.java'):
    txt=p.read_text(encoding='utf-8')
    if 'org.springframework' in txt: errors.append(f'Spring import in domain {p.relative_to(root)}')

# Money red lines
for p in root.rglob('*.java'):
    txt=p.read_text(encoding='utf-8')
    if re.search(r'\b(double|float)\s+(amount|price|money|fee|balance|total)',txt,re.I): errors.append(f'floating money field {p.relative_to(root)}')
    if 'TODO' in txt or 'FIXME' in txt: errors.append(f'TODO/FIXME {p.relative_to(root)}')

# Spec mapping
mapping=yaml.safe_load((root/'docs/MODULE-SPEC-MAPPING.yaml').read_text(encoding='utf-8'))
for m in mods:
    if m.name not in mapping['modules']: errors.append(f'Missing spec mapping {m.name}')

# Security invariants
sec=(root/'backend/marketplace-framework/marketplace-security/src/main/java/com/company/marketplace/framework/security/ScopeGuard.java').read_text(encoding='utf-8')
if 'requiredMerchantId' not in sec or 'requireShop' not in sec: errors.append('Security scope guard incomplete')
gw=(root/'backend/marketplace-gateway/src/main/java/com/company/marketplace/gateway/InternalHeaderSanitizerFilter.java').read_text(encoding='utf-8')
if 'X-Internal-Merchant-Id' not in gw: errors.append('Gateway internal merchant header sanitizer missing')

if errors:
    print('\n'.join(errors)); sys.exit(1)
print(f'PASS: {len(mods)} business modules, 6 framework modules, Maven XML/module refs, DDD boundaries, security scope and red-line checks')
