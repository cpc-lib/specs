# Simulator Alarm Scenario

Example:

```bash
SIM_ALARM_AFTER_SECONDS=10 SIM_ALARM_RECOVER_AFTER_SECONDS=40 SIM_ALARM_CODE=CONNECTOR_OVER_TEMPERATURE SIM_ALARM_SEVERITY=CRITICAL SIM_ALARM_VALUE=85 SIM_ALARM_UNIT=C java -cp target com.example.evcharging.simulator.DeviceSimulator 1 CP000001
```

Expected chain:

```text
Simulator
→ authenticated TCP ALARM
→ IoT Gateway
→ ev.device.alarm.v1
→ Operation Inbox
→ Active Alarm
→ WorkOrder
→ Flowable Dispatch
→ Repair
→ independent Verify
```
