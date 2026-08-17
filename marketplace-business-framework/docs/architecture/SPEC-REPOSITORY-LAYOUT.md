# SPEC Repository Layout

## Single source of truth

```text
spec/marketplace-v3.0/
```

is the authoritative business specification.

## Relationship

```text
Marketplace V3.0 SPEC
        ↓
MODULE-SPEC.md
        ↓
interfaces / application / domain / infrastructure
        ↓
Java implementation
        ↓
tests
```

## Forbidden

Do not create:

```text
backend/marketplace-trade/spec/
backend/marketplace-payment/spec/
backend/marketplace-product/spec/
```

with duplicated copies of the global specification.

Module-local documentation may only contain mapping and implementation notes.
