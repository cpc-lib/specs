# Recommendation Domain SPEC V2.6

## Components
- CandidateSource
- RecommendationRequest
- RecommendationResult
- RecommendationModelVersion
- RecommendationPolicy
- RecommendationExperiment
- UserInterestProfile
- ItemFeatureProjection

## Serving
Request
→ eligibility/filter
→ candidate recall
→ de-dup
→ feature fetch
→ model/rules ranking
→ business constraints
→ diversity
→ result.

Candidate sources:
TRENDING
COLLABORATIVE
CONTENT_SIMILAR
SESSION
USER_INTEREST
SIMILAR_ITEM
COMPLEMENTARY
SHOP
RECENTLY_VIEWED

## Filters
offline/blocked/restricted product,
region unavailable,
buyer already hidden,
age/compliance policy,
risk/governance,
duplicate SKU/merchant diversity policy.

## Result
offerId
score
rank
reasonCode
modelVersion
policyVersion
experimentVariant
requestId.

Recommendation result is ephemeral/read-model fact.
