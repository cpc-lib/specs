# Bounded Contexts

| Context | Owns | Does Not Own |
|---|---|---|
| User | buyer profile, identity, addresses | merchant staff |
| Merchant | merchant onboarding, qualification, deposit | shop product |
| Shop | shop profile, staff scope | merchant legal identity |
| Product | SPU/SKU/Offer metadata | price, stock |
| Pricing | price books/snapshots | product lifecycle |
| Inventory | stock/reservation/ledger | order state |
| Promotion | campaigns/coupons/budget | final Trade |
| Checkout | ephemeral calculated checkout | long-lived order |
| Trade | Trade/MerchantOrder/OrderItem/snapshots | payment channel |
| Payment | payment/refund channel facts | merchant settlement |
| Fulfillment | fulfillment/package/shipment | refund decision |
| AfterSale | aftersale and return workflow | payment provider |
| Dispute | arbitration/evidence | shipment execution |
| Settlement | merchant settlements/payables | buyer payment attempt |
| Finance | marketplace ledger/recon facts | product |
| Invoice | invoices/red flush/delivery | settlement rules |
| Search | read model | product truth |
| Risk | decisions/rules | business truth states |
| Moderation | content review | order |
