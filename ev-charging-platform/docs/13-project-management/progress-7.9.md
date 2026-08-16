# SPEC 7.9 Progress — Operation Hardening

Status: `foundation-rc-operation-hardening`

## Completed

- heartbeat deadline registry
- lease-safe device offline detection
- deterministic lifecycle event ID
- ONLINE/OFFLINE → DEVICE_OFFLINE alarm bridge
- notification policy / task / retry worker
- inspection plan / scheduler / task / overdue
- spare-part catalog / stock / append-only movement
- atomic no-negative stock consumption
- work-order attachment storage abstraction
- local development attachment storage
- technician-assignment upload security
- independent Technician UniApp
- Admin inspection / spare-parts / notification pages

## Schedule impact

SPEC 7.9 adds real scope that was only named, not budgeted in sufficient detail, in the earlier P8 estimate.

The production baseline is revised from:

- 47 weeks / 235 person-days

to:

- **50 weeks / 250 person-days**

New phase:

- `S8B Operation Hardening` — W27-W29 / 15 person-days

The original four-week integration buffer remains intact at W47-W50.

## S8B one developer + AI estimate

| Work | Person-days |
|---|---:|
| Heartbeat/offline lifecycle hardening | 2 |
| Notification escalation | 2 |
| Inspection plan/task | 3 |
| Spare-parts inventory | 2.5 |
| Work-order attachments | 1.5 |
| Technician UniApp | 2.5 |
| Admin UI / tests / docs / review | 1.5 |
| **Total** | **15** |

AI helps with page scaffolding, SQL, DTOs and test cases. Human time remains required for real on-call policy, inventory process, field-technician UX, object-storage/security review and real-device connectivity behavior.
