# Fulfillment & Logistics Domain SPEC
Aggregates:
- FulfillmentOrder
- Package
- Shipment
- LogisticsTrackingEvent

One MerchantOrder can split into multiple packages.
Shipment requires item-level quantity mapping.
Supports buyer confirm and auto-confirm.
Carrier integration through LogisticsProviderAdapter.
