# Payment Domain SPEC
Aggregates:
- PaymentOrder
- PaymentAttempt
- PaymentTransaction
- RefundOrder
- RefundTransaction
- RefundQuotaReservation

PaymentOrder = business payment intent.
PaymentAttempt = provider attempt.
PaymentTransaction = provider-confirmed money fact.

Payment UNKNOWN:
query provider before retry.

Callback validates:
signature, merchant config, appId, paymentNo, currency, exact amount, provider status.

Refund invariant:
successful_refund + reserved_refund <= refundable_amount.
