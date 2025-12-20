package com.homemate.taskmanagement.mappers;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class TaskerBusyTimeMapper implements ResultSetExtractor<Map<LocalDateTime,LocalTime>> {
    @Override
    public Map<LocalDateTime, LocalTime> extractData(ResultSet rs) throws SQLException {
        Map<LocalDateTime, LocalTime> busyTimes = new HashMap<>();

        while (rs.next()) {
            LocalDateTime startDate = rs.getTimestamp("startDate").toLocalDateTime();
            LocalTime estimation = rs.getTime("estimation").toLocalTime();
            busyTimes.put(startDate, estimation);
        }

        return busyTimes;
    }
}
