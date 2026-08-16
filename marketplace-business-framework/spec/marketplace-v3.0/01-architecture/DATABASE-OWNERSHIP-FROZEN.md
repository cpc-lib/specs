# Database Ownership — V3.0 Frozen

## Rule
A service owns its write model and migrations.
No service imports another service's Mapper/Repository to update its tables.

Cross-context interaction:
HTTP/internal command
or event
or Saga step.

## Query
Cross-domain admin/buyer views use:
- API composition
- CQRS projections
- search index
- dedicated read models.

Do not implement cross-service SQL joins as a convenience shortcut.

## Outbox
Outbox/Inbox is local to each service DB. The repository file under `flyway/outbox` is only a template.

## Sharding
Binding-table family requires the routing key physically present in every hot child table.
V3.0 migrations add missing route keys so ShardingSphere does not broadcast child-table writes/queries.
