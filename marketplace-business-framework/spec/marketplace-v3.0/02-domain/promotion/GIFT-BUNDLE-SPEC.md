# Gift & Bundle SPEC

## Gift
GiftBenefit references gift SKU + quantity.
Buyer price allocation can be 0, but inventory/cost/funding facts are explicit.
Physical gift inventory uses InventoryReservation.

## Bundle
BundleOffer:
bundleOfferId
component sku/offer quantities
bundle pricing policy
substitution policy
version.

Order snapshot records component economics.
Inventory reserves each component SKU.
Bundle-level refund policy must resolve to component quantities/economics.
