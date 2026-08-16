# ADR-037 — Outbox/Inbox is Per-Service

Decision:
The generic Outbox/Inbox migration is a template instantiated inside each transactional service database.
There is no central shared Outbox DB transaction across services.
