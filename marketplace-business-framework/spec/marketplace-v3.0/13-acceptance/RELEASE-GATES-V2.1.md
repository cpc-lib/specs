# V2.1 Release Gates

## Contract
- every mutating core OpenAPI operation has explicit request schema
- every path parameter declared
- operationId globally unique
- all Event JSON Schemas parse

## Money
- Trade amount conservation tests pass
- discount funding allocation conserves each source
- 0.01 residual deterministic
- payment allocation equals successful transaction amount
- concurrent refunds cannot exceed refundable
- settlement calculation fully traceable

## Inventory
- 1000 concurrent requests / stock 100 => <=100 successful
- Redis failure does not produce oversell in normal mode

## Isolation
- seller Merchant A cannot read/update B order/product/settlement

## Failure Recovery
- payment/refund/payout UNKNOWN query workflow tested
- RabbitMQ outage leaves Outbox recoverable
- ES outage does not block transaction core
