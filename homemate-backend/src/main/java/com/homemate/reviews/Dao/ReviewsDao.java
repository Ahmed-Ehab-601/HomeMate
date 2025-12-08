package com.homemate.reviews.Dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.homemate.reviews.DTO.ReviewDTO;
import com.homemate.reviews.DTO.ReviewImagesDTO;
import com.homemate.reviews.mappers.ReviewDTORowMapper;
import com.homemate.reviews.mappers.ReviewImagesDTORowMapper;

@Repository
public class ReviewsDao {

    private final JdbcTemplate jdbcTemplate;
    private final ReviewDTORowMapper reviewDTORowMapper;
    private final ReviewImagesDTORowMapper reviewImagesDTORowMapper;

    public ReviewsDao(JdbcTemplate jdbcTemplate,
                      ReviewDTORowMapper reviewDTORowMapper,
                      ReviewImagesDTORowMapper reviewImagesDTORowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewDTORowMapper = reviewDTORowMapper;
        this.reviewImagesDTORowMapper = reviewImagesDTORowMapper;
    }

    public ReviewDTO addReview(ReviewDTO reviewDTO) {
        String sql = "INSERT INTO Reviews (text, rate, taskID) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, reviewDTO.getText());
            ps.setDouble(2, reviewDTO.getRate());
            ps.setInt(3, reviewDTO.getTaskId());
            return ps;
        }, keyHolder);

        int reviewId = keyHolder.getKey().intValue();
        reviewDTO.setReviewId(reviewId);

        if (reviewDTO.getReviewImages() != null) {
            for (ReviewImagesDTO img : reviewDTO.getReviewImages()) {
                addReviewImage(img, reviewId);
            }
        }
        return getReviewById(reviewId);
    }

    private void addReviewImage(ReviewImagesDTO img, int reviewId) {
        String sql = "INSERT INTO ReviewImage (format, imageFile, imageName, reviewID) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, img.getFormat(), img.getImgFile(), img.getImgName(), reviewId);
    }

    public ReviewDTO getReviewByTask(int taskId) {
        String sql = "SELECT * FROM Reviews WHERE taskID = ?";
        List<ReviewDTO> reviews = jdbcTemplate.query(sql, reviewDTORowMapper, taskId);
        if (reviews.isEmpty()) {
            return null;
        }
        ReviewDTO review = reviews.get(0);
        review.setReviewImages(getReviewImages(review.getReviewId()));
        return review;
    }

    public ReviewDTO getReviewById(int reviewId) {
        String sql = "SELECT * FROM Reviews WHERE reviewID = ?";
        List<ReviewDTO> reviews = jdbcTemplate.query(sql, reviewDTORowMapper, reviewId);
        if (reviews.isEmpty()) {
            return null;
        }
        ReviewDTO review = reviews.get(0);
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

   public void updateReview(ReviewDTO reviewDTO) {
    // Update review text and rate
        String sql = "UPDATE Reviews SET text = ?, rate = ? WHERE reviewID = ?";
        jdbcTemplate.update(sql,
                reviewDTO.getText(),
                reviewDTO.getRate(),
                reviewDTO.getReviewId()
        );

        // Add NEW images only (imgId == 0)
        if (reviewDTO.getReviewImages() != null) {
            for (ReviewImagesDTO img : reviewDTO.getReviewImages()) {
                if (img.getImgId() == 0) {
                    addReviewImage(img, reviewDTO.getReviewId());
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
}
