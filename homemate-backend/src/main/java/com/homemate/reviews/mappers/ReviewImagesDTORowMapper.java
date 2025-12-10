package com.homemate.reviews.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.homemate.reviews.DTO.ReviewImagesDTO;

@Component
public class ReviewImagesDTORowMapper implements RowMapper<ReviewImagesDTO> {
    @Override
    public ReviewImagesDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        ReviewImagesDTO image = new ReviewImagesDTO();
        image.setImgId(rs.getInt("imageID"));
        image.setFormat(rs.getString("format"));
        image.setImgFile(rs.getBytes("imageFile"));
        image.setImgName(rs.getString("imageName"));
        return image;
    }
}
