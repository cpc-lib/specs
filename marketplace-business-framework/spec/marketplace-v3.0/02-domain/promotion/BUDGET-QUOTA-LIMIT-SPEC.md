# Budget / Quota / Purchase Limit SPEC

## Campaign Budget
BudgetAccount:
totalBudget, reservedBudget, consumedBudget, releasedBudget.

Invariant:
reserved + consumed <= totalBudget.

## Quota
Quota dimensions:
CAMPAIGN / RULE / SKU / REGION / CHANNEL.

Reservation:
RESERVED -> COMMITTED / RELEASED / EXPIRED.

## Purchase Limit
Limit dimensions:
USER / DEVICE / ACCOUNT_GROUP / REGION / SKU / CAMPAIGN.

Counters must distinguish:
reserved quantity
paid/committed quantity
refunded/released quantity according to policy.

## High Concurrency
Front counters can use Redis/Lua.
Durable reservation/ledger remains required.
Reconciliation repairs Redis projections from durable facts.
