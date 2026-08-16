package com.example.evcharging.framework.security;

import com.example.evcharging.framework.context.RequestContext;

public final class DataScopeGuard {
    private DataScopeGuard() {}

    public static void requireStation(long stationId) {
        AccessPrincipal principal = RequestContext.requirePrincipal();
        if (!principal.mayAccessStation(stationId)) {
            throw new SecurityException("station is outside current data scope");
        }
    }

    public static boolean allowsStation(long stationId) {
        AccessPrincipal principal = RequestContext.principal();
        return principal != null && principal.mayAccessStation(stationId);
    }
}
