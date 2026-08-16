# Digital / Virtual Fulfillment SPEC

Types:
- LICENSE_CODE
- DIGITAL_FILE
- ACCOUNT_ENTITLEMENT
- VIRTUAL_TOPUP
- SERVICE_VOUCHER

Aggregate: DigitalFulfillment
Fields:
digitalFulfillmentNo, orderItemId, provider, externalDeliveryNo,
entitlementType, deliveryStatus, deliveredAt, consumedAt, expiryAt, version.

States:
CREATED -> PROCESSING -> DELIVERED -> CONSUMED -> COMPLETED
PROCESSING -> FAILED / UNKNOWN
UNKNOWN -> DELIVERED / FAILED

Digital delivery secret/code must not be placed in MQ/log plaintext.
Refund eligibility can depend on consumedAt and product policy.
