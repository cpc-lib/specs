# Observability
OpenTelemetry trace across Gateway/HTTP/MQ/Kafka/Jobs/providers.
Structured logs:
traceId, userId, merchantId, shopId, tradeNo, orderNo, paymentNo.
Prometheus avoids userId/SKU/merchantId high-cardinality labels.
Business analytics goes to Kafka/ClickHouse/warehouse.
