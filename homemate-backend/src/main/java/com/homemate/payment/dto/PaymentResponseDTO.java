package com.homemate.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResponseDTO {
    private String clientSecret;
}
