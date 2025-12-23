package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class TaskAnalysisDao {

    private final JdbcTemplate jdbcTemplate;

    private String TABLE_NAME = "Task";
    private String TASK_BILL = "bill";

    private String generateRangeSQL(int[][] ranges, String[] labels) {
        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < ranges.length; i++) {
            if (i > 0) sql.append(", ");
            int start = ranges[i][0];
            int end = ranges[i][1];
            if (end == -1) {
                sql.append("SUM(CASE WHEN ").append(TASK_BILL).append(" >= ").append(start)
                   .append(" THEN 1 ELSE 0 END) AS ").append(labels[i]);
            } else {
                sql.append("SUM(CASE WHEN ").append(TASK_BILL).append(" >= ").append(start)
                   .append(" AND ").append(TASK_BILL).append(" < ").append(end)
                   .append(" THEN 1 ELSE 0 END) AS ").append(labels[i]);
            }
        }
        sql.append(" FROM ").append(TABLE_NAME);
        return sql.toString();
    }

    public TaskDateRangesResponse fetchStartDateRanges(TaskDateRangesRequest request) {
        List<TimeRange> ranges = request.getRanges();
        String sql = "SELECT COUNT(*) FROM Task WHERE startDate >= ? AND startDate < ?";
        return new TaskDateRangesResponse(
                ranges.stream().map(r -> {
                    Timestamp from = Timestamp.valueOf(r.getFrom());
                    Timestamp to = Timestamp.valueOf(r.getTo());
                    return jdbcTemplate.queryForObject(sql, Long.class, from, to);
                }).toList()
        );
    }

    public TaskDateRangesResponse fetchEndDateRanges(TaskDateRangesRequest request) {
        List<TimeRange> ranges = request.getRanges();
        String sql = "SELECT COUNT(*) FROM Task WHERE endDate >= ? AND endDate < ?";
        return new TaskDateRangesResponse(
                ranges.stream().map(r -> {
                    Timestamp from = Timestamp.valueOf(r.getFrom());
                    Timestamp to = Timestamp.valueOf(r.getTo());
                    return jdbcTemplate.queryForObject(sql, Long.class, from, to);
                }).toList()
        );
    }

    public BillRangesResponse fetchBillRanges() {
        int step = 10;
        int maxValue = 200;
        int numRanges = (maxValue / step) + 1;
        
        int[][] ranges = new int[numRanges][2];
        String[] labels = new String[numRanges];
        
        for (int i = 0; i < numRanges - 1; i++) {
            int start = i * step;
            int end = start + step;
            ranges[i][0] = start;
            ranges[i][1] = end;
            labels[i] = "bill_" + start + "_" + end;
        }
        
        ranges[numRanges - 1][0] = maxValue;
        ranges[numRanges - 1][1] = -1;
        labels[numRanges - 1] = "bill_" + maxValue + "_plus";
        
        String sql = generateRangeSQL(ranges, labels);

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> billRanges = new HashMap<>();
            if (rs.next()) {
                for (int i = 0; i < numRanges - 1; i++) {
                    int start = i * step;
                    int end = start + step;
                    billRanges.put(start + "-" + end, rs.getLong(labels[i]));
                }
                billRanges.put(maxValue + "+", rs.getLong(labels[numRanges - 1]));
            }
            return new BillRangesResponse(billRanges);
        });
    }

    public TaskStatusCountsResponse fetchStatusCounts() {
        long inReview = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Task WHERE status = 'InReview'",
                Long.class
        );
        long accepted = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Task WHERE status = 'Accepted'",
                Long.class
        );
        long inProgress = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Task WHERE status = 'InProgress'",
                Long.class
        );
        long suspended = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Task WHERE status = 'Suspended'",
                Long.class
        );
        long done = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Task WHERE status = 'Done'",
                Long.class
        );
        long rejected = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Task WHERE status = 'Rejected'",
                Long.class
        );
        return new TaskStatusCountsResponse(inReview, accepted, inProgress, suspended, done, rejected);
    }
}
