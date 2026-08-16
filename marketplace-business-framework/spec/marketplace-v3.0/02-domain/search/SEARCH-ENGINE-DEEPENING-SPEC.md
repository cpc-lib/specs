# Search Engine Deepening SPEC

## Indexes
Recommended logical aliases:
- marketplace_offer_read_vN
- marketplace_shop_read_vN
- marketplace_suggestion_vN

Use read/write aliases for zero-downtime reindex.

## OfferSearchDocument
Fields:
offerId, dataVersion, offerVersionId,
spuId, skuId,
merchantId, shopId,
categoryPath, brandId,
title, titleTokens,
attributeKeyword,
filterAttributes,
displayPrice, priceBucket,
promotionLabels,
saleable,
regions,
stockAvailabilitySummary,
salesSummary,
reviewSummary,
shopSummary,
governanceFlags,
rankFeatures,
updatedAt.

## Projection
Consumers:
ProductPublished/Blocked
PriceChanged
InventoryAvailabilityChanged
PromotionChanged
ReviewSummaryChanged
ShopStatus/ScoreChanged

Projection is idempotent by eventId and version.

## Query
SearchRequest:
queryText
categoryId
filters
priceRange
merchant/shop
region
sort
pageSize
searchAfter
userContextRef(optional)

## Sort
RELEVANCE
SALES
PRICE_ASC
PRICE_DESC
RATING
NEWEST

## Facet
Category, brand and SEARCH/filterable category attributes.
Aggregation fields must use keyword/numeric doc-values appropriate mappings.

## Availability
The index can expose coarse availability.
Checkout/Trade must revalidate exact inventory/region constraints against source services.
