package com.smartqueue.common.exception;

import com.smartqueue.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                404,
                "RESOURCE_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI(),
                Instant.now(),
                Map.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        for (var error : exception.getBindingResult().getFieldErrors()) {
            fieldErrors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                    .add(error.getDefaultMessage());
        }
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Invalid request data",
                request.getRequestURI(),
                Instant.now(),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }
}
