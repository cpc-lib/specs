package com.example.evcharging.framework.error;

public final class ChargingErrorCodes {
    private ChargingErrorCodes() {}
    public static final ErrorCode ACTIVE_SESSION_EXISTS = new ErrorCode(400001, "active session exists");
    public static final ErrorCode SESSION_NOT_FOUND = new ErrorCode(400002, "session not found");
    public static final ErrorCode ILLEGAL_SESSION_STATE = new ErrorCode(400003, "illegal session state");
}
