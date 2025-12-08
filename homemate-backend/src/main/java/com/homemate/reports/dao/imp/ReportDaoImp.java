package com.homemate.reports.dao.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.model.AdminStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ReportDaoImp implements ReportDao {

    private final JdbcTemplate jdbcTemplate;

    public ReportDaoImp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ShortReport> getAllShortReports(Long limit, Long offset) {
        String sql = "SELECT reportID, header, taskID, reporter, adminStatus FROM Report " +
                     "ORDER BY reportID DESC LIMIT ? OFFSET ?";
        
        return jdbcTemplate.query(
            sql, 
            (rs, rowNum) -> {
                return ShortReport.builder()
                        .reportID(rs.getInt("reportID"))
                        .header(rs.getString("header"))
                        .taskID(rs.getInt("taskID"))
                        .reporter(rs.getBoolean("reporter"))
                        .adminStatus(AdminStatus.fromValue(rs.getString("adminStatus")))
                        .build();
            },
            limit,
            offset
        );
    }

    @Override
    public Long countAllReports() {
        String sql = "SELECT COUNT(*) FROM Report";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }

    @Override
    public Optional<DetailedReport> getDetailedReportById(int reportID) {
        String sql = "SELECT " +
                     "r.reportID, r.header, r.body, r.taskID, r.reporter, r.adminStatus, " +
                     "t.userID, t.taskerID, " +
                     "u.username AS userUsername, u.email AS userEmail, u.suspended AS userSuspended, " +
                     "ta.username AS taskerUsername, ta.email AS taskerEmail, ta.suspended AS taskerSuspended " +
                     "FROM Report r " +
                     "INNER JOIN Task t ON r.taskID = t.taskID " +
                     "INNER JOIN Users u ON t.userID = u.userID " +
                     "INNER JOIN Tasker ta ON t.taskerID = ta.taskerID " +
                     "WHERE r.reportID = ?";

        try {
            DetailedReport report = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) -> {
                    return DetailedReport.builder()
                            .reportID(rs.getInt("reportID"))
                            .header(rs.getString("header"))
                            .body(rs.getString("body"))
                            .taskID(rs.getInt("taskID"))
                            .userID(rs.getInt("userID"))
                            .userUsername(rs.getString("userUsername"))
                            .userEmail(rs.getString("userEmail"))
                            .userSuspended(rs.getBoolean("userSuspended"))
                            .taskerID(rs.getInt("taskerID"))
                            .taskerUsername(rs.getString("taskerUsername"))
                            .taskerEmail(rs.getString("taskerEmail"))
                            .taskerSuspended(rs.getBoolean("taskerSuspended"))
                            .reporter(rs.getBoolean("reporter"))
                            .adminStatus(AdminStatus.fromValue(rs.getString("adminStatus")))
                            .build();
                },
                reportID
            );
            return Optional.of(report);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
