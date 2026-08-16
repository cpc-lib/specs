# Runtime Architecture

Gateway 8080 → Asset 8082 / Core 8083；IoT 8087 + TCP 19090。
Nacos 负责服务发现，Redis 负责实时设备状态，Kafka 负责领域事件/Telemetry，RabbitMQ 预留设备命令与通知。
