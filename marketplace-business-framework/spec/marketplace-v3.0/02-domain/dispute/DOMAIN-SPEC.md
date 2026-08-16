# Dispute Domain SPEC
Aggregate: DisputeCase
States:
OPEN -> EVIDENCE_COLLECTION -> PLATFORM_REVIEW -> DECIDED -> EXECUTING -> RESOLVED -> CLOSED

Evidence may include:
product snapshot, logistics, file evidence, aftersale history, audited chat references.

Decision:
FULL_REFUND / PARTIAL_REFUND / REJECT / COMPENSATION / OTHER_POLICY_ACTION.
