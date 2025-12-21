package com.homemate.payment.mapper;

import com.homemate.payment.dto.PaymentRequestDTO;
import com.homemate.payment.model.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public Payment getPaymnetEntity(PaymentRequestDTO paymentRequestDTO) {
        return Payment.builder()
                .taskId(paymentRequestDTO.getTaskId())
                .taskerId(paymentRequestDTO.getTaskerId())  // ✅ FIXED: Was using getTaskId()
                .userId(paymentRequestDTO.getUserId())
                .totalAmount(paymentRequestDTO.getBill())
                .platformFee(paymentRequestDTO.getBill() * 0.1)  // 10% platform fee
                .taskerAmount(paymentRequestDTO.getBill() * 0.9)  // 90% to tasker
                .build();
    }
}