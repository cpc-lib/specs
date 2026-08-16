# Evidence & Arbitration SPEC

EvidenceRecord:
- evidenceId
- disputeId
- submittedByType
- submittedById
- evidenceType
- fileId/referenceId
- sha256
- description
- submittedAt
- status

Evidence is append-only; invalidation is a new status/review fact.

DecisionSnapshot records:
- evidence set/version
- applicable policy/rule version
- reviewer
- decision
- amount
- execution commands
- decidedAt

Platform decision does not directly update payment/inventory/settlement tables.
It emits/executes domain commands to AfterSale, Payment, Inventory, Settlement.
