# Outbound HTTP Security

Partner Callback and Regulatory Dispatch are outbound HTTP capabilities and therefore potential SSRF surfaces.

## Production rules

When `APP_ENV=prod` or `production`:

- outbound URL must use HTTPS;
- host must be present in `OPENAPI_ALLOWED_OUTBOUND_HOSTS`;
- localhost/private literal IPs are rejected;
- URL user-info is rejected;
- OpenAPI master key may not use the development default.

The URL is validated:

1. when configuration is saved;
2. again immediately before each HTTP dispatch.

The second check protects against stale or manually altered database configuration.

## DNS note

Host allowlisting reduces risk but does not itself solve every DNS-rebinding scenario.

A production network policy should additionally constrain egress at the Kubernetes/VPC/firewall layer.
