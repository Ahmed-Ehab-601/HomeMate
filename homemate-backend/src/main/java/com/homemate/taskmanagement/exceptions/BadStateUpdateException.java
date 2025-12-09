package com.homemate.taskmanagement.exceptions;

public class BadStateUpdateException extends RuntimeException {
    public BadStateUpdateException(String message) {
        super(message);
    }
}
