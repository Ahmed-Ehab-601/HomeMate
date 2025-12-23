package com.homemate.taskmanagement.dao;

import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.model.Status;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class TaskRowMapper implements RowMapper<TaskDto> {

    @Override
    public TaskDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return TaskDto.builder().
                taskID(rs.getLong("taskID")).
                startDate(toLocalDateTime(rs.getTimestamp("startDate"))).
                endDate(toLocalDateTime(rs.getTimestamp("endDate"))).
                status(Status.valueOf(rs.getString("status"))).
                description(rs.getString("description")).
                workedHours(rs.getDouble("workedHours")).
                startInProgress(toLocalDateTime(rs.getTimestamp("startInProgress"))).
                bill(rs.getDouble("bill")).
                userName(rs.getString("userName")).
                taskerName(rs.getString("taskerName")).
                serviceName(rs.getString("serviceName")).
                chatID(rs.getLong("chatID")).
                addressDetails(buildAddress(rs)).
                userMail(rs.getString("userMail")).
                taskerMail(rs.getString("taskerMail")).
                taskerID(rs.getLong("taskerID")).
                hourRate(rs.getDouble("hourRate")).
                estimation(rs.getInt("estimation")).
                paid(rs.getBoolean("paid"))
                .build();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp){
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
    private String buildAddress(ResultSet rs) throws SQLException {
        StringBuilder address = new StringBuilder();

        String apartment = rs.getString("apartment");
        if (apartment != null && !apartment.trim().isEmpty()) {
            address.append(apartment).append(", ");
        }

        address.append(rs.getString("street")).append(", ")
                .append(rs.getString("city")).append(", ")
                .append(rs.getString("country"));

        return address.toString();
    }

}
