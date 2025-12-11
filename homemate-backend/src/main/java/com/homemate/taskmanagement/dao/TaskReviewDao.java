package com.homemate.taskmanagement.dao;

import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.TaskerProfile.mappers.ReviewDTORowMapper;
import lombok.AllArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;

@AllArgsConstructor
@Component
public class TaskReviewDao {


    private final JdbcTemplate jdbcTemplate;
    private final ReviewDTORowMapper reviewDTORowMapper;


    public Optional<ReviewDTO> getReviewByTaskId(Long taskId) {
        String sql = """
                SELECT r.reviewID, r.text, r.rate, r.time, r.taskID,
                       u.username AS reviewerUsername
                FROM Reviews r
                JOIN Task t ON r.taskID = t.taskID
                JOIN Users u ON t.userID = u.userID
                WHERE r.taskID = ?
                """;
        try {
            ReviewDTO review = jdbcTemplate.queryForObject(sql, reviewDTORowMapper, taskId);
            return Optional.ofNullable(review);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();

        }
    }
}
