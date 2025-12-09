package com.homemate.reviews.Dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.DTO.ReviewImagesDTO;
import com.homemate.reviews.mappers.ReviewsDTORowMapper;
import com.homemate.reviews.mappers.ReviewImagesDTORowMapper;

@Repository
public class ReviewsDao {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewsDTORowMapper reviewsDTORowMapper;
    private final ReviewImagesDTORowMapper reviewImagesDTORowMapper;

    public ReviewsDao(JdbcTemplate jdbcTemplate,
                      ReviewsDTORowMapper reviewsDTORowMapper,
                      ReviewImagesDTORowMapper reviewImagesDTORowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewsDTORowMapper = reviewsDTORowMapper;
        this.reviewImagesDTORowMapper = reviewImagesDTORowMapper;
    }

    public ReviewsDTO addReview(ReviewsDTO reviewsDTO) {
        String sql = "INSERT INTO Reviews (text, rate, taskID) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"reviewID"});
            ps.setString(1, reviewsDTO.getText());
            ps.setDouble(2, reviewsDTO.getRate());
            ps.setInt(3, reviewsDTO.getTaskId());
            return ps;
        }, keyHolder);

        int reviewId = keyHolder.getKey().intValue();
        reviewsDTO.setReviewId(reviewId);

        if (reviewsDTO.getReviewImages() != null) {
            for (ReviewImagesDTO img : reviewsDTO.getReviewImages()) {
                addReviewImage(img, reviewId);
            }
        }
        return getReviewById(reviewId);
    }

    private void addReviewImage(ReviewImagesDTO img, int reviewId) {
        String sql = "INSERT INTO ReviewImage (format, imageFile, imageName, reviewID) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, img.getFormat(), img.getImgFile(), img.getImgName(), reviewId);
    }

    public ReviewsDTO getReviewByTask(int taskId) {
        String sql = "SELECT * FROM Reviews WHERE taskID = ?";
        List<ReviewsDTO> reviews = jdbcTemplate.query(sql, reviewsDTORowMapper, taskId);
        if (reviews.isEmpty()) {
            return null;
        }
        ReviewsDTO review = reviews.get(0);
        review.setReviewImages(getReviewImages(review.getReviewId()));
        return review;
    }

    public ReviewsDTO getReviewById(int reviewId) {
        String sql = "SELECT * FROM Reviews WHERE reviewID = ?";
        List<ReviewsDTO> reviews = jdbcTemplate.query(sql, reviewsDTORowMapper, reviewId);
        if (reviews.isEmpty()) {
            return null;
        }
        ReviewsDTO review = reviews.get(0);
        review.setReviewImages(getReviewImages(review.getReviewId()));
        return review;
    }

    private List<ReviewImagesDTO> getReviewImages(int reviewId) {
        String sql = "SELECT * FROM ReviewImage WHERE reviewID = ?";
        return jdbcTemplate.query(sql, reviewImagesDTORowMapper, reviewId);
    }

    public boolean deleteReview(int reviewId) {
        String sql = "DELETE FROM Reviews WHERE reviewID = ?";
        int rows = jdbcTemplate.update(sql, reviewId);
        return rows > 0;
    }

   public void updateReview(ReviewsDTO reviewsDTO) {
    // Update review text and rate
        String sql = "UPDATE Reviews SET text = ?, rate = ? WHERE reviewID = ?";
        jdbcTemplate.update(sql,
                reviewsDTO.getText(),
                reviewsDTO.getRate(),
                reviewsDTO.getReviewId()
        );

        // Add NEW images only (imgId == 0)
        if (reviewsDTO.getReviewImages() != null) {
            for (ReviewImagesDTO img : reviewsDTO.getReviewImages()) {
                if (img.getImgId() == 0) {
                    addReviewImage(img, reviewsDTO.getReviewId());
                }
            }
        }
    }


    public void deleteImage(int imageId) {
        String sql = "DELETE FROM ReviewImage WHERE imageID = ?";
        jdbcTemplate.update(sql, imageId);
    }

    public int getReviewImageCount(int reviewId) {
         String sql = "SELECT COUNT(*) FROM ReviewImage WHERE reviewID = ?";
         Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
         return count != null ? count : 0;
    }
    public Integer getTaskerIdByTaskId(int taskId) {
        String sql = "SELECT taskerID FROM Task WHERE taskID = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, taskId);
        } catch (Exception e) {
            return null;
        }
    }

    public Double getAverageRatingForTasker(int taskerId) {
        String sql = """
            SELECT AVG(r.rate) 
            FROM Reviews r 
            JOIN Task t ON r.taskID = t.taskID 
            WHERE t.taskerID = ?
        """;
        Double avg = jdbcTemplate.queryForObject(sql, Double.class, taskerId);
        return avg != null ? avg : 0.0;
    }

    public void updateTaskerRating(int taskerId, double rating) {
        String sql = "UPDATE Tasker SET rating = ? WHERE taskerID = ?";
        jdbcTemplate.update(sql, rating, taskerId);
    }
}
