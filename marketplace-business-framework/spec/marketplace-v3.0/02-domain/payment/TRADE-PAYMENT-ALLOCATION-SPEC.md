# Trade Payment Allocation SPEC

PaymentTransaction proves channel money. `TradePaymentAllocation` maps that money to the marketplace trade/order facts.

Fields:
- allocationId
- paymentTransactionId
- paymentNo
- tradeId
- merchantOrderId
- orderItemId nullable
- amount
- currency
- allocationType: GOODS / SHIPPING / TAX / OTHER
- createdAt

Invariants:
- sum allocations for successful PaymentTransaction = transaction amount
- merchant-order allocation sum = corresponding paid amount
- allocation is append-only; correction uses reversal/adjustment
- payment allocation does not itself mean merchant settlement eligibility
- duplicate PaymentSucceeded event creates no duplicate allocation
