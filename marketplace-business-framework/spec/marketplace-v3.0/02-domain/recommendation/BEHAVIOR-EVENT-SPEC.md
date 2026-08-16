# Behavior Event SPEC

Kafka event types:
IMPRESSION
PRODUCT_VIEW
PRODUCT_CLICK
SEARCH
SEARCH_RESULT_CLICK
CART_ADD
CART_REMOVE
FAVORITE_ADD
FAVORITE_REMOVE
SHOP_FOLLOW
SHOP_UNFOLLOW
COUPON_CLAIM
TRADE_PAID
TRADE_COMPLETED
REFUND_SUCCESS
REVIEW_PUBLISHED
HIDE_RECOMMENDATION

Envelope:
eventId
userId(optional)
anonymousId/sessionId
deviceFingerprintRef(optional)
eventType
objectType/objectId
source/page/position
occurredAt
experimentId/variant(optional)
consentContext
properties.

Dedup:
eventId.
Ordering only guaranteed inside selected Kafka partition key, usually user/session.
