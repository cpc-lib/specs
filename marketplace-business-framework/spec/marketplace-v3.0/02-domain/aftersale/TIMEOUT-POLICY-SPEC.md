# AfterSale Timeout Policy

Explicit deadlines:
- sellerReviewDeadline
- buyerReturnShipDeadline
- returnReceiveDeadline
- inspectionDeadline
- exchangeShipDeadline
- repairCompleteDeadline

Timeout job reads records with deadline < now by keyset pagination.
Every timeout handler locks/revalidates current aggregate before transition.

Examples:
seller review timeout -> auto approve / platform review according to policy.
buyer return timeout -> ReturnOrder EXPIRED; do not refund.
inspection timeout -> escalation, not silent acceptance unless policy explicitly allows.
