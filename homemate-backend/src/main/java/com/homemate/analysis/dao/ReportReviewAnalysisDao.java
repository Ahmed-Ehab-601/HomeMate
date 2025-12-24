package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportReviewAnalysisDao {

    private final JdbcTemplate jdbcTemplate;

    public ReportsPerServiceResponse fetchReportsPerService() {
        String sql = """
            SELECT s.name AS serviceName, COUNT(DISTINCT r.reportID) AS count
            FROM Service s
            LEFT JOIN Task t ON t.serviceID = s.serviceID
            LEFT JOIN Report r ON r.taskID = t.taskID
            GROUP BY s.name
        """;

        return jdbcTemplate.query(
                sql,
                (org.springframework.jdbc.core.ResultSetExtractor<ReportsPerServiceResponse>) rs -> new ReportsPerServiceResponse(mapLongCounts(rs, "count"))
        );
    }

    public ReviewsPerServiceResponse fetchReviewsPerService() {
        String sql = """
            SELECT s.name AS serviceName, COUNT(DISTINCT rev.reviewID) AS count
            FROM Service s
            LEFT JOIN Task t ON t.serviceID = s.serviceID
            LEFT JOIN Reviews rev ON rev.taskID = t.taskID
            GROUP BY s.name
        """;

        return jdbcTemplate.query(
                sql,
                (org.springframework.jdbc.core.ResultSetExtractor<ReviewsPerServiceResponse>) rs -> new ReviewsPerServiceResponse(mapLongCounts(rs, "count"))
        );
    }

    public ReportStatusCountResponse fetchReportStatusCounts() {
        String sql = """
            SELECT
                SUM(CASE WHEN adminStatus = 'pending' THEN 1 ELSE 0 END) AS pending,
                SUM(CASE WHEN adminStatus = 'done' THEN 1 ELSE 0 END) AS done
            FROM Report
        """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new ReportStatusCountResponse(
                        rs.getLong("pending"),
                        rs.getLong("done")
                )
        );
    }

    public AvgRatingPerServiceResponse fetchAvgRatingPerService() {
        String sql = """
            SELECT s.name AS serviceName, AVG(rev.rate) AS avgRating
            FROM Service s
            LEFT JOIN Task t ON t.serviceID = s.serviceID
            LEFT JOIN Reviews rev ON rev.taskID = t.taskID
            GROUP BY s.name
        """;

        return jdbcTemplate.query(
                sql,
                (org.springframework.jdbc.core.ResultSetExtractor<AvgRatingPerServiceResponse>) rs -> new AvgRatingPerServiceResponse(mapDoubleAverages(rs, "avgRating"))
        );
    }

    private Map<String, Long> mapLongCounts(ResultSet rs, String column)
            throws SQLException {

        Map<String, Long> result = new HashMap<>();

        while (rs.next()) {
            long value = rs.getLong(column);
            result.put(
                    rs.getString("serviceName"),
                    rs.wasNull() ? 0L : value
            );
        }
        return result;
    }

    private Map<String, Double> mapDoubleAverages(ResultSet rs, String column)
            throws SQLException {

        Map<String, Double> result = new HashMap<>();

        while (rs.next()) {
            double value = rs.getDouble(column);
            result.put(
                    rs.getString("serviceName"),
                    rs.wasNull() ? 0.0 : value
            );
        }
        return result;
    }
}
