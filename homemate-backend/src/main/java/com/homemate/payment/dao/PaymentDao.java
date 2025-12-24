package com.homemate.payment.dao;
import com.homemate.payment.model.Payment;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentDao {

    private final JdbcTemplate jdbcTemplate;
    public Optional<Long> create(Payment payment) {

        String sql = """
            INSERT INTO payments
            (taskId, userId, taskerId, totalAmount, platformFee, taskerAmount)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(con -> {
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, payment.getTaskId());
                ps.setLong(2, payment.getUserId());
                ps.setLong(3, payment.getTaskerId());
                ps.setDouble(4, payment.getTotalAmount());
                ps.setDouble(5, payment.getPlatformFee());
                ps.setDouble(6, payment.getTaskerAmount());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            return key != null ? Optional.of(key.longValue()) : Optional.empty();

        } catch (DataAccessException e) {
            throw new BadTaskRequestException();
        }
    }

    public void attachStripeIntent(Long paymentId, String intentId) {
        jdbcTemplate.update("""
            UPDATE payments
            SET stripePaymentIntentId = ?, status = 'REQUIRES_PAYMENT'
            WHERE paymentID = ?
        """, intentId, paymentId);
    }

      public void markPaid(Long paymentId) {
        jdbcTemplate.update("""
            UPDATE payments
            SET status = 'PAID', paid_at = NOW()
            WHERE paymentID = ?
        """, paymentId);
    }

    public void updateStatus(Long paymentId, String status) {
        jdbcTemplate.update("""
            UPDATE payments
            SET status = ?
            WHERE paymentID = ?
        """, status, paymentId);
    }
    public void setPaidAt(Long paymentId) {
        jdbcTemplate.update("""
            UPDATE payments
            SET paid_at = NOW()
            WHERE paymentID = ?
        """, paymentId);
    }


    public Payment getByStripeIntentId(String intentId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM payments WHERE stripePaymentIntentId = ?",
                    new PaymentRowMapper(),
                    intentId
            );

        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Payment not found for Stripe Intent ID: " + intentId);
        }
    }

    public String getTaskerStripeAccountId(Long taskerID) {
        String sql = "SELECT stripe_account_id FROM Tasker WHERE taskerID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, taskerID);

        } catch (EmptyResultDataAccessException e) {
            throw new BadStateUpdateException("Tasker ID not correct");
        }
    }

    public Long getTaskerID(Long taskID) {
        String sql = "SELECT taskerID FROM Task WHERE taskID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, taskID);

        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task ID not found: " + taskID);
        }
    }

    public Long getUserID(Long taskID) {
        String sql = "SELECT userID FROM Task WHERE taskID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Long.class, taskID);

        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task ID not found: " + taskID);
        }
    }


    public Double getTaskBill(Long taskID) {
        String sql = "SELECT bill FROM Task WHERE taskID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Double.class, taskID);

        } catch (EmptyResultDataAccessException e) {
            throw new TaskNotFoundException("Task id not found");
        }
    }

    public Payment getById(Long paymentId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM payments WHERE paymentID = ?",
                    new PaymentRowMapper(),
                    paymentId
            );

        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("Payment not found with ID: " + paymentId);
        }
    }
}
