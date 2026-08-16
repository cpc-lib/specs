# V2.0 Freeze Policy

V2.0 freezes the first complete codegen architecture baseline.

Breaking changes require ADR:
- Merchant/Shop boundary
- SPU/SKU/Offer model
- Trade/MerchantOrder/OrderItem model
- Inventory reservation correctness
- Payment fact model
- Refund quota
- Settlement model
- Sharding keys
- Outbox/Inbox
- Merchant data isolation

Ordinary implementation tasks must not modify these decisions.
