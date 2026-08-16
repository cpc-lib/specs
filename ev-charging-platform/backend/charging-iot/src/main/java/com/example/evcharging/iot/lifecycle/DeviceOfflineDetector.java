package com.example.evcharging.iot.lifecycle;

import com.example.evcharging.framework.contract.DeviceLifecycleEvent;
import com.example.evcharging.framework.contract.DeviceRouteLease;
import com.example.evcharging.framework.event.DomainEventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class DeviceOfflineDetector {
    private static final DefaultRedisScript<Long> RELEASE_IF_MATCH = new DefaultRedisScript<>("""
            local removed = 0
            if redis.call('get', KEYS[1]) == ARGV[1] then redis.call('del', KEYS[1]); removed = removed + 1 end
            if redis.call('get', KEYS[2]) == ARGV[1] then redis.call('del', KEYS[2]); removed = removed + 1 end
            return removed
            """, Long.class);
    private final StringRedisTemplate redis;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;
    private final int batchSize;

    public DeviceOfflineDetector(
            StringRedisTemplate redis,
            KafkaTemplate<String, String> kafka,
            ObjectMapper mapper,
            @Value("${iot.lifecycle-topic:ev.device.lifecycle.v1}") String topic,
            @Value("${iot.offline-scan-batch-size:200}") int batchSize) {
        this.redis = redis;
        this.kafka = kafka;
        this.mapper = mapper;
        this.topic = topic;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${iot.offline-scan-ms:10000}")
    public void scan() {
        long now = System.currentTimeMillis();
        Set<String> due = redis.opsForZSet().rangeByScore(
                DeviceHeartbeatDeadlineRegistry.DEADLINE_ZSET, 0, now, 0, batchSize);
        if (due == null || due.isEmpty()) return;
        for (String encoded : due) inspect(encoded, now);
    }

    void inspect(String encoded, long now) {
        HeartbeatDeadlineMember member;
        try {
            member = HeartbeatDeadlineMember.parse(encoded);
        } catch (RuntimeException malformed) {
            redis.opsForZSet().remove(DeviceHeartbeatDeadlineRegistry.DEADLINE_ZSET, encoded);
            return;
        }

        String onlineKey = onlineKey(member.tenantId(), member.deviceId());
        String routeKey = routeKey(member.tenantId(), member.deviceId());
        String currentOnline = redis.opsForValue().get(onlineKey);
        String currentRoute = redis.opsForValue().get(routeKey);

        // A later connection/heartbeat won. The old deadline is stale.
        if ((currentOnline != null && !currentOnline.equals(member.leaseValue()))
                || (currentRoute != null && !currentRoute.equals(member.leaseValue()))) {
            redis.opsForZSet().remove(DeviceHeartbeatDeadlineRegistry.DEADLINE_ZSET, encoded);
            return;
        }

        Double score = redis.opsForZSet().score(DeviceHeartbeatDeadlineRegistry.DEADLINE_ZSET, encoded);
        if (score != null && score > now) return;

        // If the old lease is still visible, remove only that exact lease atomically.
        // If a new connection wins between GET and this script, removed == 0 and OFFLINE is suppressed.
        if (member.leaseValue().equals(currentOnline) || member.leaseValue().equals(currentRoute)) {
            Long removed = redis.execute(RELEASE_IF_MATCH, List.of(onlineKey, routeKey), member.leaseValue());
            if (removed == null || removed == 0L) {
                redis.opsForZSet().remove(DeviceHeartbeatDeadlineRegistry.DEADLINE_ZSET, encoded);
                return;
            }
        }

        DeviceRouteLease lease = DeviceRouteLease.parse(member.leaseValue());
        String eventId = "device-offline:" + member.tenantId() + ":" + member.deviceId() + ":" + lease.connectionToken();
        try {
            var payload = new DeviceLifecycleEvent(
                    member.tenantId(), "OFFLINE", member.deviceId(),
                    lease.gatewayId(), lease.connectionToken(), "HEARTBEAT_TIMEOUT", Instant.ofEpochMilli(now));
            var envelope = new DomainEventEnvelope<>(
                    eventId, "iot.device.offline", "1.0", "Device", member.deviceId(),
                    member.tenantId(), null, Instant.now(), "charging-iot", payload);
            kafka.send(topic, member.deviceId(), mapper.writeValueAsString(envelope)).get(5, TimeUnit.SECONDS);
            redis.opsForZSet().remove(DeviceHeartbeatDeadlineRegistry.DEADLINE_ZSET, encoded);
        } catch (Exception publishFailed) {
            throw new IllegalStateException("cannot publish device offline event", publishFailed);
        }
    }

    private String onlineKey(long tenantId, String deviceId) {
        return "ev:" + tenantId + ":device:online:" + deviceId;
    }

    private String routeKey(long tenantId, String deviceId) {
        return "ev:" + tenantId + ":device:route:" + deviceId;
    }
}
