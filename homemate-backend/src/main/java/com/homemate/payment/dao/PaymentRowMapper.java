package com.homemate.payment.dao;

import com.homemate.payment.model.Payment;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Maps database rows to Payment objects
 */
public class PaymentRowMapper implements RowMapper<Payment> {

    @Override
    public Payment mapRow(ResultSet rs, int rowNum) throws SQLException {
        Payment payment = new Payment();

        payment.setId(rs.getLong("paymentID"));
        payment.setTaskId(rs.getLong("taskId"));
        payment.setUserId(rs.getLong("userId"));
        payment.setTaskerId(rs.getLong("taskerId"));

        payment.setTotalAmount(rs.getDouble("totalAmount"));
        payment.setPlatformFee(rs.getDouble("platformFee"));
        payment.setTaskerAmount(rs.getDouble("taskerAmount"));

        payment.setStripePaymentIntentId(rs.getString("stripePaymentIntentId"));
        payment.setStatus(rs.getString("status"));

        // Handle timestamps (may be null)
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            payment.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp paidAt = rs.getTimestamp("paid_at");
        if (paidAt != null) {
            payment.setPaidAt(paidAt.toLocalDateTime());
        }

        return payment;
    }
}