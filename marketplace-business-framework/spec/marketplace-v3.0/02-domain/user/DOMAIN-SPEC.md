# User Domain SPEC
Aggregates:
- User
- UserAddress
- UserIdentity
- MemberAccount
- PointsAccount

User status: ACTIVE / FROZEN / CLOSED.
Addresses are mutable master data; orders store immutable AddressSnapshot.
Points changes require PointsLedger.
High-risk actions use device/risk checks.
