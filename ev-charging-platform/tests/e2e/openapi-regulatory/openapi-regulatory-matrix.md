# SPEC 8.2 OpenAPI / Regulatory E2E Matrix

## OPEN-AUTH-001 Valid signature

- valid AppKey
- current timestamp
- unique nonce
- correct canonical query
- correct body SHA-256
- valid HMAC
- expected: 200

## OPEN-AUTH-002 Tampering

Independently modify:

- body
- query
- timestamp
- nonce
- signature

Expected: request rejected.

## OPEN-AUTH-003 Replay

Send the exact same correctly signed request twice.

Expected:

- first accepted
- second rejected because nonce was consumed

## OPEN-AUTH-004 Timestamp

Test:

- +301 seconds
- -301 seconds

Expected: rejected with default 300-second window.

## OPEN-AUTH-005 Secret rotation

1. call with old AppSecret → success
2. rotate AppSecret in Admin
3. old secret → reject
4. new secret → success

## OPEN-RATE-001

Configure partner limit = 5 requests/minute.

Send 6 valid signed requests in one minute.

Expected sixth request → 429.

## OPEN-SCOPE-001 Connector command scope

Partner Station Scope = Station A.

- list returns Station A only
- start connector in Station A → allowed
- start connector in Station B by knowing its connectorCode → rejected
- order at Station B → rejected

## OPEN-USER-001

Two partners both send `externalUserId = customer-1`.

Expected: different local shadow user IDs and no order leakage between partners.

## CALLBACK-001

- configured HTTPS callback
- verify callback HMAC
- return 500 for first attempts
- task enters RETRY
- later return 200
- task becomes SENT

## CALLBACK-002 Crash recovery

Force a task to remain SENDING with claim_time older than five minutes.

Expected: recovery job returns it to RETRY.

## REG-001 Snapshot idempotency

No source data changes between two scheduler runs.

Expected: `(platform,dataType,businessKey,payloadHash)` uniqueness prevents duplicate logical tasks.

## REG-002 Adapter boundary

GB/T canonical adapter must emit:

- standardFamily
- standardPart
- `canonical-adapter-not-platform-certified`

No test may assert generic adapter is a certified provincial profile.

## REG-003 Financial isolation

Regulator endpoint unavailable for one hour.

Expected:

- charging/order/payment/ledger continue correctly
- regulatory task retries/dead-letters only
- no financial fact is rolled back

## OUTBOUND-001 Production SSRF policy

Under `APP_ENV=prod`:

- http URL → reject
- localhost → reject
- 10.x/172.16-31/192.168 literal → reject
- non-allowlisted public host → reject
- allowlisted HTTPS host → allow

Also enforce equivalent egress rules at deployment network level.
