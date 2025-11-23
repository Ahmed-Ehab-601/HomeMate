package com.homemate.TaskerProfile.Dao;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.TaskerProfile.DTO.ReviewImageDTO;
import com.homemate.TaskerProfile.mappers.ReviewDTORowMapper;
import com.homemate.TaskerProfile.mappers.ReviewImageDTORowMapper;

@Repository
public class ReviewDao {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewDTORowMapper reviewDTORowMapper;
    private final ReviewImageDTORowMapper reviewImageDTORowMapper;

    public ReviewDao(JdbcTemplate jdbcTemplate,
                     ReviewDTORowMapper reviewDTORowMapper,
                     ReviewImageDTORowMapper reviewImageDTORowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewDTORowMapper = reviewDTORowMapper;
        this.reviewImageDTORowMapper = reviewImageDTORowMapper;
    }

    public ReviewDTO getReviewById(int reviewId) {
        String sql = "SELECT r.reviewID, r.text, r.rate, r.time, r.taskID " +
                     "FROM Reviews r WHERE r.reviewID = ?";
        return jdbcTemplate.queryForObject(sql, reviewDTORowMapper, reviewId);
    }

    public List<ReviewDTO> getReviewsByTaskerId(int taskerId) {
        String sql = "SELECT r.reviewID, r.text, r.rate, r.time, r.taskID " +
                     "FROM Reviews r " +
                     "JOIN Task t ON r.taskID = t.taskID " +
                     "WHERE t.taskerID = ?";

        List<ReviewDTO> reviews = jdbcTemplate.query(sql, reviewDTORowMapper, taskerId);

        
        for (ReviewDTO review : reviews) {
            List<ReviewImageDTO> images = getReviewImages(review.getReviewId());
            review.setReviewImages(images);
        }
        return reviews;
    }

    public Long getNumberOfReviews(Long id) {
        String sql = "SELECT COUNT(*) " +
                     "FROM Reviews r JOIN Task t ON r.taskID = t.taskID " +
                     "WHERE t.taskerID = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, id);
        return count != null ? count : 0L;
    }

    public List<ReviewDTO> getTaskerReviews(Long id) {
        String sql = "SELECT r.reviewID, r.text, r.rate, r.time, r.taskID " +
                     "FROM Reviews r JOIN Task t ON r.taskID = t.taskID " +
                     "WHERE t.taskerID = ? ORDER BY r.time DESC";

        List<ReviewDTO> reviews = jdbcTemplate.query(sql, reviewDTORowMapper, id);

        for (ReviewDTO review : reviews) {
            List<ReviewImageDTO> images = getReviewImages(review.getReviewId());
            review.setReviewImages(images);
        }
        return reviews;
    }

    public List<ReviewDTO> getTaskerReviewsPaginated(Long id, int offset, int limit) {
        String sql = "SELECT r.reviewID, r.text, r.rate, r.time, r.taskID " +
                     "FROM Reviews r JOIN Task t ON r.taskID = t.taskID " +
                     "WHERE t.taskerID = ? ORDER BY r.time DESC LIMIT ? OFFSET ?";

        List<ReviewDTO> reviews = jdbcTemplate.query(sql, reviewDTORowMapper, id, limit, offset);

        for (ReviewDTO review : reviews) {
            List<ReviewImageDTO> images = getReviewImages(review.getReviewId());
            review.setReviewImages(images);
        }
        return reviews;
    }

    private List<ReviewImageDTO> getReviewImages(int reviewId) {
        String sql = "SELECT imgId, format, ImgFile, ImgName " +
                     "FROM Review_Image WHERE review_id = ?";
        return jdbcTemplate.query(sql, reviewImageDTORowMapper, reviewId);
    }
}
