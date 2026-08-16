# SPU / SKU / Offer Version SPEC

## SPU
Standard product identity independent of seller.

## SPUVersion
Immutable after APPROVED/PUBLISHED.
Contains:
title/description/category/brand/attributes/media/compliance references.

## SKU
Concrete sale specification combination.

Unique normalized key:
spuId + sorted(saleAttributeCode=valueId/valueNormalized)

## SKUVersion
Captures specifications, barcode/GTIN, dimensions and status.

## Offer
Seller/shop listing of a SKU.

## OfferVersion
Immutable publication content:
- seller title
- media
- sale policy
- aftersale policy
- logistics template
- invoice capability
- region restrictions
- product version references
- compliance snapshot ref

## Publishing
Draft editing occurs on unpublished version.
Publish performs:
merchant/shop active check
category eligibility
brand authorization
compliance checks
moderation
price availability
required logistics/aftersale policy
then atomically activates version and emits event.

## Transaction Snapshot
OrderItem snapshots the active OfferVersion/SPUVersion/SKUVersion IDs plus rendered essentials.
