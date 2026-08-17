# ADR-041 — Refund Reverse Clearing

Refund must reverse the original cash/funding/settlement economics rather than only decrementing Trade paidAmount.
Original allocation snapshots determine reverse allocation.
If merchant funds were already paid out, refund creates merchant negative balance/future deduction instead of mutating closed settlement.
