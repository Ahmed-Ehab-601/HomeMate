package com.homemate.reports.service;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.util.PaginatedResponse;
import java.util.Optional;

public interface IReportService {
    PaginatedResponse<ShortReport> getAllShortReports(int pageNumber, int pageSize);
    Optional<DetailedReport> getDetailedReportById(int reportID);
    Optional<DetailedReport> completeReport(int reportID);
}
