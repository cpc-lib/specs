# Runbook
P0:
- duplicate payment/refund/payout
- negative inventory
- cross-merchant data leak
- settlement overpay
- payment merchant routing error

P1:
- payment/refund/payout UNKNOWN surge
- MQ backlog
- ES index lag
- reconciliation critical backlog
- hot SKU saturation

Never directly repair money by SQL UPDATE. Use repair/reversal commands with audit.
