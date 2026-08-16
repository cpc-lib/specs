package com.example.evcharging.framework.webmvc;

import com.example.evcharging.framework.api.ApiResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> validation(MethodArgumentNotValidException e){
        String message=e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error->error.getField()+" "+error.getDefaultMessage())
                .orElse("validation failed");
        return ApiResponse.failure(100001,message);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> duplicate(DuplicateKeyException e){
        return ApiResponse.failure(100002,"resource already exists");
    }
}
