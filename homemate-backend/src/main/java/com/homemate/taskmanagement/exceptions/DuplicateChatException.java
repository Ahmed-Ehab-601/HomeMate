package com.homemate.taskmanagement.exceptions;

public class DuplicateChatException extends RuntimeException{
    public DuplicateChatException() {
        super("Chat is Duplicate");
    }
}

