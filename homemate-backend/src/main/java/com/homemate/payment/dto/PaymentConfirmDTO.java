package com.homemate.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Used when frontend confirms payment succeeded
 * Frontend sends this after Stripe.confirmCardPayment() succeeds
 */
@Data
public class PaymentConfirmDTO {

    @NotBlank(message = "Payment Intent ID is required")
    private String paymentIntentId;

    // Optional: Frontend can send these for logging
    private String status;
    private Long paymentId;
}