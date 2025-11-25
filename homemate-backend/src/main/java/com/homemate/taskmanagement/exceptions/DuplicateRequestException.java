package com.homemate.taskmanagement.exceptions;

import lombok.Getter;

@Getter
public class DuplicateRequestException extends RuntimeException {

    public DuplicateRequestException() {
        super("You already have a pending request with this Tasker");
    }
}
