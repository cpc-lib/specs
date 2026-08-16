# Buyer 360 Read Model SPEC

Buyer360View is CQRS projection.

Contains safe/service-authorized summaries:
buyer profile/member level
recent Trade/MerchantOrder
lifetime order/GMV summary (derived)
favorite/follow summary
coupon summary
review history summary
aftersale/dispute summary
customer service cases
notification/contact preference
risk/support flags limited by role.

Source domains remain authoritative.
Projection lag is displayed/observable where material.
No payment/settlement decision is made solely from Buyer360View.
