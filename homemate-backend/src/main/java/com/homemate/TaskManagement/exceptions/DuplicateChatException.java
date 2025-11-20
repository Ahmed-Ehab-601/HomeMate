package com.homemate.TaskManagement.exceptions;

public class DuplicateChatException extends RuntimeException{
    public DuplicateChatException() {
        super("Chat is Duplicate");
    }
}

