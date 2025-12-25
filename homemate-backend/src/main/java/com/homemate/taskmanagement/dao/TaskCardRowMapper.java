package com.homemate.taskmanagement.dao;

import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class TaskCardRowMapper implements RowMapper<TaskCardDto> {
    @Override
    public TaskCardDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        boolean userUnread = rs.getObject("userUnreadMessages") != null && rs.getInt("userUnreadMessages") > 0;
        boolean taskerUnread = rs.getObject("taskerUnreadMessages") != null && rs.getInt("taskerUnreadMessages") > 0;
        return TaskCardDto.builder()
                .taskID(rs.getLong("taskID"))
                .startDate(toLocalDateTime(rs.getTimestamp("startDate")))
                .status(StatusDto.valueOf(rs.getString("status")))
                .userName(rs.getString("userName"))
                .taskerName(rs.getString("taskerName"))
                .serviceName(rs.getString("serviceName"))
                .addressCity(rs.getString("city"))
                .estimation(rs.getInt("estimation"))
                .userHasUnreadMessages(userUnread)
                .taskerHasUnreadMessages(taskerUnread)
                .paid(rs.getBoolean("paid"))
                .build();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}


