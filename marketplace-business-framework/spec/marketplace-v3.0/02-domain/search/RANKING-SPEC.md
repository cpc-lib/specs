# Search Ranking SPEC

RankPolicy is versioned.

Feature groups:
TEXT_RELEVANCE
BUSINESS_QUALITY
SALES_CONVERSION
REVIEW_QUALITY
DELIVERY
PRICE_COMPETITIVENESS
FRESHNESS
RISK_PENALTY
PERSONALIZATION
SPONSORED

Hard filters happen before ranking.

Sponsored:
- separate sponsored eligibility
- explicit `sponsored=true`
- organicRankScore retained separately where needed.

Rank result can log sampled explainability, not every sensitive feature.
