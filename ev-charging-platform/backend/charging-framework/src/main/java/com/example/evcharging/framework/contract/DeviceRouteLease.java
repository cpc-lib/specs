package com.example.evcharging.framework.contract;

public record DeviceRouteLease(String gatewayId, String connectionToken) {
    public DeviceRouteLease {
        if (gatewayId == null || gatewayId.isBlank()) throw new IllegalArgumentException("gatewayId required");
        if (connectionToken == null || connectionToken.isBlank()) throw new IllegalArgumentException("connectionToken required");
        if (gatewayId.contains("|")) throw new IllegalArgumentException("gatewayId must not contain pipe");
        if (connectionToken.contains("|")) throw new IllegalArgumentException("connectionToken must not contain pipe");
    }

    public String encode() {
        return gatewayId + "|" + connectionToken;
    }

    public static DeviceRouteLease parse(String encoded) {
        if (encoded == null || encoded.isBlank()) throw new IllegalArgumentException("device route lease missing");
        int separator = encoded.indexOf('|');
        if (separator <= 0 || separator == encoded.length() - 1 || encoded.indexOf('|', separator + 1) >= 0) {
            throw new IllegalArgumentException("invalid device route lease");
        }
        return new DeviceRouteLease(encoded.substring(0, separator), encoded.substring(separator + 1));
    }
}
