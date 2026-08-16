# Performance Targets
These are baseline engineering targets, not business SLAs:
- product detail read P99 < 300ms under normal peak
- cart P99 < 300ms
- checkout preview P99 < 800ms
- submit trade synchronous acceptance P99 < 1s excluding queue wait in flash-sale mode
- payment callback ACK after durable local transaction, minimal external calls
- 100k+ flash sale QPS architecture must rely on preheat/rate-limit/queue/hot isolation
Exact numbers require load-test environment sizing.
