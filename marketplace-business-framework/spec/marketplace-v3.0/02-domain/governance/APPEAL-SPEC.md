# Appeal SPEC

Aggregate: AppealCase

Fields:
appealNo
violationId
merchantId
appellantUserId
reason
evidenceIds
status
decision
decisionReason
reviewer
decidedAt
version

States:
SUBMITTED -> EVIDENCE_REVIEW -> PLATFORM_REVIEW -> DECIDED -> EXECUTING -> CLOSED
May move to REJECTED if not eligible/expired.

Decision:
UPHOLD
PARTIAL_REVERSE
FULL_REVERSE
REOPEN_INVESTIGATION

Reversal creates PenaltyReversalAction; original penalty record is never deleted.
