# ADR-038 — Binding Tables Carry Explicit Routing Keys

Decision:
Hot child tables carry the same route key as their binding family:
Trade buyer_id; Payment payment_no; Settlement merchant_id; Review offer_id; IM buyer_id.
This avoids broadcast routing and removes the need for cross-shard lookup before ordinary child writes.
