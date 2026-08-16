# SPEC 7.8 Progress — Operation Vertical Slice

Status: `foundation-rc-operation-vertical-slice`

## Completed in this slice

- authenticated IoT alarm message path
- alarm raised/recovered Kafka event
- database-level active-alarm deduplication
- occurrence aggregation
- tenant alarm rule
- severity-based work-order decision
- SLA policy and deterministic due times
- Flowable maintenance process
- dispatch / repair / independent verification
- failed-verification loop
- response/resolution SLA breach scanner
- Alarm Admin UI
- Maintenance WorkOrder Admin UI
- Simulator alarm injection
- operation pure-Java harness

## One developer + AI schedule

This slice belongs to **P8 / W24-W26 / 15 person-days**.

Estimated allocation:

| Work | Person-days |
|---|---:|
| Alarm contract + IoT bridge | 2 |
| Deduplication / rule engine | 2 |
| WorkOrder model + APIs | 2 |
| Flowable BPMN integration | 3 |
| SLA / escalation facts | 2 |
| Admin operation UI | 2 |
| Tests / failure scenarios / docs | 2 |
| **Total** | **15** |

AI accelerates schema/code/test drafting. Human time remains necessary for real-device alarm semantics, safety boundaries, workflow acceptance and on-call/SLA policy review.

## Remaining Operation work

- offline-device detector tied to authoritative heartbeat facts
- notification escalation (SMS/WeChat/App)
- inspection plans/tasks
- spare-parts inventory
- work-order attachments
- technician mobile UX
- real Flowable runtime E2E release gate
