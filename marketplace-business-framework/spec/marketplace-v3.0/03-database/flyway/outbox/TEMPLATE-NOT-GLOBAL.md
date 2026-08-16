# Outbox / Inbox DDL is a TEMPLATE

`V1__integration.sql` must NOT be deployed as a central shared "outbox database".

For every transactional service that produces/consumes critical events, instantiate equivalent local tables:
- mq_outbox
- mq_inbox
- integration_task (where recovery is needed)

They live in the owning service database/schema and participate in the same local transaction as that service's aggregate.

Codegen may rename with service-local prefixes if desired, but semantics and unique constraints must remain.
