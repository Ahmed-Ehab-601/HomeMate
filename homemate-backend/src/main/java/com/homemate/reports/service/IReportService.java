package com.homemate.reports.service;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.util.PaginatedResponse;

public interface IReportService {
    PaginatedResponse<ShortReport> getAllShortReports(int pageNumber, int pageSize, ReportFilterDto filterDto);
}
