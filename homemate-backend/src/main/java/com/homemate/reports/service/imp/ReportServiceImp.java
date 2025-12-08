package com.homemate.reports.service.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.service.IReportService;
import com.homemate.util.PaginatedResponse;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportServiceImp implements IReportService {

    private final int PAGE_SIZE_LIMIT = 100;
    private final ReportDao reportDao;

    public ReportServiceImp(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public PaginatedResponse<ShortReport> getAllShortReports(int pageNumber, int pageSize) {
        
        // Validate limit
        pageNumber = Math.min(pageNumber, PAGE_SIZE_LIMIT);

        // Calculate offset
        long offset = (long) pageNumber * pageSize;

        // Fetch reports for the current page
        List<ShortReport> reports = reportDao.getAllShortReports((long) pageSize, offset);

        // Get total count of all reports
        Long totalElements = reportDao.countAllReports();

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

        // Build and return the paginated response
        return PaginatedResponse.<ShortReport>builder()
                .data(reports)
                .currentPage(pageNumber)
                .totalPages(totalPages)
                .totalElements(totalElements)
                .pageSize(pageSize)
                .build();
    }
}
