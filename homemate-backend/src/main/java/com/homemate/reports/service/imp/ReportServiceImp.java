package com.homemate.reports.service.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.reports.service.IReportService;
import com.homemate.security.model.AppUserDetails;
import com.homemate.util.PaginatedResponse;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportServiceImp implements IReportService {

    private final int PAGE_SIZE_LIMIT = 100;
    private final ReportDao reportDao;

    public ReportServiceImp(ReportDao reportDao) {
        this.reportDao = reportDao;
    }

    @Override
    public PaginatedResponse<ShortReport> getAllShortReports(int pageNumber, int pageSize, ReportFilterDto filterDto) {
        
        // Validate limit
        pageNumber = Math.min(pageNumber, PAGE_SIZE_LIMIT);

        // Calculate offset
        long offset = (long) pageNumber * pageSize;

        // Fetch reports for the current page with filters
        List<ShortReport> reports = reportDao.getAllShortReports((long) pageSize, offset, filterDto);

        // Get total count of all reports with filters
        Long totalElements = reportDao.countAllReports(filterDto);

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

    @Override
    public boolean submitReport(SubmitReport submitReport, AppUserDetails userDetails) {
        boolean reporterIsUser = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equalsIgnoreCase("ROLE_USER"));

        // Basic length validation
        if (submitReport.getHeader() == null || submitReport.getHeader().length() > 100) {
            return false;
        }
        if (submitReport.getBody() == null || submitReport.getBody().length() > 500) {
            return false;
        }

        // Validate task exists
        if (!reportDao.taskExists(submitReport.getTaskID())) {
            return false;
        }

        // Validate association based on role
        long principalId = userDetails.getId();
        boolean ownsTask = reporterIsUser
                ? reportDao.taskOwnedByUser(submitReport.getTaskID(), principalId)
                : reportDao.taskOwnedByTasker(submitReport.getTaskID(), principalId);

        if (!ownsTask) {
            return false;
        }

        return reportDao.submitReport(submitReport, reporterIsUser);
    }
    
    public Optional<DetailedReport> getDetailedReportById(int reportID) {
        return reportDao.getDetailedReportById(reportID);
    }

    @Override
    public Optional<DetailedReport> completeReport(int reportID) {
        reportDao.updateReportStatus(reportID, "done");
        return reportDao.getDetailedReportById(reportID);
    }
}
