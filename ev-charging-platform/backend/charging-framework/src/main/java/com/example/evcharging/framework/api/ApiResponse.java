package com.example.evcharging.framework.api;

import com.example.evcharging.framework.context.RequestContext;

public record ApiResponse<T>(int code, String message, T data, String requestId) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "success", data, RequestContext.requestId());
    }

    /**
     * Backward-compatible alias retained for earlier vertical slices.
     * New code should prefer {@link #success(Object)}.
     */
    public static <T> ApiResponse<T> ok(T data) {
        return success(data);
    }

    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null, RequestContext.requestId());
    }
}
