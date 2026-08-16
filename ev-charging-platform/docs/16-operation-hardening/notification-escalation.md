# Notification Escalation

Flow:

```text
Alarm / SLA Breach
    ↓
Notification Policy
    ↓
Notification Task
    ↓
Scheduled Worker
    ↓
NotificationGateway
```

Channels currently modeled:

- APP
- SMS
- WECHAT

SPEC 7.9 includes a Mock adapter only.

Production adapters must be added behind `NotificationGateway`.

## Invariants

- Notification calls do not execute inside the alarm transaction.
- Tasks are persisted before dispatch.
- Retry state is explicit.
- Same trigger/business/policy is idempotent.
- CRITICAL incidents have a development fallback APP task for `ON_CALL` if no tenant policy exists.
