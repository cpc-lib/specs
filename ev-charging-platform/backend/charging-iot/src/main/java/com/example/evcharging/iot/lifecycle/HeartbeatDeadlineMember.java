package com.example.evcharging.iot.lifecycle;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record HeartbeatDeadlineMember(long tenantId, String deviceId, String leaseValue) {
    public HeartbeatDeadlineMember {
        if (tenantId <= 0) throw new IllegalArgumentException("tenantId required");
        if (deviceId == null || deviceId.isBlank()) throw new IllegalArgumentException("deviceId required");
        if (leaseValue == null || leaseValue.isBlank()) throw new IllegalArgumentException("leaseValue required");
        if (deviceId.contains("\t")) throw new IllegalArgumentException("deviceId contains tab");
    }

    public String encode() {
        String lease = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(leaseValue.getBytes(StandardCharsets.UTF_8));
        return tenantId + "\t" + deviceId + "\t" + lease;
    }

    public static HeartbeatDeadlineMember parse(String encoded) {
        if (encoded == null || encoded.isBlank()) throw new IllegalArgumentException("deadline member required");
        String[] p = encoded.split("\t", 3);
        if (p.length != 3) throw new IllegalArgumentException("invalid deadline member");
        long tenant = Long.parseLong(p[0]);
        String lease = new String(Base64.getUrlDecoder().decode(p[2]), StandardCharsets.UTF_8);
        return new HeartbeatDeadlineMember(tenant, p[1], lease);
    }
}
