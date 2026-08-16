# V2.3 Release Gates

Mandatory:
- package item quantities never exceed fulfillable OrderItem quantities
- partial shipment summary is correct
- duplicate/out-of-order tracking callbacks are safe
- carrier delivered != automatic order completed
- refusal does not erase original fulfillment facts
- RETURN_REFUND cannot skip required return/inspection
- aftersale quantity cannot exceed bought quantity
- accepted return quantity caps refund quantity/amount
- return stock is quarantined before disposition
- only RESTOCK_SELLABLE increments AVAILABLE
- exchange replacement stock uses normal InventoryReservation
- repair/exchange/return timeouts are explicit and idempotent
- dispute execution uses domain commands
- settlement hold interacts correctly with partial aftersale
