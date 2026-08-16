# Customer Service Domain SPEC

CustomerServiceCase:
caseNo
requesterType/requesterId
buyerId
merchantId/shopId
relatedTrade/order/afterSale/dispute/payment/shipment
category
priority
status
assignedGroup/agent
SLA
resolution
createdAt/closedAt.

States:
OPEN -> ASSIGNED -> IN_PROGRESS -> WAITING_CUSTOMER / WAITING_MERCHANT / WAITING_INTERNAL
-> RESOLVED -> CLOSED
May REOPEN by policy.

Support can issue approved domain commands through existing APIs.
Support case must not directly mutate external service DB.
