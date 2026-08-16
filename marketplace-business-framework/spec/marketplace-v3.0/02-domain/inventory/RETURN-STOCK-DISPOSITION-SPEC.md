# Returned Stock Disposition SPEC

Returned inventory first enters RETURN_QUARANTINE.

`ReturnStockDisposition` decisions:
- RESTOCK_SELLABLE
- RESTOCK_DEFECTIVE
- REPAIR_REQUIRED
- RETURN_TO_VENDOR
- SCRAP
- QUARANTINE

Flow:
ReturnInspection accepted
→ InventoryReturnInbound
→ RETURN_QUARANTINE ledger
→ disposition
→ corresponding stock bucket ledger entry.

Only RESTOCK_SELLABLE increases normal AVAILABLE.
Every action is append-only in inventory ledger and references ReturnOrder/Inspection.
