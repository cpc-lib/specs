# Pricing Domain SPEC
Models:
- PriceBook
- OfferPrice
- PriceRule
- PriceSnapshot

Supported dimensions:
base, sale, member, promotion, channel, region.

Checkout and SubmitTrade must recalculate server-side.
Trade stores immutable PriceSnapshot and discount allocation.
