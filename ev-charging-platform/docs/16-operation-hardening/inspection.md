# Inspection Management

## Plan

An inspection plan references:

- stationId
- cycleDays
- assigneeUserId
- checklist JSON
- nextGenerateDate

## Generation

`planId + scheduledDate` is unique.

The generator uses Scanner → `REQUIRES_NEW` Worker so one broken plan cannot roll back all other plan generation.

A maximum of 31 missed cycles is generated in one run to avoid catch-up storms.

## Task

Lifecycle:

`PENDING → IN_PROGRESS → COMPLETED`

`overdue` is an independent flag so overdue tasks can still retain their real workflow state.

Only the assigned technician may start/complete the task.
