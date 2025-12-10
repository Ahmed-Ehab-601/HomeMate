package com.homemate.reviews.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.homemate.reviews.DTO.ReviewsDTO;

@Component("reviewsReviewsDTORowMapper")
public class ReviewsDTORowMapper implements RowMapper<ReviewsDTO> {
    @Override
    public ReviewsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewsDTO review = new ReviewsDTO();
        review.setReviewId(rs.getInt("reviewID"));
        review.setText(rs.getString("text"));
        review.setRate(rs.getDouble("rate"));
        review.setTime(rs.getTimestamp("time"));
        review.setTaskId(rs.getInt("taskID"));
        return review;
    }
}
