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
public class TaskerAnalysisDao {
    private final JdbcTemplate jdbcTemplate;

    private String generateAgeBucketsSQL(String tableName, String ageCalculation) {
        return "SELECT " +
                "SUM(CASE WHEN age BETWEEN 0 AND 9 THEN 1 ELSE 0 END) AS b_0_10, " +
                "SUM(CASE WHEN age BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS b_10_20, " +
                "SUM(CASE WHEN age BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS b_20_30, " +
                "SUM(CASE WHEN age BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS b_30_40, " +
                "SUM(CASE WHEN age BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS b_40_50, " +
                "SUM(CASE WHEN age BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS b_50_60, " +
                "SUM(CASE WHEN age >= 60 THEN 1 ELSE 0 END) AS b_60_plus " +
                "FROM (" + ageCalculation + ") " + tableName;
    }

    private String generateRangeSQL(String tableName, String columnName, int[][] ranges, String[] labels) {
        StringBuilder sql = new StringBuilder("SELECT ");
        for (int i = 0; i < ranges.length; i++) {
            if (i > 0) sql.append(", ");
            int start = ranges[i][0];
            int end = ranges[i][1];
            if (end == -1) {
                sql.append("SUM(CASE WHEN ").append(columnName).append(" >= ").append(start)
                   .append(" THEN 1 ELSE 0 END) AS ").append(labels[i]);
            } else {
                sql.append("SUM(CASE WHEN ").append(columnName).append(" >= ").append(start)
                   .append(" AND ").append(columnName).append(" < ").append(end)
                   .append(" THEN 1 ELSE 0 END) AS ").append(labels[i]);
            }
        }
        sql.append(" FROM ").append(tableName);
        return sql.toString();
    }

    public GenderCountResponse fetchGenderCounts() {
        String sql = """
                    SELECT
                    SUM(CASE WHEN gender = 'M' THEN 1 ELSE 0 END) AS male, 
                    SUM(CASE WHEN gender = 'F' THEN 1 ELSE 0 END) AS female
                    FROM Tasker
                    """;
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return new GenderCountResponse(
                    rs.getLong("male"),
                    rs.getLong("female")
                );
            }
            return new GenderCountResponse(0L, 0L);
        });
    }

    public AgeBucketsResponse fetchAgeBuckets() {
        String sql = generateAgeBucketsSQL(
            "t", 
            """
            SELECT TIMESTAMPDIFF(YEAR, birthDate, CURDATE()) AS age 
            FROM Tasker 
            WHERE birthDate IS NOT NULL
            """
        );

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> buckets = new HashMap<>();
            if (rs.next()) {
                buckets.put("0-10", rs.getLong("b_0_10"));
                buckets.put("10-20", rs.getLong("b_10_20"));
                buckets.put("20-30", rs.getLong("b_20_30"));
                buckets.put("30-40", rs.getLong("b_30_40"));
                buckets.put("40-50", rs.getLong("b_40_50"));
                buckets.put("50-60", rs.getLong("b_50_60"));
                buckets.put("60+", rs.getLong("b_60_plus"));
            }
            return new AgeBucketsResponse(buckets);
        });
    }

    public NewAccountsResponse fetchNewAccountsCounts(NewAccountsRequest request) {
        List<TimeRange> ranges = request.getRanges();
        String sql = "SELECT COUNT(*) FROM Tasker WHERE createdTime >= ? AND createdTime < ?";
        return new NewAccountsResponse(
                ranges.stream().map(r -> {
                    Timestamp from = Timestamp.valueOf(r.getFrom());
                    Timestamp to = Timestamp.valueOf(r.getTo());
                    return jdbcTemplate.queryForObject(sql, Long.class, from, to);
                }).toList()
        );
    }

    public RatingRangesResponse fetchRatingRanges() {
        int[][] ranges = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 6}};
        String[] labels = {"r_0_1", "r_1_2", "r_2_3", "r_3_4", "r_4_5"};
        String sql = generateRangeSQL("Tasker", "rating", ranges, labels);

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> _ranges = new HashMap<>();
            if (rs.next()) {
                _ranges.put("0-1", rs.getLong("r_0_1"));
                _ranges.put("1-2", rs.getLong("r_1_2"));
                _ranges.put("2-3", rs.getLong("r_2_3"));
                _ranges.put("3-4", rs.getLong("r_3_4"));
                _ranges.put("4-5", rs.getLong("r_4_5"));
            }
            return new RatingRangesResponse(_ranges);
        });
    }

    public HourRateRangesResponse fetchHourRateRanges() {
        int step = 10;
        int maxValue = 80;
        int numRanges = (maxValue / step) + 1;
        
        int[][] ranges = new int[numRanges][2];
        String[] labels = new String[numRanges];
        
        for (int i = 0; i < numRanges - 1; i++) {
            int start = i * step;
            int end = start + step;
            ranges[i][0] = start;
            ranges[i][1] = end;
            labels[i] = "hr_" + start + "_" + end;
        }
        
        ranges[numRanges - 1][0] = maxValue;
        ranges[numRanges - 1][1] = -1;
        labels[numRanges - 1] = "hr_" + maxValue + "_plus";
        
        String sql = generateRangeSQL("Tasker", "hourRate", ranges, labels);

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> _ranges = new HashMap<>();
            if (rs.next()) {
                for (int i = 0; i < numRanges - 1; i++) {
                    int start = i * step;
                    int end = start + step;
                    _ranges.put(start + "-" + end, rs.getLong(labels[i]));
                }
                _ranges.put(maxValue + "+", rs.getLong(labels[numRanges - 1]));
            }
            return new HourRateRangesResponse(_ranges);
        });
    }

    public WorkedHoursRangesResponse fetchWorkedHoursRanges() {
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
            labels[i] = "wh_" + start + "_" + end;
        }
        
        ranges[numRanges - 1][0] = maxValue;
        ranges[numRanges - 1][1] = -1;
        labels[numRanges - 1] = "wh_" + maxValue + "_plus";
        
        String sql = generateRangeSQL("Tasker", "WorkedHours", ranges, labels);

        return jdbcTemplate.query(sql, rs -> {
            Map<String, Long> _ranges = new HashMap<>();
            if (rs.next()) {
                for (int i = 0; i < numRanges - 1; i++) {
                    int start = i * step;
                    int end = start + step;
                    _ranges.put(start + "-" + end, rs.getLong(labels[i]));
                }
                _ranges.put(maxValue + "+", rs.getLong(labels[numRanges - 1]));
            }
            return new WorkedHoursRangesResponse(_ranges);
        });
    }

    public ServiceCountResponse fetchServiceCounts(ServiceCountRequest request) {
        List<Long> serviceIds = request.getServiceIds();
        if (serviceIds == null || serviceIds.isEmpty()) {
            return new ServiceCountResponse(new HashMap<>());
        }

        Map<Long, Long> counts = new HashMap<>();
        String sql = "SELECT COUNT(*) FROM Tasker WHERE serviceID = ?";
        
        for (Long serviceId : serviceIds) {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, serviceId);
            counts.put(serviceId, count != null ? count : 0L);
        }
        
        return new ServiceCountResponse(counts);
    }

    public CityCountResponse fetchCityCounts(CityCountRequest request) {
        List<String> cities = request.getCities();
        if (cities == null || cities.isEmpty()) {
            return new CityCountResponse(new HashMap<>());
        }

        Map<String, Long> counts = new HashMap<>();
        String sql = "SELECT COUNT(*) FROM Tasker WHERE addressCity = ?";
        
        for (String city : cities) {
            Long count = jdbcTemplate.queryForObject(sql, Long.class, city);
            counts.put(city, count != null ? count : 0L);
        }
        
        return new CityCountResponse(counts);
    }

    public TaskerStatusCountsResponse fetchStatusCounts() {
        Long suspended = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Tasker WHERE suspended = TRUE",
                Long.class
        );
        Long active = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Tasker WHERE suspended = FALSE",
                Long.class
        );
        return new TaskerStatusCountsResponse(suspended, active);
    }
}
