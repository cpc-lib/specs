# SPEC 7.7 Progress — Finance Hardening

Status: `foundation-rc-finance-hardening`

## Completed in this release candidate

- T+1 automatic reconciliation schedule.
- Raw normalized channel bill archive with SHA-256.
- Consumer-safe reconciliation idempotency.
- Append-only financial Adjustment.
- Adjustment maker-checker approval.
- Adjustment Reversal.
- Reconciliation uses original facts + posted adjustments.
- Settlement calculation changed to PENDING_APPROVAL.
- Settlement maker-checker approval/rejection.
- Settlement Ledger Posting.
- Invoice provider abstraction + Mock provider.
- Invoice issue / retry / red flush.
- Admin pages for Adjustment and Invoice.
- Updated Settlement approval UI.
- Project schedule remains embedded in the engineering package.

## Schedule position

This release spans late **P6 (W18-W21)** and an early slice of **P7 (W22-W23)**.

Production V1 baseline remains **47 weeks / 235 person-days** for one developer + AI.

The schedule is intentionally not compressed because remaining real-world work still includes:

- real WeChat / Alipay statement acquisition,
- production object-storage adapter,
- real invoice-provider sandbox/certification,
- settlement payout/disbursement workflow,
- finance close-period controls,
- end-to-end Docker/Testcontainers runtime verification.
