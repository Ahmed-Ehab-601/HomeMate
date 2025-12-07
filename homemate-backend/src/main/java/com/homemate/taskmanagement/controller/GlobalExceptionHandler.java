package com.homemate.taskmanagement.controller;

import com.homemate.taskmanagement.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<?> handleDuplicateRequest(DuplicateRequestException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "error", "DUPLICATE_REQUEST",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(RequestLimitExceededException.class)
    public ResponseEntity<?> handleLimitExceeded(RequestLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "error", "REQUEST_LIMIT_EXCEEDED",
                "message", ex.getMessage()
        ));
    }
    @ExceptionHandler(BadTaskRequestException.class)
    public ResponseEntity<?> handleBadTaskRequest(BadTaskRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "Task Not Created",
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(BadAcceptRejectException.class)
    public ResponseEntity<?> handleBadAcceptRejectException(BadAcceptRejectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<?> handleTaskNotFoundException(TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", ex.getMessage(),
                "message", ex.getMessage()
        ));
    }

}
