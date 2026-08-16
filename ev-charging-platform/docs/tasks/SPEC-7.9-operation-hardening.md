# TASK — SPEC 7.9 Operation Hardening

## Objective

Complete the operational hardening layer after SPEC 7.8.

## Scope

1. Device heartbeat deadline / offline lifecycle
2. Offline alarm recovery
3. Notification escalation
4. Inspection plan/task
5. Spare-parts inventory
6. Work-order attachments
7. Technician UniApp
8. Admin operation-hardening pages

## Hard invariants

- old heartbeat lease cannot delete a newer connection
- DEVICE_OFFLINE does not auto-create work order unless tenant explicitly configures it
- notification dispatch is outside alarm transaction
- inspection generation is idempotent by plan/date
- spare stock can never go negative
- spare movement is requestId-idempotent
- technician may upload attachment only for assigned work order
- production total schedule baseline is 50 weeks / 250 person-days

## Runtime exit gate

- live Redis lease race test
- Kafka lifecycle E2E
- Operation MySQL/Flyway migration
- live notification worker retry
- inspection scheduler E2E
- concurrent spare-stock consume
- multipart attachment E2E
- Technician UniApp build/run
