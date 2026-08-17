package com.company.marketplace.framework.web;

import com.company.marketplace.framework.common.api.ApiResponse;
import com.company.marketplace.framework.common.error.BusinessException;
import org.springframework.http.HttpStatus; import org.springframework.validation.BindException; import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public final class GlobalExceptionHandler {
 @ExceptionHandler(BusinessException.class) @ResponseStatus(HttpStatus.CONFLICT)
 ApiResponse<Void> business(BusinessException e){ return ApiResponse.failure(e.errorCode().code(), e.errorCode().message(), TraceContext.traceId()); }
 @ExceptionHandler(BindException.class) @ResponseStatus(HttpStatus.BAD_REQUEST)
 ApiResponse<Void> validation(BindException e){ return ApiResponse.failure("VALIDATION_ERROR", e.getAllErrors().isEmpty()?"Validation failed":e.getAllErrors().getFirst().getDefaultMessage(), TraceContext.traceId()); }
 @ExceptionHandler(Exception.class) @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
 ApiResponse<Void> unknown(Exception e){ return ApiResponse.failure("INTERNAL_ERROR", "Internal server error", TraceContext.traceId()); }
}
