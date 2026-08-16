# Release Gates V2.2

All V2.1 gates remain mandatory plus:
1. every OrderItem has economics/funding snapshot;
2. PaymentTransaction -> Clearing -> MerchantPending conservation passes;
3. platform-funded vs merchant-funded discount settlement tests pass;
4. refund reversal before/after settlement passes;
5. one settlement creates one payable;
6. concurrent payout cannot over-reserve payable;
7. payout UNKNOWN is query-before-retry;
8. MerchantBalanceAccount reconciles to MerchantBalanceLedger;
9. all ledger entries balance;
10. daily close detects unexplained ¥0.01;
11. C2C hold prevents premature settlement;
12. regulatory boundary is preserved: no implementation claims unsupported custody/escrow capability.
