package com.homemate.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    @NotNull
    private Long taskId;

    @NotNull
    private Long taskerId;

    @NotNull
    private Long userId;


    @NotNull
    private double bill;
}
