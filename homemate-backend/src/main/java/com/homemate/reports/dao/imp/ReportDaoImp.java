package com.homemate.reports.dao.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import org.springframework.jdbc.core.JdbcTemplate;
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

        Object[] params = {limit, offset};
        
        return jdbcTemplate.query(
            sql, 
            params,
            (rs, rowNum) -> {
                return ShortReport.builder()
                        .reportID(rs.getInt("reportID"))
                        .header(rs.getString("header"))
                        .taskID(rs.getInt("taskID"))
                        .reporter(rs.getBoolean("reporter"))
                        .adminStatus(rs.getString("adminStatus"))
                        .build();
            }
        );
    }

    @Override
    public Long countAllReports() {
        String sql = "SELECT COUNT(*) FROM Report";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0L;
    }
}
