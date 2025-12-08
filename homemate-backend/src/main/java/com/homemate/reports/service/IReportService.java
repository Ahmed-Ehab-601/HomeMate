package com.homemate.reports.service;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.ReportsPaginatedResponse;

public interface IReportService {
    ReportsPaginatedResponse<ShortReport> getAllShortReports(int pageNumber, int pageSize);
}
