package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ReportReviewAnalysisDao {

    private static final int MIN_YEAR = 2023;
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_DONE = "done";

    private final JdbcTemplate jdbcTemplate;

    public ReportsPerServiceResponse fetchReportsPerService() {
        String sql = """
            SELECT s.name AS serviceName, COUNT(DISTINCT r.reportID) AS count
            FROM Service s
            LEFT JOIN Task t ON t.serviceID = s.serviceID
            LEFT JOIN Report r ON r.taskID = t.taskID
            WHERE YEAR(t.startDate) >= ?
            GROUP BY s.name
        """;

        return jdbcTemplate.query(
                sql,
                (ResultSetExtractor<ReportsPerServiceResponse>) rs ->
                        new ReportsPerServiceResponse(mapLongCounts(rs, "count")),
                        MIN_YEAR
        );
    }

    public ReviewsPerServiceResponse fetchReviewsPerService() {
        String sql = """
            SELECT s.name AS serviceName, COUNT(DISTINCT rev.reviewID) AS count
            FROM Service s
            LEFT JOIN Task t ON t.serviceID = s.serviceID
            LEFT JOIN Reviews rev ON rev.taskID = t.taskID
            WHERE YEAR(t.startDate) >= ?
            GROUP BY s.name
        """;

        return jdbcTemplate.query(
                sql,
                (ResultSetExtractor<ReviewsPerServiceResponse>) rs ->
                        new ReviewsPerServiceResponse(mapLongCounts(rs, "count")),
                        MIN_YEAR
        );
    }

    public ReportStatusCountResponse fetchReportStatusCounts() {
        String sql = """
            SELECT
                SUM(CASE WHEN r.adminStatus = ? THEN 1 ELSE 0 END) AS pending,
                SUM(CASE WHEN r.adminStatus = ? THEN 1 ELSE 0 END) AS done
            FROM Report r
            JOIN Task t ON r.taskID = t.taskID
            WHERE YEAR(t.startDate) >= ?
        """;

        return jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> new ReportStatusCountResponse(
                        rs.getLong("pending"),
                        rs.getLong("done")
                ),
                STATUS_PENDING,
                STATUS_DONE,
                MIN_YEAR
        );
    }

    public AvgRatingPerServiceResponse fetchAvgRatingPerService() {
        String sql = """
            SELECT s.name AS serviceName, AVG(rev.rate) AS avgRating
            FROM Service s
            LEFT JOIN Task t ON t.serviceID = s.serviceID
            LEFT JOIN Reviews rev ON rev.taskID = t.taskID
            WHERE YEAR(t.startDate) >= ?
            GROUP BY s.name
        """;

        return jdbcTemplate.query(
                sql,
                (ResultSetExtractor<AvgRatingPerServiceResponse>) rs ->
                        new AvgRatingPerServiceResponse(mapDoubleAverages(rs, "avgRating")),
                        MIN_YEAR
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
