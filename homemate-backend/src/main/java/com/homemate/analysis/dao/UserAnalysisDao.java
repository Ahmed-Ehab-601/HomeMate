package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserAnalysisDao {

    private final JdbcTemplate jdbcTemplate;

    public GenderCountResponse fetchGenderCounts() {
        String sql = "SELECT " +
                "SUM(CASE WHEN gender = 'M' THEN 1 ELSE 0 END) AS male, " +
                "SUM(CASE WHEN gender = 'F' THEN 1 ELSE 0 END) AS female " +
                "FROM Users";
        return jdbcTemplate.query(sql, rs -> {
            if (rs.next()) {
                return new GenderCountResponse(rs.getLong("male"), rs.getLong("female"));
            }
            return new GenderCountResponse(0, 0);
        });
    }

    public StatusCountsResponse fetchStatusCounts() {
        long suspended = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Users WHERE suspended = TRUE",
                Long.class
        );
        long adminActive = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Users WHERE admin = TRUE AND suspended = FALSE",
                Long.class
        );
        long nonAdminActive = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM Users WHERE admin = FALSE AND suspended = FALSE",
                Long.class
        );
        return new StatusCountsResponse(suspended, adminActive, nonAdminActive);
    }

    public AgeBucketsResponse fetchAgeBuckets() {
        String sql = "SELECT " +
                "SUM(CASE WHEN age BETWEEN 0 AND 9 THEN 1 ELSE 0 END) AS b_0_10, " +
                "SUM(CASE WHEN age BETWEEN 10 AND 19 THEN 1 ELSE 0 END) AS b_10_20, " +
                "SUM(CASE WHEN age BETWEEN 20 AND 29 THEN 1 ELSE 0 END) AS b_20_30, " +
                "SUM(CASE WHEN age BETWEEN 30 AND 39 THEN 1 ELSE 0 END) AS b_30_40, " +
                "SUM(CASE WHEN age BETWEEN 40 AND 49 THEN 1 ELSE 0 END) AS b_40_50, " +
                "SUM(CASE WHEN age BETWEEN 50 AND 59 THEN 1 ELSE 0 END) AS b_50_60, " +
                "SUM(CASE WHEN age >= 60 THEN 1 ELSE 0 END) AS b_60_plus " +
                "FROM (SELECT TIMESTAMPDIFF(YEAR, birthDate, CURDATE()) AS age FROM Users WHERE birthDate IS NOT NULL) u";

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
        String sql = "SELECT COUNT(*) FROM Users WHERE createdTime >= ? AND createdTime < ?";
        return new NewAccountsResponse(
                ranges.stream().map(r -> {
                    Timestamp from = Timestamp.valueOf(r.getFrom());
                    Timestamp to = Timestamp.valueOf(r.getTo());
                    return jdbcTemplate.queryForObject(sql, Long.class, from, to);
                }).toList()
        );
    }
}
