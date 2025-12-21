package com.homemate.TaskerProfile.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.homemate.TaskerProfile.DTO.ReviewImageDTO;

@Component
public class ReviewImageDTORowMapper implements RowMapper<ReviewImageDTO> {

    @Override
    public ReviewImageDTO mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
        ReviewImageDTO image = new ReviewImageDTO();
        image.setImgId(rs.getInt("imageID"));
        image.setFormat(rs.getString("format"));
        image.setImgFile(rs.getString("imageFile"));
        image.setImgName(rs.getString("imageName"));
        return image;
    }
}
