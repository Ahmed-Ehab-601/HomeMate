package com.homemate.reports.dao.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.reports.model.AdminStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Repository
public class ReportDaoImp implements ReportDao {

    private final JdbcTemplate jdbcTemplate;

    public ReportDaoImp(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ShortReport> getAllShortReports(Long limit, Long offset, ReportFilterDto filterDto) {
        StringBuilder sql = new StringBuilder("SELECT reportID, header, taskID, reporter, adminStatus FROM Report WHERE 1=1 ");
        
        List<Object> params = new ArrayList<>();
        
        sql.append(buildWhereClause(filterDto, params));
        sql.append(" ORDER BY reportID DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        
        return jdbcTemplate.query(
            sql.toString(),
            (rs, rowNum) -> {
                return ShortReport.builder()
                        .reportID(rs.getInt("reportID"))
                        .header(rs.getString("header"))
                        .taskID(rs.getInt("taskID"))
                        .reporter(rs.getBoolean("reporter"))
                        .adminStatus(AdminStatus.fromValue(rs.getString("adminStatus")))
                        .build();
            },
            params.toArray()
        );
    }

    @Override
    public Long countAllReports(ReportFilterDto filterDto) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM Report WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        
        sql.append(buildWhereClause(filterDto, params));
        
        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
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

    @Override
    public void updateReportStatus(int reportID, String status) {
        String sql = "UPDATE Report SET adminStatus = ? WHERE reportID = ?";
        jdbcTemplate.update(sql, status, reportID);
    }
    
    private String buildWhereClause(ReportFilterDto filterDto, List<Object> params) {
        StringBuilder whereClause = new StringBuilder();
        
        if (filterDto.getHeader() != null && !filterDto.getHeader().isEmpty()) {
            whereClause.append(" AND header LIKE ? ");
            params.add("%" + filterDto.getHeader() + "%");
        }
        
        if (filterDto.getBody() != null && !filterDto.getBody().isEmpty()) {
            whereClause.append(" AND body LIKE ? ");
            params.add("%" + filterDto.getBody() + "%");
        }
        
        if (filterDto.getTaskID() != null) {
            whereClause.append(" AND taskID = ? ");
            params.add(filterDto.getTaskID());
        }
        
        if (filterDto.getReporter() != null) {
            whereClause.append(" AND reporter = ? ");
            params.add(filterDto.getReporter());
        }
        
        if (filterDto.getAdminStatus() != null && !filterDto.getAdminStatus().isEmpty()) {
            whereClause.append(" AND adminStatus = ? ");
            params.add(filterDto.getAdminStatus());
        }
        
        return whereClause.toString();
    }
}
