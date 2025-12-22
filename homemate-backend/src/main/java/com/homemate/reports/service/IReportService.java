package com.homemate.reports.service;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.security.model.AppUserDetails;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.util.PaginatedResponse;
import java.util.Optional;

public interface IReportService {
    boolean submitReport(SubmitReport submitReport, AppUserDetails userDetails);
    PaginatedResponse<ShortReport> getAllShortReports(int pageNumber, int pageSize, ReportFilterDto filterDto);
    Optional<DetailedReport> getDetailedReportById(int reportID);
    Optional<DetailedReport> completeReport(int reportID);
    void respondToReport(int reportID, String message);
}
