package com.example.evcharging.iot.gateway;

import io.netty.channel.Channel;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceChannelRegistry {
    private final Map<String, Channel> channels = new ConcurrentHashMap<>();

    public void register(long tenantId, String deviceId, Channel channel) {
        Channel previous = channels.put(key(tenantId, deviceId), channel);
        if (previous != null && previous != channel && previous.isActive()) {
            previous.close();
        }
    }

    public Optional<Channel> find(long tenantId, String deviceId) {
        Channel channel = channels.get(key(tenantId, deviceId));
        if (channel == null || !channel.isActive()) {
            return Optional.empty();
        }
        return Optional.of(channel);
    }

    public boolean unregister(long tenantId, String deviceId, Channel channel) {
        return channels.remove(key(tenantId, deviceId), channel);
    }

    private String key(long tenantId, String deviceId) {
        return tenantId + ":" + deviceId;
    }
}
