package com.homemate.reviews.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.homemate.reviews.DTO.ReviewDTO;

@Component
public class ReviewDTORowMapper implements RowMapper<ReviewDTO> {
    @Override
    public ReviewDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewDTO review = new ReviewDTO();
        review.setReviewId(rs.getInt("reviewID"));
        review.setText(rs.getString("text"));
        review.setRate(rs.getDouble("rate"));
        review.setTime(rs.getTimestamp("time"));
        review.setTaskId(rs.getInt("taskID"));
        return review;
    }
}
