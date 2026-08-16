# Consistency Strategy

1. Local DB transaction first.
2. Outbox + MQ + Inbox for cross-service facts.
3. Saga for long transactions.
4. TCC only for short critical distributed reservations if local ownership cannot solve.
5. Avoid global XA/Seata AT across marketplace core.
6. Read models are eventually consistent.
7. Payment/refund/payout UNKNOWN must query provider before retry.
