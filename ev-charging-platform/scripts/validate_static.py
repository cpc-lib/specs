from pathlib import Path
import json
import re
import sys
import xml.etree.ElementTree as ET

try:
    import yaml
except Exception:
    yaml = None

root = Path(__file__).resolve().parents[1]
errors = []
notes = []

# XML / POM
for p in root.rglob('*.xml'):
    try:
        ET.parse(p)
    except Exception as e:
        errors.append(f'XML {p.relative_to(root)}: {e}')

# JSON contracts / package files
for p in root.rglob('*.json'):
    try:
        json.loads(p.read_text(encoding='utf-8'))
    except Exception as e:
        errors.append(f'JSON {p.relative_to(root)}: {e}')

# YAML
if yaml:
    for p in list(root.rglob('*.yml')) + list(root.rglob('*.yaml')):
        try:
            yaml.safe_load(p.read_text(encoding='utf-8'))
        except Exception as e:
            errors.append(f'YAML {p.relative_to(root)}: {e}')
else:
    notes.append('PyYAML unavailable: YAML parse skipped')

# Maven modules must physically exist.
root_pom = ET.parse(root / 'backend' / 'pom.xml').getroot()
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
for node in root_pom.findall('./m:modules/m:module', ns):
    rel = node.text.strip()
    if not (root / 'backend' / rel / 'pom.xml').exists():
        errors.append(f'Maven module missing pom: {rel}')

# Old Testcontainers 1.x package is invalid for the pinned 2.x MySQL module.
for p in root.rglob('*.java'):
    text = p.read_text(encoding='utf-8')
    if 'org.testcontainers.containers.MySQLContainer' in text:
        errors.append(f'Old Testcontainers MySQL import: {p.relative_to(root)}')

# 2025.x Nacos config services must use spring.config.import.
for p in root.glob('backend/*/src/main/resources/application.yml'):
    text = p.read_text(encoding='utf-8')
    if 'spring-cloud-starter-alibaba-nacos-config' in (p.parents[3] / 'pom.xml').read_text(encoding='utf-8'):
        if 'optional:nacos:' not in text:
            errors.append(f'Nacos config import missing: {p.relative_to(root)}')
        if 'import-check:' in text:
            errors.append(f'Legacy Nacos import-check workaround remains: {p.relative_to(root)}')

# Gateway lb:// requires LoadBalancer starter.
gateway_pom = (root / 'backend/charging-gateway/pom.xml').read_text(encoding='utf-8')
gateway_yml = (root / 'backend/charging-gateway/src/main/resources/application.yml').read_text(encoding='utf-8')
if 'lb://' in gateway_yml and 'spring-cloud-starter-loadbalancer' not in gateway_pom:
    errors.append('Gateway uses lb:// but spring-cloud-starter-loadbalancer is missing')

# Docker host port collision check.
compose = root / 'deploy/docker/docker-compose.yml'
if yaml and compose.exists():
    data = yaml.safe_load(compose.read_text(encoding='utf-8')) or {}
    seen = {}
    for name, svc in (data.get('services', {}) or {}).items():
        for item in svc.get('ports', []) or []:
            host = str(item).split(':')[0]
            if host in seen:
                errors.append(f'Docker host port collision {host}: {seen[host]} vs {name}')
            seen[host] = name

# Empty frontend dependency regression guard.
for app in ('admin-web', 'merchant-web'):
    pkg = json.loads((root / app / 'package.json').read_text(encoding='utf-8'))
    if not pkg.get('dependencies') or not pkg.get('devDependencies'):
        errors.append(f'{app}: dependencies/devDependencies must not be empty')
    if not (root / app / 'src/pages/StationPage.tsx').exists():
        errors.append(f'{app}: StationPage.tsx missing')

# IoT must deliver a Rabbit command to a real channel, not merely log it.
iot_msg = (root / 'backend/charging-iot/src/main/java/com/example/evcharging/iot/messaging/DeviceCommandMessaging.java').read_text(encoding='utf-8')
if 'channel.writeAndFlush' not in iot_msg:
    errors.append('IoT Rabbit consumer does not deliver command to Netty channel')


# SPEC 7.3 vertical-slice regression guards.
required_73 = [
    'backend/charging-asset/src/main/java/com/example/evcharging/asset/charger/AssetDeviceController.java',
    'backend/charging-core/src/main/java/com/example/evcharging/core/charging/application/ChargingController.java',
    'backend/charging-core/src/main/resources/db/migration/V1.1.0__charging_vertical_slice.sql',
    'docs/13-project-management/one-person-ai-development-plan.md',
]
for rel in required_73:
    if not (root / rel).exists():
        errors.append(f'SPEC 7.3 required file missing: {rel}')
core_pom_text=(root/'backend/charging-core/pom.xml').read_text(encoding='utf-8')
if 'spring-cloud-starter-openfeign' not in core_pom_text:
    errors.append('SPEC 7.3: Core OpenFeign dependency missing')
sim=(root/'device-simulator/src/main/java/com/example/evcharging/simulator/DeviceSimulator.java').read_text(encoding='utf-8')
for token in ('CHARGING_STARTED','TELEMETRY','CHARGING_STOPPED'):
    if token not in sim: errors.append(f'SPEC 7.3 simulator behavior missing: {token}')


# SPEC 7.4 hardening guards.
required_74 = [
    'backend/charging-core/src/main/java/com/example/evcharging/core/billing/domain/TimeOfUseBillingEngine.java',
    'backend/charging-core/src/main/java/com/example/evcharging/core/charging/recovery/ChargingRecoveryService.java',
    'backend/charging-core/src/main/java/com/example/evcharging/core/charging/realtime/ChargingWebSocketConfig.java',
    'backend/charging-core/src/main/resources/db/migration/V1.2.0__charging_hardening.sql',
    'admin-web/src/pages/DevicePage.tsx',
    'admin-web/src/pages/ChargingPage.tsx',
    'admin-web/src/pages/BillingPage.tsx',
    'backend/charging-core/src/main/java/com/example/evcharging/core/charging/realtime/ChargingRealtimeTicketService.java',
    'backend/charging-core/src/main/java/com/example/evcharging/core/billing/application/BillingReplayService.java',
]
for rel in required_74:
    if not (root / rel).exists(): errors.append(f'SPEC 7.4 required file missing: {rel}')
core_yml=(root/'backend/charging-core/src/main/resources/application.yml').read_text(encoding='utf-8')
if core_yml.count('\ncharging:\n') != 1:
    errors.append('SPEC 7.4: charging-core application.yml must contain one charging root key')
core_migration=(root/'backend/charging-core/src/main/resources/db/migration/V1.2.0__charging_hardening.sql').read_text(encoding='utf-8')
for token in ('billing_version','billing_period','charging_segment','charging_billing_result','charging_recovery_record'):
    if token not in core_migration: errors.append(f'SPEC 7.4 migration missing: {token}')
sim74=(root/'device-simulator/src/main/java/com/example/evcharging/simulator/DeviceSimulator.java').read_text(encoding='utf-8')
if 'QUERY_TRANSACTION' not in sim74: errors.append('SPEC 7.4 simulator recovery command missing')
gateway74=(root/'backend/charging-gateway/src/main/resources/application.yml').read_text(encoding='utf-8')
if 'lb:ws://charging-core' not in gateway74: errors.append('SPEC 7.4 websocket gateway route missing')


admin_vite=(root/'admin-web/vite.config.ts').read_text(encoding='utf-8')
if "'/ws'" not in admin_vite or 'ws: true' not in admin_vite:
    errors.append('SPEC 7.4: Admin Vite websocket proxy missing')
ws_config=(root/'backend/charging-core/src/main/java/com/example/evcharging/core/charging/realtime/ChargingWebSocketConfig.java').read_text(encoding='utf-8')
if 'TicketHandshakeInterceptor' not in ws_config or 'query(request.getURI()).get("tenantId")' in ws_config:
    errors.append('SPEC 7.4: WebSocket must authenticate using short-lived ticket, not tenant query input')
recovery_service=(root/'backend/charging-core/src/main/java/com/example/evcharging/core/charging/recovery/ChargingRecoveryService.java').read_text(encoding='utf-8')
if 'ChargingRecoveryWorker' not in recovery_service:
    errors.append('SPEC 7.4: Recovery scanner must delegate to transactional worker (avoid self-invocation)')

core_config=root/'backend/charging-core/src/main/java/com/example/evcharging/core/config/CoreInfrastructureConfiguration.java'
if not core_config.exists() or 'SnowflakeIdGenerator' not in core_config.read_text(encoding='utf-8'):
    errors.append('SPEC 7.4: Core IdGenerator bean missing')
command_pub=(root/'backend/charging-core/src/main/java/com/example/evcharging/core/charging/infrastructure/DeviceCommandOutboxPublisher.java').read_text(encoding='utf-8')
for token in ('CorrelationData','PUBLISHING','stale publishing claim recovered'):
    if token not in command_pub: errors.append(f'SPEC 7.4 command-outbox hardening missing: {token}')

# SPEC 7.4 route-aware command / broker delivery hardening.
iot_yml=(root/'backend/charging-iot/src/main/resources/application.yml').read_text(encoding='utf-8')
if 'gateway-id:' not in iot_yml:
    errors.append('SPEC 7.4: IoT gateway-id configuration missing')
if 'default-requeue-rejected: false' not in iot_yml:
    errors.append('SPEC 7.4: IoT command listener must not requeue poison/offline messages forever')
if 'template:\n      mandatory: true' not in core_yml:
    errors.append('SPEC 7.4: RabbitTemplate mandatory publishing missing')
for token in ('device:route:', 'DeviceRouteLease.parse', 'routingKeyPrefix + route.gatewayId()'):
    if token not in command_pub:
        errors.append(f'SPEC 7.4 route-aware command dispatch missing: {token}')
iot_server=(root/'backend/charging-iot/src/main/java/com/example/evcharging/iot/gateway/NettyDeviceServer.java').read_text(encoding='utf-8')
for token in ('CONNECTION_LEASE', 'device:route:', 'RELEASE_LEASE', 'iot.gateway-id'):
    if token not in iot_server:
        errors.append(f'SPEC 7.4 distributed device lease missing: {token}')
iot_messaging=(root/'backend/charging-iot/src/main/java/com/example/evcharging/iot/messaging/DeviceCommandMessaging.java').read_text(encoding='utf-8')
if '#{deviceCommandQueue.name}' not in iot_messaging or '"gateway." + gatewayId' not in iot_messaging:
    errors.append('SPEC 7.4: IoT queue/routing must be gateway-specific')


# SPEC 7.5 payment/ledger/project-plan guards.
required_75 = [
    'backend/charging-payment/src/main/resources/db/migration/V1.0.0__payment_vertical_slice.sql',
    'backend/charging-payment/src/main/java/com/example/evcharging/payment/application/PaymentApplicationService.java',
    'backend/charging-payment/src/main/java/com/example/evcharging/payment/infrastructure/PaymentEventOutboxPublisher.java',
    'backend/charging-core/src/main/java/com/example/evcharging/core/trade/PaymentProjectionConsumer.java',
    'backend/charging-finance/src/main/resources/db/migration/V1.0.0__ledger_vertical_slice.sql',
    'backend/charging-finance/src/main/java/com/example/evcharging/finance/ledger/PaymentLedgerConsumer.java',
    'docs/13-project-management/roadmap.md',
    'docs/13-project-management/milestones.md',
    'docs/13-project-management/sprint-plan.md',
    'docs/13-project-management/task-estimates.md',
    'docs/13-project-management/release-gates.md',
]
for rel in required_75:
    if not (root / rel).exists(): errors.append(f'SPEC 7.5 required file missing: {rel}')
payment_pom=(root/'backend/charging-payment/pom.xml').read_text(encoding='utf-8')
for dep in ('spring-boot-starter-jdbc','spring-kafka','flyway-mysql','spring-cloud-starter-openfeign'):
    if dep not in payment_pom: errors.append(f'SPEC 7.5 payment dependency missing: {dep}')
finance_pom=(root/'backend/charging-finance/pom.xml').read_text(encoding='utf-8')
for dep in ('spring-boot-starter-jdbc','spring-kafka','flyway-mysql'):
    if dep not in finance_pom: errors.append(f'SPEC 7.5 finance dependency missing: {dep}')
gateway75=(root/'backend/charging-gateway/src/main/resources/application.yml').read_text(encoding='utf-8')
if 'lb://charging-payment' not in gateway75: errors.append('SPEC 7.5 payment gateway route missing')
if '一人 + AI' not in (root/'docs/13-project-management/roadmap.md').read_text(encoding='utf-8'): errors.append('SPEC 7.5 one-person+AI roadmap baseline missing')
payment_service=(root/'backend/charging-payment/src/main/java/com/example/evcharging/payment/application/PaymentApplicationService.java').read_text(encoding='utf-8')
if 'TransactionTemplate' not in payment_service or 'payment_active_order' not in payment_service: errors.append('SPEC 7.5 payment create transaction boundary/active-order guard missing')
if not (root/'backend/charging-payment/src/main/java/com/example/evcharging/payment/config/PaymentFeignConfiguration.java').exists(): errors.append('SPEC 7.5 payment Feign tenant propagation missing')
if not (root/'backend/charging-core/src/main/java/com/example/evcharging/core/config/CoreFeignConfiguration.java').exists(): errors.append('SPEC 7.5 core Feign tenant propagation missing')
for rel in ('admin-web/src/pages/PaymentPage.tsx','admin-web/src/pages/FinancePage.tsx','backend/charging-finance/src/main/java/com/example/evcharging/finance/ledger/LedgerAdminController.java'):
    if not (root/rel).exists(): errors.append(f'SPEC 7.5 admin payment/finance UI missing: {rel}')



# SPEC 7.7 finance-hardening guards.
required_77 = [
    'backend/charging-finance/src/main/resources/db/migration/V1.2.0__finance_hardening.sql',
    'backend/charging-finance/src/main/java/com/example/evcharging/finance/adjustment/FinanceAdjustmentService.java',
    'backend/charging-finance/src/main/java/com/example/evcharging/finance/reconciliation/FinanceT1ReconciliationScheduler.java',
    'backend/charging-finance/src/main/java/com/example/evcharging/finance/reconciliation/ChannelBillRawImportService.java',
    'backend/charging-finance/src/main/java/com/example/evcharging/finance/settlement/SettlementApplicationService.java',
    'backend/charging-finance/src/main/java/com/example/evcharging/finance/ledger/LedgerPostingService.java',
    'backend/charging-finance/src/main/java/com/example/evcharging/finance/invoice/InvoiceApplicationService.java',
    'admin-web/src/pages/AdjustmentPage.tsx',
    'admin-web/src/pages/InvoicePage.tsx',
]
for rel in required_77:
    if not (root / rel).exists(): errors.append(f'SPEC 7.7 required file missing: {rel}')
finance_migration=(root/'backend/charging-finance/src/main/resources/db/migration/V1.2.0__finance_hardening.sql').read_text(encoding='utf-8')
for token in ('finance_adjustment_order','finance_reconciliation_schedule','finance_channel_bill_archive','finance_invoice_request','finance_invoice_red_flush','finance_invoice_active'):
    if token not in finance_migration: errors.append(f'SPEC 7.7 finance migration missing: {token}')
settlement77=(root/'backend/charging-finance/src/main/java/com/example/evcharging/finance/settlement/SettlementApplicationService.java').read_text(encoding='utf-8')
for token in ('PENDING_APPROVAL','maker-checker','ledger.post','ALLOCATED','MAX_SOURCES_PER_BATCH'):
    if token not in settlement77: errors.append(f'SPEC 7.7 settlement approval hardening missing: {token}')
recon77=(root/'backend/charging-finance/src/main/java/com/example/evcharging/finance/reconciliation/ReconciliationApplicationService.java').read_text(encoding='utf-8')
for token in ("adjustment_type='PAYMENT_AMOUNT'","adjustment_type='REFUND_AMOUNT'",'local_adjustment_fen'):
    if token not in recon77: errors.append(f'SPEC 7.7 reconciliation adjustment support missing: {token}')
ctx77=(root/'backend/charging-framework/src/main/java/com/example/evcharging/framework/context/RequestContext.java').read_text(encoding='utf-8')
if 'currentUserId' not in ctx77 or 'requireUserId' not in ctx77: errors.append('SPEC 7.7 maker-checker user context missing')
finance_app=(root/'backend/charging-finance/src/main/java/com/example/evcharging/finance/ChargingFinanceApplication.java').read_text(encoding='utf-8')
if '@EnableScheduling' not in finance_app: errors.append('SPEC 7.7 T+1 scheduling not enabled')



# SPEC 7.8 operation vertical-slice guards.
required_78 = [
    'backend/charging-operation/src/main/resources/db/migration/V1.0.0__operation_vertical_slice.sql',
    'backend/charging-operation/src/main/resources/processes/maintenance-work-order.bpmn20.xml',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/alarm/AlarmEventConsumer.java',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/workorder/WorkOrderWorkflowService.java',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/sla/SlaBreachScanner.java',
    'backend/charging-framework/src/main/java/com/example/evcharging/framework/contract/DeviceAlarmEvent.java',
    'admin-web/src/pages/AlarmPage.tsx',
    'admin-web/src/pages/MaintenancePage.tsx',
    'docs/13-project-management/progress-7.8.md',
]
for rel in required_78:
    if not (root / rel).exists(): errors.append(f'SPEC 7.8 required file missing: {rel}')
operation_pom=(root/'backend/charging-operation/pom.xml').read_text(encoding='utf-8')
if 'flowable-spring-boot-starter-process' not in operation_pom:
    errors.append('SPEC 7.8 Flowable process starter missing')
operation_migration=(root/'backend/charging-operation/src/main/resources/db/migration/V1.0.0__operation_vertical_slice.sql').read_text(encoding='utf-8')
for token in ('operation_active_alarm','operation_alarm_occurrence','operation_work_order','operation_sla_breach'):
    if token not in operation_migration: errors.append(f'SPEC 7.8 operation migration missing: {token}')
iot78=(root/'backend/charging-iot/src/main/java/com/example/evcharging/iot/gateway/NettyDeviceServer.java').read_text(encoding='utf-8')
for token in ('ALARM_RECOVERED','ev.device.alarm.v1','DeviceAlarmEvent'):
    if token not in iot78: errors.append(f'SPEC 7.8 IoT alarm bridge missing: {token}')
sim78=(root/'device-simulator/src/main/java/com/example/evcharging/simulator/DeviceSimulator.java').read_text(encoding='utf-8')
if 'SIM_ALARM_AFTER_SECONDS' not in sim78 or 'ALARM_RECOVERED' not in sim78:
    errors.append('SPEC 7.8 simulator alarm injection missing')
gateway78=(root/'backend/charging-gateway/src/main/resources/application.yml').read_text(encoding='utf-8')
if 'lb://charging-operation' not in gateway78:
    errors.append('SPEC 7.8 operation gateway route missing')
workflow78=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/workorder/WorkOrderWorkflowService.java').read_text(encoding='utf-8')
if 'repair assignee cannot verify own work' not in workflow78:
    errors.append('SPEC 7.8 independent maintenance verification guard missing')


api_response=(root/'backend/charging-framework/src/main/java/com/example/evcharging/framework/api/ApiResponse.java').read_text(encoding='utf-8')
if 'ApiResponse<T> ok(' not in api_response:
    errors.append('Shared ApiResponse backward-compatible ok(...) alias missing')


if sys.platform != 'win32' and not ((root/'backend'/'mvnw').stat().st_mode & 0o111):
    errors.append('mvnw is not executable')


sla_scanner=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/sla/SlaBreachScanner.java').read_text(encoding='utf-8')
if 'SlaBreachWorker' not in sla_scanner or 'worker.scanOne' not in sla_scanner:
    errors.append('SPEC 7.8: SLA scanner must delegate to transactional worker')
sla_worker=root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/sla/SlaBreachWorker.java'
if not sla_worker.exists() or 'Propagation.REQUIRES_NEW' not in sla_worker.read_text(encoding='utf-8'):
    errors.append('SPEC 7.8: SLA breach worker must use an independent transaction')


# SPEC 7.9 operation-hardening guards.
required_79 = [
    'backend/charging-framework/src/main/java/com/example/evcharging/framework/contract/DeviceLifecycleEvent.java',
    'backend/charging-iot/src/main/java/com/example/evcharging/iot/lifecycle/HeartbeatDeadlineMember.java',
    'backend/charging-iot/src/main/java/com/example/evcharging/iot/lifecycle/DeviceOfflineDetector.java',
    'backend/charging-operation/src/main/resources/db/migration/V1.1.0__operation_hardening.sql',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/notification/NotificationDispatchWorker.java',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/inspection/InspectionPlanGenerationWorker.java',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/spare/SpareStockService.java',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/attachment/WorkOrderAttachmentService.java',
    'backend/charging-operation/src/main/java/com/example/evcharging/operation/technician/TechnicianOperationController.java',
    'technician-app/pages.json',
    'admin-web/src/pages/InspectionPage.tsx',
    'admin-web/src/pages/SparePartsPage.tsx',
    'admin-web/src/pages/NotificationPage.tsx',
    'docs/13-project-management/progress-7.9.md',
]
for rel in required_79:
    if not (root/rel).exists(): errors.append(f'SPEC 7.9 required file missing: {rel}')

migration79=(root/'backend/charging-operation/src/main/resources/db/migration/V1.1.0__operation_hardening.sql').read_text(encoding='utf-8')
for token in ('operation_notification_task','operation_inspection_plan','operation_inspection_task',
              'operation_spare_stock','operation_spare_stock_transaction','operation_work_order_attachment'):
    if token not in migration79: errors.append(f'SPEC 7.9 operation migration missing: {token}')
if 'uk_spare_stock_tx_request' not in migration79:
    errors.append('SPEC 7.9 spare movement requestId uniqueness missing')
if 'CHECK (available_qty >= 0)' not in migration79:
    errors.append('SPEC 7.9 spare stock DB non-negative guard missing')

offline79=(root/'backend/charging-iot/src/main/java/com/example/evcharging/iot/lifecycle/DeviceOfflineDetector.java').read_text(encoding='utf-8')
for token in ('RELEASE_IF_MATCH','device-offline:','connectionToken','removed == 0L'):
    if token not in offline79: errors.append(f'SPEC 7.9 offline race hardening missing: {token}')

netty79=(root/'backend/charging-iot/src/main/java/com/example/evcharging/iot/gateway/NettyDeviceServer.java').read_text(encoding='utf-8')
for token in ('heartbeatDeadlines.touch','DeviceLifecycleEvent','ev.device.lifecycle.v1'):
    if token not in netty79: errors.append(f'SPEC 7.9 heartbeat/lifecycle bridge missing: {token}')

alarm_rules79=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/alarm/AlarmRuleDecisionService.java').read_text(encoding='utf-8')
if '"DEVICE_OFFLINE".equalsIgnoreCase(alarmCode)' not in alarm_rules79 or 'new Decision(false' not in alarm_rules79:
    errors.append('SPEC 7.9 DEVICE_OFFLINE must default to alarm-only')

notification79=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/notification/NotificationDispatchWorker.java').read_text(encoding='utf-8')
if 'Propagation.REQUIRES_NEW' not in notification79:
    errors.append('SPEC 7.9 notification worker must use independent transaction')

inspection79=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/inspection/InspectionPlanGenerationWorker.java').read_text(encoding='utf-8')
if 'Propagation.REQUIRES_NEW' not in inspection79 or 'generated<31' not in inspection79:
    errors.append('SPEC 7.9 inspection generator transaction/catch-up guard missing')

spare79=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/spare/SpareStockService.java').read_text(encoding='utf-8')
for token in ('available_qty>=?','requestId','insufficient spare-part stock'):
    if token not in spare79: errors.append(f'SPEC 7.9 spare stock invariant missing: {token}')

attachment79=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/attachment/LocalWorkOrderAttachmentStorage.java').read_text(encoding='utf-8')
for token in ('MAX_BYTES','SHA-256','path.startsWith(root)'):
    if token not in attachment79: errors.append(f'SPEC 7.9 attachment hardening missing: {token}')

tech_attach79=(root/'backend/charging-operation/src/main/java/com/example/evcharging/operation/technician/TechnicianAttachmentController.java').read_text(encoding='utf-8')
if 'assignee_user_id=?' not in tech_attach79:
    errors.append('SPEC 7.9 technician attachment assignment guard missing')

gateway79=(root/'backend/charging-gateway/src/main/resources/application.yml').read_text(encoding='utf-8')
if 'Path=/technician-api/v1/operation/**' not in gateway79:
    errors.append('SPEC 7.9 technician gateway route missing')

plan79=(root/'PROJECT_PLAN.md').read_text(encoding='utf-8')
if '50 周 / 250 人日' not in plan79 or 'S8B Operation Hardening' not in plan79:
    errors.append('SPEC 7.9 updated one-person+AI schedule baseline missing')


# SPEC 8.0 product-MVP guards.
required_80=[
 'backend/charging-framework/src/main/java/com/example/evcharging/framework/security/AccessPrincipal.java',
 'backend/charging-framework/src/main/java/com/example/evcharging/framework/security/AccessTokenCodec.java',
 'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/PermissionInterceptor.java',
 'backend/charging-system/src/main/resources/db/migration/V1.0.0__product_iam.sql',
 'backend/charging-system/src/main/java/com/example/evcharging/system/auth/AuthController.java',
 'backend/charging-asset/src/main/java/com/example/evcharging/asset/station/AppStationController.java',
 'backend/charging-core/src/main/java/com/example/evcharging/core/trade/AppOrderController.java',
 'admin-web/src/pages/LoginPage.tsx','admin-web/src/pages/DashboardPage.tsx','admin-web/src/pages/SystemPage.tsx',
 'merchant-web/src/pages/LoginPage.tsx','merchant-web/src/pages/DashboardPage.tsx','merchant-web/src/pages/OrderPage.tsx',
 'user-app/services/http.js','user-app/pages/login/index.vue','user-app/pages/charging/index.vue',
 'technician-app/services/http.js','docs/13-project-management/progress-8.0.md',
]
for rel in required_80:
    if not (root/rel).exists(): errors.append(f'SPEC 8.0 required file missing: {rel}')

ctx80=(root/'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/RequestContextFilter.java').read_text(encoding='utf-8')
for token in ('surfaceRoleAllowed','X-Service-Key','X-Internal-Tenant-Id','DEV_TENANT_HEADER_ENABLED:false','MERCHANT_STATION'):
    if token not in ctx80: errors.append(f'SPEC 8.0 auth boundary missing: {token}')

bootstrap80=(root/'backend/charging-system/src/main/java/com/example/evcharging/system/bootstrap/ProductIamBootstrap.java').read_text(encoding='utf-8')
if 'ConditionalOnProperty' not in bootstrap80 or 'bootstrap-demo-users' not in bootstrap80:
    errors.append('SPEC 8.0 demo IAM bootstrap must be explicit opt-in')

for surface in ('admin-web','merchant-web','user-app','technician-app'):
    for p in (root/surface).rglob('*'):
        if p.is_file() and p.suffix in ('.ts','.tsx','.js','.vue'):
            text=p.read_text(encoding='utf-8')
            if 'X-Tenant-Id' in text or 'X-User-Id' in text:
                errors.append(f'SPEC 8.0 product frontend identity spoofing remains: {p.relative_to(root)}')

core_dash=(root/'backend/charging-core/src/main/java/com/example/evcharging/core/trade/CoreDashboardController.java').read_text(encoding='utf-8')
merchant_core=(root/'backend/charging-core/src/main/java/com/example/evcharging/core/trade/MerchantCoreController.java').read_text(encoding='utf-8')
if 'status=30' not in core_dash or 'status=30' not in merchant_core:
    errors.append('SPEC 8.0 dashboard must use ChargingSessionStatus.CHARGING code 30')

# SPEC 8.1 supersedes the 8.0 fail-closed fallback by introducing authoritative local station projections.
projection_files = {
 'backend/charging-payment/src/main/java/com/example/evcharging/payment/application/MerchantPaymentController.java':'station_id',
 'backend/charging-finance/src/main/java/com/example/evcharging/finance/settlement/MerchantFinanceController.java':'s.station_id',
 'backend/charging-operation/src/main/java/com/example/evcharging/operation/MerchantOperationController.java':'station_id',
}
for rel,token in projection_files.items():
    text=(root/rel).read_text(encoding='utf-8')
    if token not in text or 'DataScopeType.STATION' not in text:
        errors.append(f'SPEC 8.1 merchant station projection/filter missing: {rel}')

gateway80=(root/'backend/charging-gateway/src/main/resources/application.yml').read_text(encoding='utf-8')
for token in ('/auth-api/**','/app-api/v1/orders/**','/merchant-api/v1/core/**','/merchant-api/v1/finance/**'):
    if token not in gateway80: errors.append(f'SPEC 8.0 gateway route missing: {token}')

if '50 周 / 250 人日' not in (root/'PROJECT_PLAN.md').read_text(encoding='utf-8'):
    errors.append('SPEC 8.0 project schedule baseline missing')


# SPEC 8.1 product-hardening guards.
required_81=[
 'backend/charging-framework/src/main/java/com/example/evcharging/framework/security/VerifiedAccessToken.java',
 'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/TokenRevocationChecker.java',
 'backend/charging-system/src/main/resources/db/migration/V1.1.0__auth_rbac_hardening.sql',
 'backend/charging-system/src/main/java/com/example/evcharging/system/auth/AuthSessionRevocationService.java',
 'backend/charging-system/src/main/java/com/example/evcharging/system/admin/SystemAdminService.java',
 'backend/charging-payment/src/main/resources/db/migration/V1.1.0__merchant_station_projection.sql',
 'backend/charging-finance/src/main/resources/db/migration/V1.3.0__merchant_station_projection.sql',
 'backend/charging-operation/src/main/resources/db/migration/V1.2.0__merchant_station_projection.sql',
 'backend/charging-payment/src/main/java/com/example/evcharging/payment/infrastructure/PaymentStationProjectionRepairJob.java',
 'backend/charging-finance/src/main/java/com/example/evcharging/finance/reconciliation/FinanceStationProjectionRepairJob.java',
 'backend/charging-operation/src/main/java/com/example/evcharging/operation/asset/OperationStationProjectionRepairJob.java',
 'admin-web/src/pages/ProfilePage.tsx',
]
for rel in required_81:
    if not (root/rel).exists(): errors.append(f'SPEC 8.1 required file missing: {rel}')

codec81=(root/'backend/charging-framework/src/main/java/com/example/evcharging/framework/security/AccessTokenCodec.java').read_text(encoding='utf-8')
for token in ('"jti"','"sid"','verifyToken','IssuedAccessToken'):
    if token not in codec81: errors.append(f'SPEC 8.1 access-token session claim missing: {token}')

filter81=(root/'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/RequestContextFilter.java').read_text(encoding='utf-8')
for token in ('revocations.requireActive','path.startsWith("/internal-api/")','principal.hasRole("SERVICE")'):
    if token not in filter81: errors.append(f'SPEC 8.1 security filter hardening missing: {token}')

auth81=(root/'backend/charging-system/src/main/java/com/example/evcharging/system/auth/AuthService.java').read_text(encoding='utf-8')
for token in ('refresh_token_hash','TOKEN_REFRESH','refresh token already rotated','failed_login_count'):
    if token not in auth81: errors.append(f'SPEC 8.1 auth hardening missing: {token}')

rbac81=(root/'backend/charging-system/src/main/java/com/example/evcharging/system/admin/SystemAdminService.java').read_text(encoding='utf-8')
for token in ('replaceUserRoles','replaceStationScope','resetPassword','revokeUserSessions','replacePermissionsInternal'):
    if token not in rbac81: errors.append(f'SPEC 8.1 RBAC admin hardening missing: {token}')

for rel in (
 'backend/charging-payment/src/main/resources/db/migration/V1.1.0__merchant_station_projection.sql',
 'backend/charging-finance/src/main/resources/db/migration/V1.3.0__merchant_station_projection.sql',
 'backend/charging-operation/src/main/resources/db/migration/V1.2.0__merchant_station_projection.sql'):
    if 'station_id' not in (root/rel).read_text(encoding='utf-8'):
        errors.append(f'SPEC 8.1 station projection migration missing: {rel}')

ws81=(root/'backend/charging-core/src/main/java/com/example/evcharging/core/charging/realtime/ChargingRealtimeTicketService.java').read_text(encoding='utf-8')
if 'user_id=?' not in ws81 or 'RequestContext.requireUserId' not in ws81:
    errors.append('SPEC 8.1 realtime ticket must be bound to session owner')
driver81=(root/'user-app/pages/charging/index.vue').read_text(encoding='utf-8')
for token in ('realtime-ticket','uni.connectSocket','startFallback'):
    if token not in driver81: errors.append(f'SPEC 8.1 driver realtime hardening missing: {token}')

for surface in ('admin-web','merchant-web','user-app','technician-app'):
    for p in (root/surface).rglob('*'):
        if p.is_file() and p.suffix in ('.ts','.tsx','.js','.vue'):
            text=p.read_text(encoding='utf-8')
            if 'X-Tenant-Id' in text or 'X-User-Id' in text:
                errors.append(f'SPEC 8.1 frontend identity spoofing regression: {p.relative_to(root)}')


login_attempt81=(root/'backend/charging-system/src/main/java/com/example/evcharging/system/auth/LoginAttemptService.java').read_text(encoding='utf-8')
if 'Propagation.REQUIRES_NEW' not in login_attempt81 or 'failed_login_count' not in login_attempt81:
    errors.append('SPEC 8.1 login failure counter must commit independently')
auth_login81=(root/'backend/charging-system/src/main/java/com/example/evcharging/system/auth/AuthService.java').read_text(encoding='utf-8')
if '@Transactional\\n    public LoginResult login' in auth_login81:
    errors.append('SPEC 8.1 login must not rollback failed-attempt accounting')


secret_guard81=(root/'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/SecurityConfigurationGuard.java').read_text(encoding='utf-8')
for token in ('APP_ENV','development identity headers must be disabled','must be different'):
    if token not in secret_guard81: errors.append(f'SPEC 8.1 production secret guard missing: {token}')
rbac_full81=(root/'backend/charging-system/src/main/java/com/example/evcharging/system/admin/SystemAdminService.java').read_text(encoding='utf-8')
for token in ('createPermission','updatePermission','deletePermission'):
    if token not in rbac_full81: errors.append(f'SPEC 8.1 Permission CRUD missing: {token}')


# SPEC 8.2 openapi-regulatory guards.
required_82=[
 'backend/charging-open/pom.xml',
 'backend/charging-open/src/main/java/com/example/evcharging/open/ChargingOpenApplication.java',
 'backend/charging-open/src/main/resources/db/migration/V1.0.0__openapi_regulatory.sql',
 'backend/charging-open/src/main/java/com/example/evcharging/open/security/PartnerAuthenticationFilter.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/security/OpenApiSignature.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/security/SecretCipher.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/security/OutboundUrlPolicy.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/partner/PartnerChargingService.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/callback/PartnerCallbackTaskRepository.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/regulatory/RegulatoryProtocolAdapter.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/regulatory/GbT44130CanonicalAdapter.java',
 'admin-web/src/pages/OpenIntegrationPage.tsx',
 'docs/18-openapi/openapi-v1.yaml',
 'tests/e2e/openapi-regulatory/openapi-regulatory-matrix.md',
 'docs/13-project-management/progress-8.2.md',
]
for rel in required_82:
    if not (root/rel).exists(): errors.append(f'SPEC 8.2 required file missing: {rel}')

root_pom82=(root/'backend'/'pom.xml').read_text(encoding='utf-8')
if '<module>charging-open</module>' not in root_pom82:
    errors.append('SPEC 8.2 charging-open module missing from root reactor')

gateway82=(root/'backend/charging-gateway/src/main/resources/application.yml').read_text(encoding='utf-8')
if 'lb://charging-open' not in gateway82 or 'Path=/open-api/**,/admin-api/v1/open/**' not in gateway82:
    errors.append('SPEC 8.2 charging-open gateway route missing')

sig82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/security/OpenApiSignature.java').read_text(encoding='utf-8')
for token in ('HmacSHA256','canonicalQuery','sha256Hex','MessageDigest.isEqual'):
    if token not in sig82: errors.append(f'SPEC 8.2 OpenAPI signature invariant missing: {token}')

auth_filter82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/security/PartnerAuthenticationFilter.java').read_text(encoding='utf-8')
for token in ('X-App-Key','X-Timestamp','X-Nonce','X-Signature-Version','replayRate.requireFreshNonce','open_api_audit_log'):
    if token not in auth_filter82: errors.append(f'SPEC 8.2 partner auth filter missing: {token}')
if auth_filter82.find('constantTimeEquals') > auth_filter82.find('requireFreshNonce') or auth_filter82.find('constantTimeEquals') < 0:
    errors.append('SPEC 8.2 nonce must be consumed only after valid HMAC signature')

cipher82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/security/SecretCipher.java').read_text(encoding='utf-8')
for token in ('AES/GCM/NoPadding','GCMParameterSpec','key.length!=32'):
    if token not in cipher82: errors.append(f'SPEC 8.2 encrypted secret-at-rest invariant missing: {token}')

rate82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/security/PartnerReplayRateGuard.java').read_text(encoding='utf-8')
if 'setIfAbsent' not in rate82 or "redis.call('INCR'" not in rate82:
    errors.append('SPEC 8.2 Redis nonce/rate-limit atomic guards missing')

partner_charge82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/partner/PartnerChargingService.java').read_text(encoding='utf-8')
for token in ('assets.connector','PartnerScopeGuard.requireStation','partner:"+p.partnerId()'):
    if token not in partner_charge82: errors.append(f'SPEC 8.2 Partner command scope/idempotency missing: {token}')

member_charge82=(root/'backend/charging-core/src/main/java/com/example/evcharging/core/charging/application/ChargingApplicationService.java').read_text(encoding='utf-8')
if 'RequestContext.requireUserId()' not in member_charge82 or 'startTransaction.create(tenantId,1L' in member_charge82:
    errors.append('SPEC 8.2 native member charging identity bridge not fixed')

callback_worker82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/callback/PartnerCallbackWorker.java').read_text(encoding='utf-8')
callback_repo82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/callback/PartnerCallbackTaskRepository.java').read_text(encoding='utf-8')
if '@Transactional' in callback_worker82:
    errors.append('SPEC 8.2 callback HTTP must not hold DB transaction')
if 'Propagation.REQUIRES_NEW' not in callback_repo82 or "status='SENDING'" not in callback_repo82:
    errors.append('SPEC 8.2 callback claim/result transaction pattern missing')

reg_worker82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/regulatory/RegulatoryDispatchWorker.java').read_text(encoding='utf-8')
reg_repo82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/regulatory/RegulatoryTaskRepository.java').read_text(encoding='utf-8')
if '@Transactional' in reg_worker82:
    errors.append('SPEC 8.2 regulatory HTTP must not hold DB transaction')
if 'Propagation.REQUIRES_NEW' not in reg_repo82 or "status='SENDING'" not in reg_repo82:
    errors.append('SPEC 8.2 regulatory claim/result transaction pattern missing')

gbt82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/regulatory/GbT44130CanonicalAdapter.java').read_text(encoding='utf-8')
for token in ('GB/T 44130.2-2025','GB/T 44130.3-2025','canonical-adapter-not-platform-certified'):
    if token not in gbt82: errors.append(f'SPEC 8.2 GB/T canonical adapter boundary missing: {token}')

outbound82=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/security/OutboundUrlPolicy.java').read_text(encoding='utf-8')
for token in ('OPENAPI_ALLOWED_OUTBOUND_HOSTS','production outbound URL must use https','outbound host is not allowlisted'):
    if token not in outbound82: errors.append(f'SPEC 8.2 outbound SSRF policy missing: {token}')

schema82=(root/'backend/charging-open/src/main/resources/db/migration/V1.0.0__openapi_regulatory.sql').read_text(encoding='utf-8')
for token in ('open_partner_app','open_partner_scope','open_partner_station_scope','open_api_audit_log',
              'open_partner_callback_task','open_regulatory_platform','open_regulatory_report_task'):
    if token not in schema82: errors.append(f'SPEC 8.2 OpenAPI schema missing: {token}')
if 'secret_ciphertext' not in schema82 or 'request_body_sha256' not in schema82:
    errors.append('SPEC 8.2 secret/audit schema invariant missing')

plan82=(root/'PROJECT_PLAN.md').read_text(encoding='utf-8')
if 'W38-W39' not in plan82 or '50 周 / 250 人日' not in plan82:
    errors.append('SPEC 8.2 schedule baseline missing')


# SPEC 8.3 security-performance-chaos guards.
required_83=[
 'backend/charging-gateway/src/main/java/com/example/evcharging/gateway/resilience/GatewaySentinelRuleBootstrap.java',
 'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/resilience/SentinelHotPathRuleBootstrap.java',
 'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/resilience/BoundedExecutorConfiguration.java',
 'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/resilience/BoundedExecutorMetrics.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/admin/OpenSecretRewrapService.java',
 'deploy/observability/prometheus.yml',
 'deploy/observability/alert-rules.yml',
 'tests/performance/k6/public-station-read.js',
 'tests/performance/k6/openapi-station-read.js',
 'scripts/chaos/chaos.sh',
 'tests/chaos/chaos-matrix.md',
 'docs/19-hardening/slo-and-capacity.md',
 'docs/13-project-management/progress-8.3.md',
]
for rel in required_83:
    if not (root/rel).exists(): errors.append(f'SPEC 8.3 required file missing: {rel}')

webmvc_pom83=(root/'backend/charging-framework-webmvc/pom.xml').read_text(encoding='utf-8')
for token in ('spring-cloud-starter-alibaba-sentinel','micrometer-registry-prometheus'):
    if token not in webmvc_pom83: errors.append(f'SPEC 8.3 MVC resilience dependency missing: {token}')
gateway_pom83=(root/'backend/charging-gateway/pom.xml').read_text(encoding='utf-8')
for token in ('spring-cloud-starter-alibaba-sentinel','spring-cloud-alibaba-sentinel-gateway','micrometer-registry-prometheus'):
    if token not in gateway_pom83: errors.append(f'SPEC 8.3 Gateway resilience dependency missing: {token}')

hot83=(root/'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/resilience/SentinelHotPathRuleBootstrap.java').read_text(encoding='utf-8')
for token in ('charging.start','charging.stop','payment.create','payment.refund','DEGRADE_GRADE_EXCEPTION_RATIO'):
    if token not in hot83: errors.append(f'SPEC 8.3 Sentinel hot-path rule missing: {token}')

for rel in (
 'backend/charging-core/src/main/java/com/example/evcharging/core/charging/application/ChargingApplicationService.java',
 'backend/charging-payment/src/main/java/com/example/evcharging/payment/application/PaymentApplicationService.java'):
    if '@SentinelResource' not in (root/rel).read_text(encoding='utf-8'):
        errors.append(f'SPEC 8.3 Sentinel resource annotation missing: {rel}')

executor83=(root/'backend/charging-framework-webmvc/src/main/java/com/example/evcharging/framework/webmvc/resilience/BoundedExecutorConfiguration.java').read_text(encoding='utf-8')
if 'AbortPolicy' not in executor83 or 'setQueueCapacity' not in executor83:
    errors.append('SPEC 8.3 bounded executor must reject instead of using an unbounded queue')

for rel in (
 'backend/charging-open/src/main/java/com/example/evcharging/open/callback/PartnerCallbackScanner.java',
 'backend/charging-open/src/main/java/com/example/evcharging/open/regulatory/RegulatoryDispatchScanner.java'):
    text=(root/rel).read_text(encoding='utf-8')
    if 'TaskRejectedException' not in text or 'executor.execute' not in text:
        errors.append(f'SPEC 8.3 external dispatch backpressure missing: {rel}')

cipher83=(root/'backend/charging-open/src/main/java/com/example/evcharging/open/security/SecretCipher.java').read_text(encoding='utf-8')
for token in ('v2:','activeKeyId','OPENAPI_PREVIOUS_MASTER_KEYS','rewrap'):
    if token not in cipher83: errors.append(f'SPEC 8.3 OpenAPI key rotation missing: {token}')

for module in ('charging-core','charging-finance','charging-open','charging-operation','charging-payment'):
    y=(root/f'backend/{module}/src/main/resources/application.yml').read_text(encoding='utf-8')
    for token in ('spring.cloud.openfeign.client.config.default.connectTimeout','spring.cloud.openfeign.client.config.default.readTimeout'):
        if token not in y: errors.append(f'SPEC 8.3 Feign timeout missing in {module}: {token}')

for yml in root.glob('backend/*/src/main/resources/application.yml'):
    text=yml.read_text(encoding='utf-8')
    for token in ('management.prometheus.metrics.export.enabled','management.metrics.distribution.percentiles-histogram.http.server.requests'):
        if token not in text: errors.append(f'SPEC 8.3 observability setting missing in {yml.parent.parent.parent.parent.name}: {token}')

if '50 周 / 250 人日' not in (root/'PROJECT_PLAN.md').read_text(encoding='utf-8'):
    errors.append('SPEC 8.3 schedule baseline missing')

print('STATIC_VALIDATION=' + ('PASS' if not errors else 'FAIL'))
for note in notes:
    print('NOTE:', note)
for e in errors:
    print('ERROR:', e)
sys.exit(1 if errors else 0)
