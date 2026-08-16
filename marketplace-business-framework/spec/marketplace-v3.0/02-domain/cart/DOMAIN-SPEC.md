# Cart Domain SPEC
Models:
- Cart
- CartItem

Cart does not freeze price or inventory.
Read path groups by merchant/shop and refreshes price/stock/promotion.
Cart may be stored in Redis + persistent backing depending on retention policy.
