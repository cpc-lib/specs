# Partner Callback

Partner callbacks use a persisted delivery task.

```text
Charging / Order state
→ Callback Task
→ claim_token
→ HTTP Worker
→ SENT / RETRY / DEAD
```

HTTP is never executed while a business DB transaction is holding the charging/order transaction open.

## Callback signature

Headers:

- `X-Callback-Timestamp`
- `X-Callback-Nonce`
- `X-Callback-Body-SHA256`
- `X-Callback-Signature-Version: v1`
- `X-Callback-Signature`

Canonical string:

```text
timestamp
nonce
bodySha256
```

Signature:

`hex(HMAC-SHA256(callbackSecret, canonical))`

## Delivery

- success: HTTP 2xx
- maximum automatic attempts: 8
- exponential backoff
- DEAD tasks can be manually retried
- stale `SENDING` claims are recovered after 5 minutes

Callback URL and callback secret are controlled separately from AppSecret.
