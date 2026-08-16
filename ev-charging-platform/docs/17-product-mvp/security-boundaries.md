# Product MVP Security Boundaries

## Secure defaults

- development tenant/user headers: OFF by default
- demo IAM bootstrap: OFF by default
- production must provide a random access-token secret
- production must provide a separate internal service key

## Prohibited patterns

- frontend-generated tenant identity as authentication
- frontend-generated user identity for maker-checker
- reusing user bearer token for service-to-service calls
- returning TENANT data to a STATION-scoped principal when station projection is unavailable
- automatic production demo-user creation

## Current limitation

The HMAC Product-MVP token is a local signed access token, not a full OAuth2 authorization server.

The Production Hardening phase should evaluate:

- Spring Security
- OAuth2/OIDC
- key rotation
- refresh tokens
- centralized session revocation
- MFA for financial/administrative roles
