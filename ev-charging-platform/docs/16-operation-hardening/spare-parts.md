# Spare Parts Inventory

## Model

- Spare Part Catalog
- Warehouse Stock
- Append-only Stock Transaction

## Receive

Atomic MySQL upsert increments available stock.

## Consume

```sql
UPDATE operation_spare_stock
SET available_qty = available_qty - ?
WHERE available_qty >= ?
```

This is the final integrity guard against negative inventory.

Every stock movement requires a `requestId` and is idempotent per tenant.

Work-order consumption requires `workOrderNo`.

Historical stock movements are append-only.
