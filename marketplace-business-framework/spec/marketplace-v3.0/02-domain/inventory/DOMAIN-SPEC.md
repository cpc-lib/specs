# Inventory Domain SPEC
Aggregates:
- InventoryStock
- InventoryReservation
- InventoryLedger

Buckets:
ON_HAND / AVAILABLE / RESERVED / LOCKED / IN_TRANSIT / DEFECTIVE / FROZEN

Reservation states:
RESERVED / COMMITTED / RELEASED / EXPIRED

Normal reserve must use conditional DB update:
available_qty >= requested_qty.

Every stock change creates InventoryLedger.
