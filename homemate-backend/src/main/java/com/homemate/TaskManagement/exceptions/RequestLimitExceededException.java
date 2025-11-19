package com.homemate.TaskManagement.exceptions;

public class RequestLimitExceededException extends RuntimeException {
    public RequestLimitExceededException() {
        super("Maximum pending requests limit (10) reached");
    }
}
