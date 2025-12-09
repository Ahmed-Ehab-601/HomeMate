package com.homemate.reports.dao;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import java.util.List;
import java.util.Optional;

public interface ReportDao {
    boolean submitReport(SubmitReport submitReport, boolean reporterIsUser);

    boolean taskExists(long taskId);

    boolean taskOwnedByUser(long taskId, long userId);

    boolean taskOwnedByTasker(long taskId, long taskerId);
  
    List<ShortReport> getAllShortReports(Long limit, Long offset, ReportFilterDto filterDto);
  
    Long countAllReports(ReportFilterDto filterDto);
  
    Optional<DetailedReport> getDetailedReportById(int reportID);
  
    void updateReportStatus(int reportID, String status);
}
