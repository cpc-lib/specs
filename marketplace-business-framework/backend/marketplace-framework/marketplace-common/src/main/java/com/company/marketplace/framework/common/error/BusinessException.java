package com.company.marketplace.framework.common.error;

public final class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    public BusinessException(ErrorCode errorCode) { super(errorCode.message()); this.errorCode = errorCode; }
    public ErrorCode errorCode() { return errorCode; }
}
