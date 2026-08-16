# Marketplace Specification

This directory is the **single source of truth** for Marketplace business design.

Current frozen baseline:

`marketplace-v3.0`

Do not duplicate the full SPEC into individual service modules.

Each business module keeps only a `MODULE-SPEC.md` mapping document that points back to
the relevant files under `spec/marketplace-v3.0/`.

## Current structure

```text
spec/
├── VERSION
├── README.md
└── marketplace-v3.0/
    ├── 00-master/
    ├── 01-architecture/
    ├── 02-domain/
    ├── 03-database/
    ├── 04-openapi/
    ├── 05-events/
    ├── 08-tests/
    ├── 10-registries/
    ├── 11-codegen/
    ├── 12-test-data/
    ├── 13-acceptance/
    ├── 14-task-bundles/
    ├── 15-deployment/
    └── tasks/
```
