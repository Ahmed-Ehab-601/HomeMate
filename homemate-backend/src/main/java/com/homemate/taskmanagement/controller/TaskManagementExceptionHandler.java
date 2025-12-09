package com.homemate.taskmanagement.controller;

import com.homemate.taskmanagement.exceptions.*;
import com.homemate.taskmanagement.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class TaskManagementExceptionHandler {

    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<?> handleDuplicateRequest(DuplicateRequestException ex) {
        return ResponseEntity.status(400).body(Map.of(
                "error", Error.DuplicateRequest,
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(RequestLimitExceededException.class)
    public ResponseEntity<?> handleLimitExceeded(RequestLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                "error", Error.RequestLimitExceeded,
                "message", ex.getMessage()
        ));
    }
    @ExceptionHandler(BadTaskRequestException.class)
    public ResponseEntity<?> handleBadTaskRequest(BadTaskRequestException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", Error.BadTaskRequestError,
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(BadAcceptRejectException.class)
    public ResponseEntity<?> handleBadAcceptRejectException(BadAcceptRejectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", Error.BadAcceptRejectError,
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(TaskNotFoundException.class)
    public ResponseEntity<?> handleTaskNotFoundException(TaskNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", Error.TaskNotFoundError,
                "message", ex.getMessage()
        ));
    }
    @ExceptionHandler(BadStateUpdateException.class)
    public ResponseEntity<?> handleBadStateUpdateException(BadStateUpdateException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", Error.BadStateUpdateException,
                "message", ex.getMessage()
        ));
    }
    @ExceptionHandler(BadRescheduleException.class)
    public ResponseEntity<?> handleBadReschedule(BadRescheduleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", Error.BadReschedule,
                "message", ex.getMessage()
        ));
    }
    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<?> handleReviewNotFound(ReviewNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", Error.ReviewNotFound,
                "message", ex.getMessage()
        ));
    }



}
