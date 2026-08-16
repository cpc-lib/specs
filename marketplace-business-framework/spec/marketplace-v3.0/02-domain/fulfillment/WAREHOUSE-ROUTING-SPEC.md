# Warehouse Routing SPEC

## Inputs
- merchant fulfillment mode
- destination region/geocode
- inventory availability and reservations
- warehouse service area
- SKU storage class
- cold chain / hazardous / oversized constraints
- carrier service
- promised SLA
- shipping cost
- split-package count/cost
- merchant routing priority
- risk/compliance restriction

## Output
`FulfillmentRouteSnapshot`:
- routeVersion
- candidate warehouses
- selected warehouse(s)
- selected quantities by SKU
- score components
- reason codes
- carrier/service suggestion
- promisedShipAt
- promisedDeliverAt

## Rules
1. Route result is snapshotted for audit.
2. Routing never directly decrements stock.
3. Inventory reservation remains inventory-domain command.
4. If split is required, create multiple FulfillmentOrders.
5. Re-routing after payment requires versioned command and cannot double reserve stock.
