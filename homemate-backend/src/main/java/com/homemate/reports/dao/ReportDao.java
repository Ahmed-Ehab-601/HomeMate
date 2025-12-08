package com.homemate.reports.dao;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.DetailedReport;
import java.util.List;
import java.util.Optional;

public interface ReportDao {
    List<ShortReport> getAllShortReports(Long limit, Long offset);
    Long countAllReports();
    Optional<DetailedReport> getDetailedReportById(int reportID);
    void updateReportStatus(int reportID, String status);
}
