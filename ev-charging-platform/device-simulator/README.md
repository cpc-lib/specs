# Device Simulator — SPEC 7.3

纯 JDK 21 状态化模拟器。

```bash
javac -d target src/main/java/com/example/evcharging/simulator/DeviceSimulator.java
java -cp target com.example.evcharging.simulator.DeviceSimulator 1 CP000001
```

行为：

- AUTH + heartbeat
- 收到 `START_CHARGING` 后发送 `CHARGING_STARTED`
- 每秒发送 Telemetry（SOC / power / meter）
- 收到 `STOP_CHARGING` 后发送 `CHARGING_STOPPED`
- 每个 Command 返回 `COMMAND_ACK`


## Alarm injection

```bash
SIM_ALARM_AFTER_SECONDS=10 SIM_ALARM_RECOVER_AFTER_SECONDS=40 SIM_ALARM_CODE=CONNECTOR_OVER_TEMPERATURE SIM_ALARM_SEVERITY=CRITICAL SIM_ALARM_VALUE=85 SIM_ALARM_UNIT=C java -cp target com.example.evcharging.simulator.DeviceSimulator 1 CP000001
```

The simulator emits `ALARM` and `ALARM_RECOVERED` through the same authenticated TCP session.
