# Operation Domain — SPEC 7.8

## Boundary

Operation owns:

- Alarm rules
- Active alarm aggregation
- Alarm occurrence timeline
- Maintenance work orders
- Flowable workflow correlation
- SLA policies and breach facts

Operation does not own:

- Device protocol sessions
- ChargingSession state
- Payment / finance facts

## Alarm invariant

A physical ongoing fault is identified by:

`deviceId + connectorNo + alarmCode`

and represented by one `ACTIVE` Alarm.

Repeated `RAISED` events:

- increment occurrence count
- update last occurrence
- retain the highest observed severity
- do not create duplicate active work orders

Recovery removes the active-alarm lock and closes the Alarm fact as `RECOVERED`.

## Work order invariant

The same Alarm can have at most one work order in the vertical slice.

Work order lifecycle:

`PENDING_ASSIGNMENT → ASSIGNED → IN_PROGRESS → WAIT_VERIFY → CLOSED`

Failed verification loops back to `IN_PROGRESS`.

The repair assignee cannot verify their own work.

## Safety boundary

SPEC 7.8 does **not** automatically execute `STOP_CHARGING`, OTA, reboot, refund, or other high-risk actions based solely on an alarm rule.

Alarm automation may create a maintenance work order. Safety-critical remote commands require a separately approved command policy and audit flow.
