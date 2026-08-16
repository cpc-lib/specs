# Checkout Domain SPEC
Aggregate: CheckoutSession
Contains buyer/address/items/prices/discounts/shipping/tax/stock/risk preview.
CheckoutToken protects submit integrity and replay.
SubmitTrade must revalidate all mutable facts.
CheckoutSession expires.
