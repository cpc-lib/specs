# AfterSale Deepening SPEC V2.3

## AfterSaleCase Responsibility
AfterSaleCase owns entitlement/request/decision, not warehouse stock and not payment provider facts.

Types:
- REFUND_ONLY
- RETURN_REFUND
- EXCHANGE
- REPAIR

## Quantity Scope
Every case references exact OrderItem and requested quantity.
Concurrent cases invariant:
active + successfully completed aftersale quantity must not exceed bought quantity,
accounting for prior returns/exchanges/cancellations.

## Refund Only
Allowed only by policy:
- unshipped quantity
- merchant/platform no-return decision
- digital policy
- arbitration result

## Return Refund
APPROVED -> ReturnOrder -> ReverseShipment -> inspection -> REFUND_ELIGIBLE
then Payment Refund flow starts.

## Exchange
APPROVED -> optional ReturnOrder/Inspection -> replacement inventory reserve
-> ExchangeOrder -> replacement fulfillment -> buyer receive -> COMPLETED.

## Repair
APPROVED -> ReturnOrder -> receive/inspection -> RepairOrder
-> repair/quality check -> outbound shipment -> buyer receive -> COMPLETED.

## Seller Decision
Seller cannot approve an amount greater than platform-computed refundable cap.
Seller rejection records reason/evidence and may allow buyer dispute.

## Settlement
Opening aftersale creates/updates settlement hold for affected economic scope.
Closing case releases hold only after refund/reverse logistics facts are final.
