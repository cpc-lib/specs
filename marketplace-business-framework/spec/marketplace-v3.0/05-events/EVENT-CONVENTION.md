# Event Convention
Routing key: `{domain}.{aggregate}.{action}.v{major}`
Event IDs are globally unique.
Aggregate version is recommended for ordered facts.
All transactional producers use local Outbox.
All transactional consumers use Inbox.
Large binary payloads never go through MQ.
