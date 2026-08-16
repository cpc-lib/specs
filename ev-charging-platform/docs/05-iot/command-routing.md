# Device Command Routing - SPEC 7.4

## Route lease

Each authenticated TCP connection owns a short Redis lease:

```text
ev:{tenant}:device:online:{deviceId} = {gatewayId}|{connectionToken}
ev:{tenant}:device:route:{deviceId}  = {gatewayId}|{connectionToken}
TTL = 90s
```

Heartbeat refreshes both keys.

A disconnect deletes the keys only if their current value still equals the disconnecting connection's lease.
This compare-and-delete rule prevents an old Gateway connection from deleting a newer connection after cross-node reconnect.

## Command dispatch

```text
DeviceCommandOutbox
  -> Redis route lookup
  -> routing key gateway.{gatewayId}
  -> queue ev.device.command.gateway-{gatewayId}
  -> IoT Gateway local Channel Registry
  -> Netty Channel
```

Every IoT instance must have a stable unique `IOT_GATEWAY_ID` for the lifetime of the process/pod.
Kubernetes deployment should inject the pod identity (or another unique gateway identity) as this value.

## Reliability

- DB Outbox claim prevents concurrent Core publishers from taking the same NEW command.
- Rabbit publisher confirm + mandatory return prevents unroutable messages from being marked PUBLISHED.
- Queue messages that cannot be consumed are dead-lettered rather than requeued forever.
- Business recovery still queries device transaction state when command ACK/result is uncertain.
