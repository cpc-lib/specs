# Return Order SPEC

Aggregate: ReturnOrder
States:
CREATED -> WAITING_BUYER_SHIP -> IN_TRANSIT -> RECEIVED -> INSPECTING
-> ACCEPTED / PARTIALLY_ACCEPTED / REJECTED -> CLOSED
WAITING_BUYER_SHIP -> EXPIRED / CANCELLED

Fields:
returnNo, afterSaleId, merchantId, orderItemId, expectedQty,
receivedQty, returnAddressSnapshot, requiredShipBefore, status, version.

ReverseShipment is separate from ReturnOrder.

Inspection result per quantity:
- ACCEPT_SELLABLE
- ACCEPT_DEFECTIVE
- ACCEPT_REPAIR
- REJECT_WRONG_ITEM
- REJECT_DAMAGED_BY_BUYER
- REJECT_MISSING_PARTS
- REVIEW_REQUIRED

A refund command is generated only for accepted refundable quantity/amount.
