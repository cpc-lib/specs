# ADR-015 — Guaranteed Transaction Regulatory Boundary

## Decision
Marketplace `GUARANTEED_TRANSACTION` is a business settlement-hold model, not a claim that the platform itself is a licensed escrow/custody institution.

Actual customer-fund custody, split settlement and payout must use licensed PSP/bank products appropriate to jurisdiction.

## Consequences
- no fictitious internal "bank balance" treated as real custodied cash;
- provider sub-merchant/account references are tokens/references;
- PaymentTransaction records provider-confirmed fact;
- platform clearing/settlement ledgers are operational/accounting records;
- deployment-specific legal/accounting review can replace posting policy without changing transaction facts.
