# Promotion Compatibility SPEC

Relationship:
STACKABLE
MUTUALLY_EXCLUSIVE
PRIORITY_EXCLUSIVE
BEST_BENEFIT_ONLY
REQUIRED_COMBINATION

Rules may be defined by:
promotion type pair,
campaign group,
owner,
specific campaign/rule.

Compatibility evaluation creates a graph/set constraint.
Selected result must be deterministic.

Examples:
- platform coupon + shop coupon: may STACK
- two shop full-reduction campaigns: usually MUTUALLY_EXCLUSIVE
- flash sale + direct price promotion: BEST_BENEFIT_ONLY or flash priority
- bundle + component promotion: configured explicitly

No hidden hard-coded if/else is allowed outside the versioned compatibility policy.
