# Key Rotation

## Partner AppSecret

Already supported:

- rotate AppSecret
- old HMAC immediately stops validating

## Partner Callback Secret

Already supported independently from AppSecret.

## OpenAPI encryption master key

SPEC 8.2 ciphertext:

`v1:<ciphertext>`

did not contain a key ID.

SPEC 8.3 ciphertext:

`v2:<keyId>:<ciphertext>`

supports a key ring.

Configuration:

- `OPENAPI_MASTER_KEY_ID`
- `OPENAPI_MASTER_KEY_BASE64`
- `OPENAPI_PREVIOUS_MASTER_KEYS`

Previous key format:

```text
old-2026-01=<base64>,old-2025-10=<base64>
```

## Safe rotation procedure

For an 8.2 database:

1. deploy 8.3 code with the **old master key** as active;
2. call the Admin secret-rewrap operation;
3. all v1 ciphertext becomes `v2:old-key-id:*`;
4. deploy the new master key as active;
5. include the old key in `OPENAPI_PREVIOUS_MASTER_KEYS`;
6. run rewrap again;
7. confirm all ciphertext uses the new key ID;
8. remove the previous key only after rollback/conformance window closes.

This avoids guessing which key encrypted a legacy v1 ciphertext.

The rewrap operation never returns secret plaintext.
