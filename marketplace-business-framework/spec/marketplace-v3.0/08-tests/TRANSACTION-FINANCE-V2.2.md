# Transaction & Finance V2.2 Test Matrix

## Funding conservation
- platform coupon ¥10 on merchant item ¥100: buyer cash ¥90, merchant entitlement remains according to campaign snapshot;
- merchant coupon ¥10: buyer cash ¥90 and merchant entitlement reduced according to snapshot;
- cross-item residual ¥0.01 deterministic and replay-stable.

## Clearing
- PaymentSucceeded x100 -> one PaymentClearingRecord and one effective merchant pending effect;
- payment transaction amount != clearing allocation sum -> CRITICAL exception;
- provider fee updated by statement produces explicit adjustment, not silent mutation.

## Refund
- before settlement: refund reverses merchant pending;
- after approved settlement but before payout: payable adjusted/re-reserved safely;
- after payout: settlement immutable, merchant negative/future deduction created;
- refund UNKNOWN keeps quota, no final reverse clearing.

## Settlement/Payout
- one SettlementBatch -> one MerchantPayable;
- concurrent payout 80+80 against payable100 -> reserved+paid <=100;
- payout UNKNOWN cannot create second provider request;
- duplicate provider payout callback -> one PayoutTransaction/effect.

## C2C hold
- completed order remains HOLDING until holdUntil;
- dispute extends hold;
- risk freeze blocks eligibility;
- release job revalidates before release.

## Daily close
- payment success no clearing detected;
- refund success no reverse clearing detected;
- settlement payable mismatch detected;
- payout statement differs by ¥0.01 -> mismatch;
- unbalanced ledger -> CRITICAL.
