# Device Offline Detection — SPEC 7.9

## Ownership

IoT owns connectivity truth.

Operation must not scan Redis and infer whether a charger is online.

## Lease model

Every authenticated device connection owns a unique:

`gatewayId | connectionToken`

The IoT gateway refreshes:

- `ev:{tenant}:device:online:{deviceId}`
- `ev:{tenant}:device:route:{deviceId}`
- `ev:iot:heartbeat:deadlines` ZSET

Heartbeat timeout is 90 seconds in the current development baseline.

## Race protection

An expired ZSET member is checked against the current lease.

The offline detector uses Lua compare-and-delete. If a new connection wins between GET and DELETE, the new lease is not removed and OFFLINE publication is suppressed.

Offline event IDs are deterministic per connection token:

`device-offline:{tenant}:{device}:{connectionToken}`

Therefore repeated scanner publication remains consumer-idempotent.

## Operation behavior

`OFFLINE` lifecycle → virtual `DEVICE_OFFLINE` MAJOR alarm.

`ONLINE` lifecycle → recover the active `DEVICE_OFFLINE` alarm.

A transient TCP disconnect does not immediately become an operational alarm; the heartbeat timeout grace window applies.
