package com.homemate.payment.controller;

import com.homemate.payment.dto.PaymentConfirmDTO;
import com.homemate.payment.dto.PaymentRequestDTO;
import com.homemate.payment.dto.PaymentResponseDTO;
import com.homemate.payment.service.StripePaymentService;
import com.homemate.security.model.AppUserDetails;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasker")
@AllArgsConstructor
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;

    // ==================== TASKER ACCOUNT ====================

    @PostMapping("/create-connected-account/{taskerId}")
    public ResponseEntity<String> createConnectedAccount(@PathVariable Long taskerId) {
        return ResponseEntity.ok(
                stripePaymentService.generateOnboardingLink(taskerId)
        );
    }

    // ==================== CREATE PAYMENT ====================

    @PostMapping("/payments/create")
    public ResponseEntity<PaymentResponseDTO> createPayment(
            @Valid @RequestBody PaymentRequestDTO request) {

        return ResponseEntity.ok(
                stripePaymentService.createPaymentAndReturnClientSecret(request)
        );
    }

    // ==================== CONFIRM PAYMENT ====================

    @PostMapping("/payments/confirm")
    public ResponseEntity<String> confirmPayment(
            @Valid @RequestBody PaymentConfirmDTO request) {

        stripePaymentService.confirmPayment(request.getPaymentIntentId());
        return ResponseEntity.ok("Payment confirmed successfully");
    }

    // ==================== PAYMENT STATUS ====================

    @GetMapping("/payments/{paymentId}/status")
    public ResponseEntity<String> getPaymentStatus(@PathVariable Long paymentId) {
        return ResponseEntity.ok(
                stripePaymentService.getPaymentStatus(paymentId)
        );
    }

    // ==================== CASH PAYMENT ====================

    @PostMapping("/payments/mark-paid-cash/{taskId}")
    @PreAuthorize("hasRole('TASKER')")
    public ResponseEntity<String> markPaidCash(
            @PathVariable Long taskId,
            @AuthenticationPrincipal AppUserDetails userDetails) {

        stripePaymentService.markTaskAsPaidCash(taskId, userDetails.getId());

        return ResponseEntity.ok("Task marked as paid (cash)");
    }

}
