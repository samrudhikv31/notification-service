package com.notification.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException ex) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (ex.getMessage().contains("not found") ||
                ex.getMessage().contains("No rule")) {
            status = HttpStatus.NOT_FOUND;
        }

        if (ex.getMessage().contains("Invalid API key") ||
                ex.getMessage().contains("disabled")) {
            status = HttpStatus.UNAUTHORIZED;
        }

        if (ex.getMessage().contains("already registered")) {
            status = HttpStatus.CONFLICT;
        }

        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(ex.getMessage())
                        .timestamp(LocalDateTime.now())
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(500)
                        .error("Internal Server Error")
                        .message("Something went wrong.")
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}