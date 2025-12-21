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
public class TaskerBusyTimeMapper implements ResultSetExtractor<Map<LocalDateTime,Integer>> {
    @Override
    public Map<LocalDateTime, Integer> extractData(ResultSet rs) throws SQLException {
        Map<LocalDateTime, Integer> busyTimes = new HashMap<>();

        while (rs.next()) {
            LocalDateTime startDate = rs.getTimestamp("startDate").toLocalDateTime();
            int estimation = rs.getInt("estimation");
            busyTimes.put(startDate, estimation);
        }

        return busyTimes;
    }
}
