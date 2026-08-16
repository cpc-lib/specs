# Delivery / Receive SPEC

Facts:
1. CarrierDelivered: provider claims delivered.
2. BuyerReceived: buyer explicitly confirms.
3. AutoReceived: platform policy closes receive window.
4. RejectedDelivery: buyer/carrier refusal fact.

Auto receive requires:
- carrier delivered or policy equivalent
- configured elapsed period
- no blocking dispute/return request created before cutoff
- current state revalidation

Rejected delivery:
Shipment -> RETURNING
Package -> RETURNING
MerchantOrder remains fulfilled/shipped summary until reverse path decides final outcome.
Never set original Trade to CANCELLED merely because delivery was refused.
