package com.homemate.TaskManagement.exceptions;

public class BadTaskRequestException extends RuntimeException {
    public BadTaskRequestException() {
        super("Task Not Created Correctly");
    }
}


