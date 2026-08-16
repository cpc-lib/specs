package com.example.evcharging.framework.webmvc;

import com.example.evcharging.framework.api.ApiResponse;
import org.slf4j.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.alibaba.csp.sentinel.slots.block.BlockException;

@RestControllerAdvice
public class GlobalApiExceptionHandler {
    private static final Logger log=LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    @ExceptionHandler(BlockException.class)
    public ResponseEntity<ApiResponse<Void>> overloaded(BlockException e){
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After","1")
                .body(ApiResponse.failure(100429,"system is busy, retry later"));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> security(SecurityException e){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(100403,e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> badRequest(IllegalArgumentException e){
        return ResponseEntity.badRequest().body(ApiResponse.failure(100400,e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> conflict(IllegalStateException e){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(100409,e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> unexpected(Exception e){
        log.error("unhandled api error",e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(100500,"internal server error"));
    }
}
