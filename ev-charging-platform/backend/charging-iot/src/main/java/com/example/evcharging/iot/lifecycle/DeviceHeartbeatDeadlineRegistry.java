package com.example.evcharging.iot.lifecycle;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DeviceHeartbeatDeadlineRegistry {
    public static final String DEADLINE_ZSET = "ev:iot:heartbeat:deadlines";
    private final StringRedisTemplate redis;

    public DeviceHeartbeatDeadlineRegistry(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void touch(long tenantId, String deviceId, String leaseValue, Duration ttl) {
        long deadline = System.currentTimeMillis() + ttl.toMillis();
        redis.opsForZSet().add(DEADLINE_ZSET,
                new HeartbeatDeadlineMember(tenantId, deviceId, leaseValue).encode(),
                deadline);
    }

    public void remove(long tenantId, String deviceId, String leaseValue) {
        redis.opsForZSet().remove(DEADLINE_ZSET,
                new HeartbeatDeadlineMember(tenantId, deviceId, leaseValue).encode());
    }
}
