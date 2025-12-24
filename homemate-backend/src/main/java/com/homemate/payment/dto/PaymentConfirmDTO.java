package com.homemate.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class PaymentConfirmDTO {

    @NotBlank(message = "Payment Intent ID is required")
    private String paymentIntentId;
    private String status;
    private Long paymentId;
}