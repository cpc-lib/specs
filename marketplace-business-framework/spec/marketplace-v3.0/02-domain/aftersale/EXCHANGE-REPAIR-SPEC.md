# Exchange & Repair SPEC

## ExchangeOrder
Fields:
exchangeNo, afterSaleId, originalOrderItemId, replacementSkuId,
replacementQty, priceDifferencePolicy, inventoryReservationNo,
fulfillmentNo, status.

States:
CREATED -> WAITING_RETURN(optional) -> WAITING_REPLACEMENT_STOCK
-> RESERVED -> FULFILLING -> SHIPPED -> RECEIVED -> COMPLETED
Branches: FAILED / CANCELLED / DISPUTE.

Replacement inventory uses normal InventoryReservation correctness rules.
Do not create an unrelated buyer Trade unless commercial policy explicitly requires price re-purchase.

## RepairOrder
States:
CREATED -> WAITING_RECEIPT -> INSPECTING -> REPAIRING -> QUALITY_CHECK
-> RETURN_SHIPPING -> RECEIVED -> COMPLETED
Branches: UNREPAIRABLE / DISPUTE / CANCELLED.

Repair SLA and cost responsibility are snapshotted from original policy and decision.
