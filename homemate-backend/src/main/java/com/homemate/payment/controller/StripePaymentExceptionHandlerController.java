package com.homemate.payment.controller;

import com.homemate.payment.exceptions.BadRequestException;
import com.homemate.payment.exceptions.PaymentException;
import com.homemate.payment.exceptions.ResourceNotFoundException;
import com.homemate.payment.exceptions.StripeOperationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class StripePaymentExceptionHandlerController {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(StripeOperationException.class)
    public ResponseEntity<String> handleStripeError(StripeOperationException ex) {
        return ResponseEntity.status(502).body(ex.getMessage());
    }
    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<String> handlePayment(Exception ex) {
        return ResponseEntity.status(500).body(ex.getMessage());
    }
}
