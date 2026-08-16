package com.example.evcharging.framework.context;

import com.example.evcharging.framework.security.AccessPrincipal;

import java.util.OptionalLong;

public final class RequestContext {
    private static final ThreadLocal<State> LOCAL = new ThreadLocal<>();

    private RequestContext() {}

    public static void set(Long tenantId, String requestId) {
        set(tenantId, null, requestId);
    }

    public static void set(Long tenantId, Long userId, String requestId) {
        LOCAL.set(new State(tenantId, userId, requestId, null));
    }

    public static void set(AccessPrincipal principal, String requestId) {
        LOCAL.set(new State(principal.tenantId(), principal.userId(), requestId, principal));
    }

    public static Long tenantId() {
        State state = LOCAL.get();
        return state == null ? null : state.tenantId();
    }

    public static long requireTenantId() {
        Long tenantId = tenantId();
        if (tenantId == null || tenantId <= 0) throw new IllegalStateException("tenant context is missing");
        return tenantId;
    }

    public static OptionalLong currentUserId() {
        State state = LOCAL.get();
        if (state == null || state.userId() == null || state.userId() <= 0) return OptionalLong.empty();
        return OptionalLong.of(state.userId());
    }

    public static long requireUserId() {
        return currentUserId().orElseThrow(() -> new IllegalStateException("user context is missing"));
    }

    public static AccessPrincipal principal() {
        State state = LOCAL.get();
        return state == null ? null : state.principal();
    }

    public static AccessPrincipal requirePrincipal() {
        AccessPrincipal principal = principal();
        if (principal == null) throw new SecurityException("authenticated principal required");
        return principal;
    }

    public static String requestId() {
        State state = LOCAL.get();
        return state == null ? null : state.requestId();
    }

    public static void clear() { LOCAL.remove(); }

    private record State(Long tenantId, Long userId, String requestId, AccessPrincipal principal) {}
}
