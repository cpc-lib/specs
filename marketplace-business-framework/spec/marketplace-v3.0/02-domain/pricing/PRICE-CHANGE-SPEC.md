# Price Change / Approval SPEC

High-risk price changes can require Flowable approval:
- price below floor
- change beyond configured percentage
- regulated category
- platform self-operated margin guard

PriceChangeRequest:
DRAFT -> PENDING_APPROVAL -> APPROVED -> SCHEDULED -> EFFECTIVE
or REJECTED/CANCELLED.

Effective price row is immutable; correction creates new version.
