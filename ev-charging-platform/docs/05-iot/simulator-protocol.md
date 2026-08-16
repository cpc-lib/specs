# SPEC 7.4 Simulator Protocol

This is a cloud integration simulation protocol, **not** a replacement for GB/T or OCPP.

## Authentication

```text
AUTH|{tenantId}|{deviceId}|{secret}
AUTH_ACK|{tenantId}|{deviceId}
```

After AUTH, tenant/device identity is bound to the Netty Channel. Subsequent charging frames do not carry a trusted device identity.

## Heartbeat

```text
PING|{epochMillis}
PONG|{deviceId}
```

## Charging facts

```text
CHARGING_STARTED|{sessionNo}|{connectorNo}|{startMeterWh}|{startSoc}|{occurredAtEpochMillis}
TELEMETRY|{sessionNo}|{connectorNo}|{soc}|{powerW}|{meterWh}|{occurredAtEpochMillis}
CHARGING_STOPPED|{sessionNo}|{connectorNo}|{finalMeterWh}|{finalSoc}|{reason}|{occurredAtEpochMillis}
```

The device event timestamp is preserved so delayed/replayed traffic is billed against the correct local peak/flat/valley period.

## Cloud command

```text
COMMAND|{commandId}|{commandType}|{payloadJson}
```

Supported RC commands:

- `START_CHARGING`
- `STOP_CHARGING`
- `QUERY_TRANSACTION`

Device reply:

```text
COMMAND_ACK|{commandId}|SUCCESS|{commandType}
```

## Offline/reconnect behavior

The simulator keeps its transaction state outside the TCP socket. If the socket is lost:

1. the local meter continues increasing while `charging=true`;
2. the simulator reconnects automatically;
3. Cloud Recovery may issue `QUERY_TRANSACTION`;
4. the simulator replays the original `CHARGING_STARTED` fact or final `CHARGING_STOPPED` fact;
5. missing telemetry is reconstructed for billing via deterministic meter interpolation.

## Security

`dev-secret` is local-integration only. Production device identity must migrate to per-device credentials/certificates, timestamp/nonce replay protection, key rotation and protocol-specific security controls.
