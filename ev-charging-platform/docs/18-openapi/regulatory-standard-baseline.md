# Regulatory Standard Baseline — verified 2026-08-10

The national information-exchange baseline used by SPEC 8.2 is the current GB/T 44130 series:

| Standard | Scope used by this project | Status |
|---|---|---|
| GB/T 44130.1-2024 | General principles | Current; implemented 2024-09-01 |
| GB/T 44130.2-2025 | Public information exchange | Current; implemented 2026-03-01 |
| GB/T 44130.3-2025 | Business information exchange | Current; implemented 2026-03-01 |
| GB/T 44130.4-2025 | Charging equipment ↔ service platform exchange | Current; implemented 2026-03-01 |
| GB/T 44130.5-2025 | Data transmission and security | Current; implemented 2026-03-01 |

Official source checked: 全国标准信息公共服务平台 / 国家市场监督管理总局.

## Project interpretation

For `charging-open`:

- station/public facility snapshots align conceptually with Part 2;
- charging/order business snapshots align conceptually with Part 3;
- Part 4 remains relevant to device ↔ platform integration boundaries;
- transport/authentication design is informed by the security concerns represented by Part 5.

## Critical compliance statement

`GB_T_44130_2025_CANONICAL` is an **engineering adapter boundary**, not a declaration of standards certification.

The generated payload explicitly contains:

`profile = canonical-adapter-not-platform-certified`

Before connecting to a real national/provincial/municipal platform, implement that platform's exact profile:

- mandatory field names/codes
- encryption/signature requirements
- endpoint paths
- certificates
- message acknowledgements
- reporting frequency
- error/retry rules
- local extensions

Then pass the regulator/platform's official conformance or acceptance test.

This avoids pretending that a generic JSON envelope is byte-for-byte compliant with every regulatory implementation.
