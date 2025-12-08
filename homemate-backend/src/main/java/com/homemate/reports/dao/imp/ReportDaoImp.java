package com.homemate.reports.dao.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.model.AdminStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    public boolean submitReport(SubmitReport submitReport, boolean reporterIsUser) {
        String sql = "INSERT INTO Report (taskID, header, body, reporter, adminStatus) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int rows = jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"reportID"});
            ps.setInt(1, submitReport.getTaskID());
            ps.setString(2, submitReport.getHeader());
            ps.setString(3, submitReport.getBody());
            ps.setBoolean(4, reporterIsUser);
            ps.setString(5, AdminStatus.PENDING.getValue());
            return ps;
        }, keyHolder);

        return rows > 0 && keyHolder.getKey() != null;
    }

    @Override
    public boolean taskExists(long taskId) {
        String sql = "SELECT COUNT(*) FROM Task WHERE taskID = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, taskId);
        return count != null && count > 0;
    }

    @Override
    public boolean taskOwnedByUser(long taskId, long userId) {
        String sql = "SELECT COUNT(*) FROM Task WHERE taskID = ? AND userID = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, taskId, userId);
        return count != null && count > 0;
    }

    @Override
    public boolean taskOwnedByTasker(long taskId, long taskerId) {
        String sql = "SELECT COUNT(*) FROM Task WHERE taskID = ? AND taskerID = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, taskId, taskerId);
        return count != null && count > 0;
    }
}
