package com.homemate.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {

    private Long id;
    private Long taskId;
    private Long userId;
    private Long taskerId;

    private Double totalAmount;
    private Double platformFee;
    private Double taskerAmount;

    private String stripePaymentIntentId;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

}
