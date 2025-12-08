package com.homemate.reports.dao.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.reports.model.AdminStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
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
