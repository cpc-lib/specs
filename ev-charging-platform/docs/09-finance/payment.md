# Payment SPEC 7.5

## Ownership

- Core：ChargeOrder 商业订单事实及支付状态投影。
- Payment：PaymentOrder、渠道流水、Callback、Refund。
- Finance：Ledger、Reconciliation、Settlement。

## Payment Success

Payment Callback → Payment 本地事务 → PaymentTransaction + Event Outbox → Kafka → Core Inbox 更新订单 → Finance Inbox 双式记账。

## Idempotency

- Create Payment：`tenant_id + request_id` 唯一。
- Callback：`tenant_id + callback_fingerprint` 唯一。
- Core Projection：`event_id` 唯一。
- Finance Ledger：`tenant_id + biz_event_id` 唯一。

## Refund

创建退款先占用 `refund_reserved_fen`，成功后转换为 `refunded_amount_fen`，避免并发超退。
