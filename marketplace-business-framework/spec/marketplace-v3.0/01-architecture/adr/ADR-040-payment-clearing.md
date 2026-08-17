# ADR-040 — Payment Clearing Boundary

Payment service owns provider transaction facts.
Finance owns payment clearing and operational money allocation.
Settlement owns merchant eligibility/payable/payout lifecycle.
No service may shortcut PaymentSucceeded directly into merchant available balance.
