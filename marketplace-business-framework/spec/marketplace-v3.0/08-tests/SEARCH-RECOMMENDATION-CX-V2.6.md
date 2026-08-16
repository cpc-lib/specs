# V2.6 Search / Recommendation / Review / CX Test Matrix

## Search
1. ProductPublished creates/replaces OfferSearchDocument.
2. event dataVersion 10 then version 9 -> indexed version stays 10.
3. duplicate projection event idempotent.
4. BLOCKED/OFFLINE offer disappears from buyer search.
5. price/search display is eventual; Checkout revalidation catches stale price.
6. search_after produces stable next page with deterministic sort.
7. deep from/size beyond policy rejected.
8. reindex writes new index and alias switches only after validation.
9. ES unavailable does not block Trade/Payment.
10. prohibited/governance hard filter cannot be bypassed by ranking.

## Recommendation
11. stable experiment assignment for same subject.
12. assignment does not emit exposure until result displayed.
13. offline/blocked candidates removed after recall.
14. hidden recommendation is excluded for policy period.
15. same request/model/policy/feature snapshot yields deterministic ranking where model is deterministic.
16. recommendation outage can degrade to trending/static without blocking checkout.

## Behavior
17. duplicate Kafka eventId counted once in derived feature aggregation.
18. no raw auth/payment credentials in behavior event.
19. user/session partition key preserves intended per-subject ordering.

## Review
20. non-purchaser cannot create verified review.
21. same order item cannot exceed review policy slot.
22. additional review does not overwrite original.
23. seller reply cannot edit buyer review.
24. risk BLOCK + moderation PASS -> review remains blocked.
25. moderation BLOCK + risk PASS -> review remains blocked.
26. review summary rebuild equals online projection.

## Favorites / Follow
27. repeated favorite/follow is idempotent.
28. remove nonexistent relation safe/idempotent.

## Notification / Support / IM
29. notification logical dedup prevents duplicate SMS/email.
30. marketing opt-out does not suppress transactional by default policy.
31. IM duplicate clientMessageId creates one server message.
32. IM content moderation block does not mutate Trade.
33. support case action uses domain command, not direct DB write.
34. Buyer360 stale projection never authorizes refund/payment.
