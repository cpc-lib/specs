# Technician UniApp

Separate application: `technician-app/`.

It is intentionally separated from the EV-driver `user-app`.

MVP capability:

- development identity/login
- assigned work orders
- start repair
- complete repair
- upload repair photos
- assigned inspections
- start/complete inspections
- spare-stock lookup

Production authentication must replace development `X-Tenant-Id` / `X-User-Id` headers.
