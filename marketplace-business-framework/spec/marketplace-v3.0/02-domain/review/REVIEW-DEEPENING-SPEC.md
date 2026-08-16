# Review Deepening SPEC

## Aggregate
Review:
reviewNo
buyerId
merchantId/shopId
offerId/orderItemId
ratings
content/media
verifiedPurchase
status
riskDecisionRef
moderationRef
createdAt.

Eligibility:
completed/received order item
within configured review window
allowed review count policy.

## Additional Review
AdditionalReview is append-only child:
reviewId
content/media
createdAt.
Original review remains.

## Seller Reply
SellerReviewReply:
reviewId
merchant/shop
content
moderation status
version.

Seller reply cannot modify buyer text/rating.

## States
PENDING -> PUBLISHED
PENDING -> REVIEWING -> PUBLISHED/BLOCKED
PUBLISHED -> HIDDEN/BLOCKED
HIDDEN/BLOCKED -> APPEALED/RESTORED by policy.

## Review Summary
Derived and rebuildable.
