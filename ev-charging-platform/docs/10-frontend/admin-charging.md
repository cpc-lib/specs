# Admin Charging UI - SPEC 7.4

Added pages:

- Chargers & Connectors
- Realtime Charging

The charging page first loads the current REST projection and then subscribes to
`POST /app-api/v1/charging/sessions/{sessionNo}/realtime-ticket` issues a 60-second, single-use Redis ticket.
The browser then connects to `/ws/charging?ticket=...`. The handshake consumes the ticket and recovers the tenant/session identity.

Production still needs full JWT/member authorization at ticket issuance, but tenant identifiers are no longer trusted from WebSocket query parameters.
