package com.example.evcharging.framework.security;

import java.util.Set;

public record AccessPrincipal(
        long tenantId,
        long userId,
        String username,
        Set<String> roles,
        Set<String> permissions,
        DataScopeType dataScopeType,
        Set<Long> stationIds
) {
    public AccessPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        permissions = permissions == null ? Set.of() : Set.copyOf(permissions);
        stationIds = stationIds == null ? Set.of() : Set.copyOf(stationIds);
        dataScopeType = dataScopeType == null ? DataScopeType.TENANT : dataScopeType;
    }

    public boolean hasRole(String role) {
        return role != null && roles.stream().anyMatch(x -> x.equalsIgnoreCase(role));
    }

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) return true;
        return permissions.contains("*") || permissions.contains(permission);
    }

    public boolean mayAccessStation(long stationId) {
        return switch (dataScopeType) {
            case ALL, TENANT -> true;
            case STATION -> stationIds.contains(stationId);
            case SELF -> false;
        };
    }
}
