package com.homemate.taskmanagement.exceptions;

public class BadTaskRequestException extends RuntimeException {
    public BadTaskRequestException() {
        super("Task Not Created Correctly");
    }
}


