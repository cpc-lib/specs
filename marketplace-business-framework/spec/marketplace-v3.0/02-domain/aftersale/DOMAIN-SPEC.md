# AfterSale Domain SPEC
Aggregate: AfterSaleCase

Types:
REFUND_ONLY / RETURN_REFUND / EXCHANGE / REPAIR

State:
APPLIED -> REVIEWING -> APPROVED -> WAITING_RETURN -> RETURNED -> INSPECTING -> REFUNDING -> COMPLETED
REVIEWING -> REJECTED
Any eligible state -> DISPUTE

Refund calculator consumes immutable order discount/tax/shipping allocation snapshots.
