# Query Understanding SPEC

Models:
SearchQueryPlan
SearchSuggestion
SynonymSet
QueryRewriteRule

Pipeline:
normalize
→ tokenize
→ synonym expansion
→ typo candidate
→ intent/category/brand extraction
→ query plan.

QueryPlan snapshots:
originalText
normalizedText
tokens
synonyms
correction
intent
filters
analyzerVersion
dictionaryVersion
rewriteVersion.

AI rewrite may propose normalized intent but cannot silently erase the original query.
