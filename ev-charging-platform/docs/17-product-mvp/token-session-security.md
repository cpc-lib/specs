# Token / Session Security

## Token lifetimes

Development baseline:

- Access Token: 15 minutes
- Refresh Session: 30 days

These are configuration values.

## Logout

Logout revokes the entire authentication session.

Redis:

`ev:auth:revoked-session:{sessionId}`

The Redis revocation TTL extends through the refresh-session expiration.

## Refresh rotation

A refresh request locks the session row and updates:

`refresh_token_hash`

with:

```sql
WHERE refresh_token_hash = old_hash
  AND status = 'ACTIVE'
```

Only one concurrent caller can rotate a refresh token successfully.

## Password change

Password change:

1. validates the current password;
2. stores a new PBKDF2 password hash;
3. revokes all sessions for that user;
4. requires a fresh login.

## Account and RBAC changes

Administrator changes that affect authorization revoke active user sessions.

This prevents an old access token from retaining stale role/DataScope permissions until its normal expiration.

## Production startup guard

When `APP_ENV=prod` or `production`:

- development identity headers must be OFF;
- access secret must not be a development default;
- internal service key must not be a development default;
- access secret and service key must differ.

The service refuses to start if these requirements are violated.
