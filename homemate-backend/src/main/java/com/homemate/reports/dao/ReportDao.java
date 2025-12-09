package com.homemate.reports.dao;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import java.util.List;
import java.util.Optional;

public interface ReportDao {
    List<ShortReport> getAllShortReports(Long limit, Long offset, ReportFilterDto filterDto);
    Long countAllReports(ReportFilterDto filterDto);
    Optional<DetailedReport> getDetailedReportById(int reportID);
    void updateReportStatus(int reportID, String status);
}
