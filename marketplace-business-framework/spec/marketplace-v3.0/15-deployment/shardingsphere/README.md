# ShardingSphere sample
This is a routing baseline/example, not an environment-sized production topology.
Before production, choose datasource/table counts from capacity test and migration plan.
Trade-family tables sharing buyer_id must use aligned routing when local joins/transactions are required.
Merchant-facing order listing uses MerchantOrderReadModel/index rather than broadcast scans.
