package com.homemate.reports.service.imp;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.reports.service.IReportService;
import com.homemate.security.model.AppUserDetails;
import com.homemate.util.PaginatedResponse;

import com.homemate.notification.service.EmailService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReportServiceImp implements IReportService {

    private final int PAGE_SIZE_LIMIT = 30;
    private final ReportDao reportDao;
    private final EmailService emailService;

    public ReportServiceImp(ReportDao reportDao, EmailService emailService) {
        this.reportDao = reportDao;
        this.emailService = emailService;
    }

    @Override
    public PaginatedResponse<ShortReport> getAllShortReports(int pageNumber, int pageSize, ReportFilterDto filterDto) {
        
        pageSize = Math.min(pageSize, PAGE_SIZE_LIMIT);
        long offset = (long) pageNumber * pageSize;
        List<ShortReport> reports = reportDao.getAllShortReports((long) pageSize, offset, filterDto);
        Long totalElements = reportDao.countAllReports(filterDto);
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);

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

        // Report validation
        if (submitReport.getTaskID() <= 0)
            return false;

        if (
            submitReport.getHeader() == null || 
            submitReport.getHeader().length() > 100
        )
            return false;

        if (
            submitReport.getBody() == null || 
            submitReport.getBody().length() > 500
        )
            return false;

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

    @Override
    public void respondToReport(int reportID, String message) {
        Optional<DetailedReport> reportOpt = reportDao.getDetailedReportById(reportID);
        if (reportOpt.isEmpty()) {
             throw new IllegalArgumentException("Report with ID " + reportID + " not found");
        }

        DetailedReport report = reportOpt.get();

        // Only respond if the report is marked as done
        if (report.getAdminStatus() == null || !"done".equalsIgnoreCase(report.getAdminStatus().name())) {
            throw new IllegalStateException("Report is not in done status");
        }
        
        String subject = "Admin Response: " + report.getHeader();
        
        if (report.getUserEmail() != null) {
            emailService.sendDirectEmail(report.getUserEmail(), subject, message);
        }
        if (report.getTaskerEmail() != null) {
            emailService.sendDirectEmail(report.getTaskerEmail(), subject, message);
        }
    }
}
