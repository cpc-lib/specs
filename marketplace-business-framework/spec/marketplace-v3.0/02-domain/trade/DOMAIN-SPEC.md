# Trade Domain SPEC
Aggregates:
- Trade
- MerchantOrder
- OrderItem
- OrderSnapshot
- DiscountAllocation

Trade: one buyer submit.
MerchantOrder: split by merchant/shop.
OrderItem: minimal sold item fact.

Trade:
CREATED -> WAITING_PAYMENT -> PAID -> PARTIALLY_FULFILLED -> COMPLETED/CLOSED
WAITING_PAYMENT -> CANCELLED

MerchantOrder:
CREATED -> WAITING_PAYMENT -> PAID -> WAITING_FULFILLMENT
-> PARTIALLY_SHIPPED -> SHIPPED -> RECEIVED -> COMPLETED -> CLOSED
Can cancel before irreversible fulfillment according to policy.

CreateTrade uses Saga with inventory/coupon/promotion reservations.
