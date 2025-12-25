package com.homemate.payment.exceptions;

public class StripeOperationException extends RuntimeException {
    public StripeOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
