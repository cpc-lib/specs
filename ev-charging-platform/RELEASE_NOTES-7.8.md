# Release Notes — SPEC 7.8

Status: `foundation-rc-operation-vertical-slice`

## Added

- `DeviceAlarmEvent`
- IoT ALARM / ALARM_RECOVERED protocol bridge
- `ev.device.alarm.v1`
- active alarm database deduplication
- alarm occurrence history
- alarm rules
- SLA policies / breach facts
- maintenance work orders
- Flowable maintenance BPMN
- independent repair verification
- Alarm Admin page
- Maintenance Admin page
- Simulator alarm injection
- Operation JUnit + pure Java harness

## Safety

Alarm automation creates work orders only. It does not directly invoke remote stop, reboot, OTA, refund or financial operations.

## Runtime gate

Flowable/MySQL/Kafka/Spring Boot integration remains a runtime release gate until Maven/Docker dependencies can execute in the verification environment.

## Cross-slice fixes

- added backward-compatible `ApiResponse.ok(...)`
- restored executable `mvnw` / shell-script permissions
- alarm severity escalation re-evaluates auto-work-order policy
- explicitly disabled alarm rules suppress auto work-order fallback
