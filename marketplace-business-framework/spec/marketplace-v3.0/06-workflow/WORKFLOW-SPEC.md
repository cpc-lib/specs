# Flowable Workflow SPEC

Human approvals:
- merchant onboarding review
- merchant qualification special review
- platform dispute arbitration escalation
- high-value refund approval
- settlement exceptional adjustment
- merchant punishment appeal
- high-risk payout/manual finance adjustment

Flowable never directly updates business state tables.
Workflow completion invokes application command; domain revalidates.
