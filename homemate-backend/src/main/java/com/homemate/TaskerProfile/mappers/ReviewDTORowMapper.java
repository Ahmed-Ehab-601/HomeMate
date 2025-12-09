package com.homemate.TaskerProfile.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.TaskerProfile.DTO.ReviewDTO;

@Component("taskerProfileReviewDTORowMapper")
public class ReviewDTORowMapper implements RowMapper<ReviewDTO> {

    @Override
    public ReviewDTO mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        ReviewDTO review = new ReviewDTO();
        review.setReviewId(rs.getInt("reviewID"));
        review.setText(rs.getString("text"));
        review.setRate(rs.getDouble("rate"));
        review.setTime(rs.getTimestamp("time"));
        review.setTaskId(rs.getInt("taskID"));
        review.setReviewerUsername(rs.getString("reviewerUsername"));
       
        return review;
    }
}
