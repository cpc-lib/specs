# SPEC Entrypoint

Always start from:

1. `spec/VERSION`
2. `spec/marketplace-v3.0/00-master/MASTER-SPEC-V3.0.md`
3. `spec/marketplace-v3.0/00-master/V3.0-FROZEN-CONTRACT.md`
4. `spec/marketplace-v3.0/00-master/CODEGEN-ENTRYPOINT.md`
5. the selected service's `MODULE-SPEC.md`

## Rule

`spec/marketplace-v3.0/` is authoritative.

Service source code, local README files, database entities and API implementations must not
silently redefine frozen business semantics.
