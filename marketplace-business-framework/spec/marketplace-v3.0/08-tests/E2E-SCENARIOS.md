# E2E Scenarios

## E2E-001 Multi Merchant Checkout
Buyer cart:
- Shop A SKU1 ¥600
- Shop B SKU2 ¥400
Platform coupon ¥100
Submit:
- 1 Trade
- 2 MerchantOrders
- discount allocation stored
- inventory reserved separately
- single PaymentOrder
Payment succeeds
- Trade paid
- both orders paid
- clearing allocated by MerchantOrder
- merchant settlement remains pending

## E2E-002 Partial Refund
Refund only one OrderItem.
Other merchant/order unaffected.
Discount allocation snapshot determines maximum refund.

## E2E-003 C2C Dispute
Seller ships → buyer requests return/refund → seller rejects → dispute → evidence → platform decision → refund/settlement freeze released according to result.
