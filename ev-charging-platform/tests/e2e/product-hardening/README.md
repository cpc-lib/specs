# SPEC 8.1 Product Hardening E2E

This directory defines the Product MVP release acceptance matrix.

The tests require a live stack:

- Gateway
- System
- Asset
- Core
- Payment
- Finance
- Operation
- Redis
- MySQL
- Kafka
- RabbitMQ
- Nacos

Run `scripts/product_e2e_smoke.sh` only after the runtime stack is healthy.

The shell script is a smoke gate, not a replacement for the full matrix in `product-e2e-matrix.md`.
