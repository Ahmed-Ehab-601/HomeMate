package com.homemate.taskmanagement.mappers;

import com.homemate.taskmanagement.dto.TaskTimeDto;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskerBusyTimeMapper implements ResultSetExtractor<List<TaskTimeDto>> {
    @Override
    public List<TaskTimeDto> extractData(ResultSet rs) throws SQLException {
        List<TaskTimeDto> list=new ArrayList<>();
        while (rs.next()) {
          list.add(TaskTimeDto.builder().taskID(rs.getLong("taskID"))
                  .startDate(rs.getTimestamp("startDate").toLocalDateTime())
                  .estimation(rs.getInt("estimation")).build());
        }
        return list;
    }
}
