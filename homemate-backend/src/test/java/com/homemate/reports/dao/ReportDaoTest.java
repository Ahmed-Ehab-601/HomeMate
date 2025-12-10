package com.homemate.reports.dao;

import com.homemate.reports.dao.imp.ReportDaoImp;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.model.AdminStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("reports")
class ReportDaoTest {

    @Autowired
    private ReportDaoImp reportDao;

    @Test
    void testGetAllShortReports_NoFilters() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        
        // Act
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        // Assert
        assertNotNull(reports);
        assertEquals(5, reports.size());
        
        // Verify ordering (DESC by reportID)
        assertTrue(reports.get(0).getReportID() > reports.get(1).getReportID());
    }

    @Test
    void testGetAllShortReports_WithHeaderFilter() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setHeader("Tasker");
        
        // Act
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        // Assert
        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).getHeader().contains("Tasker"));
    }

    @Test
    void testGetAllShortReports_WithBodyFilter() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setBody("pay");
        
        // Act
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        // Assert
        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertTrue(reports.get(0).getReportID() == 2);
    }

    @Test
    void testGetAllShortReports_WithTaskIDFilter() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setTaskID(1);
        
        // Act
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        // Assert
        assertNotNull(reports);
        assertEquals(2, reports.size());
        assertTrue(reports.stream().allMatch(r -> r.getTaskID() == 1));
    }

    @Test
    void testGetAllShortReports_WithReporterFilter() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setReporter(true); // User reports
        
        // Act
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        // Assert
        assertNotNull(reports);
        assertEquals(3, reports.size());
        assertTrue(reports.stream().allMatch(ShortReport::isReporter));
    }

    @Test
    void testGetAllShortReports_WithAdminStatusFilter() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setAdminStatus("pending");
        
        // Act
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        // Assert
        assertNotNull(reports);
        assertEquals(3, reports.size());
        assertTrue(reports.stream().allMatch(r -> r.getAdminStatus() == AdminStatus.PENDING));
    }

    @Test
    void testGetAllShortReports_WithMultipleFilters() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setTaskID(1);
        filterDto.setReporter(true);
        filterDto.setAdminStatus("pending");
        
        // Act
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        // Assert
        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals(1, reports.get(0).getTaskID());
        assertTrue(reports.get(0).isReporter());
        assertEquals(AdminStatus.PENDING, reports.get(0).getAdminStatus());
    }

    @Test
    void testGetAllShortReports_WithPagination() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        
        // Act - First page
        List<ShortReport> page1 = reportDao.getAllShortReports(2L, 0L, filterDto);
        List<ShortReport> page2 = reportDao.getAllShortReports(2L, 2L, filterDto);
        
        // Assert
        assertEquals(2, page1.size());
        assertEquals(2, page2.size());
        assertNotEquals(page1.get(0).getReportID(), page2.get(0).getReportID());
    }

    @Test
    void testCountAllReports_NoFilters() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        
        // Act
        Long count = reportDao.countAllReports(filterDto);
        
        // Assert
        assertEquals(5L, count);
    }

    @Test
    void testCountAllReports_WithFilters() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setTaskID(1);
        
        // Act
        Long count = reportDao.countAllReports(filterDto);
        
        // Assert
        assertEquals(2L, count);
    }

    @Test
    void testSubmitReport_ByUser_Success() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(4)
                .header("New Report Header")
                .body("This is a test report body")
                .build();
        
        // Act
        boolean result = reportDao.submitReport(submitReport, true);
        
        // Assert
        assertTrue(result);
        
        // Verify the report was created
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setHeader("New Report Header");
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        assertEquals(1, reports.size());
        assertEquals("New Report Header", reports.get(0).getHeader());
        assertTrue(reports.get(0).isReporter());
        assertEquals(AdminStatus.PENDING, reports.get(0).getAdminStatus());
    }

    @Test
    void testSubmitReport_ByTasker_Success() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(3)
                .header("Tasker Report")
                .body("User behaved poorly")
                .build();
        
        // Act
        boolean result = reportDao.submitReport(submitReport, false);
        
        // Assert
        assertTrue(result);
        
        // Verify the report was created
        ReportFilterDto filterDto = new ReportFilterDto();
        filterDto.setHeader("Tasker Report");
        List<ShortReport> reports = reportDao.getAllShortReports(10L, 0L, filterDto);
        
        assertEquals(1, reports.size());
        assertFalse(reports.get(0).isReporter()); // Reporter is tasker
    }

    @Test
    void testTaskExists_ExistingTask() {
        // Act & Assert
        assertTrue(reportDao.taskExists(1L));
        assertTrue(reportDao.taskExists(2L));
        assertTrue(reportDao.taskExists(3L));
    }

    @Test
    void testTaskExists_NonExistentTask() {
        // Act & Assert
        assertFalse(reportDao.taskExists(999L));
    }

    @Test
    void testTaskOwnedByUser_ValidOwnership() {
        // Act & Assert
        assertTrue(reportDao.taskOwnedByUser(1L, 1L)); // Task 1 owned by User 1
        assertTrue(reportDao.taskOwnedByUser(2L, 2L)); // Task 2 owned by User 2
    }

    @Test
    void testTaskOwnedByUser_InvalidOwnership() {
        // Act & Assert
        assertFalse(reportDao.taskOwnedByUser(1L, 2L)); // Task 1 not owned by User 2
        assertFalse(reportDao.taskOwnedByUser(999L, 1L)); // Non-existent task
    }

    @Test
    void testTaskOwnedByTasker_ValidOwnership() {
        // Act & Assert
        assertTrue(reportDao.taskOwnedByTasker(1L, 1L)); // Task 1 owned by Tasker 1
        assertTrue(reportDao.taskOwnedByTasker(2L, 2L)); // Task 2 owned by Tasker 2
    }

    @Test
    void testTaskOwnedByTasker_InvalidOwnership() {
        // Act & Assert
        assertFalse(reportDao.taskOwnedByTasker(1L, 2L)); // Task 1 not owned by Tasker 2
        assertFalse(reportDao.taskOwnedByTasker(999L, 1L)); // Non-existent task
    }

    @Test
    void testGetDetailedReportById_ExistingReport() {
        // Act
        Optional<DetailedReport> reportOpt = reportDao.getDetailedReportById(1);
        
        // Assert
        assertTrue(reportOpt.isPresent());
        DetailedReport report = reportOpt.get();
        
        assertEquals(1, report.getReportID());
        assertEquals("Tasker did not complete the work", report.getHeader());
        assertNotNull(report.getBody());
        assertEquals(1, report.getTaskID());
        assertEquals(1, report.getUserID());
        assertEquals("johndoe", report.getUserUsername());
        assertEquals("john@test.com", report.getUserEmail());
        assertFalse(report.isUserSuspended());
        assertEquals(1, report.getTaskerID());
        assertEquals("mikej", report.getTaskerUsername());
        assertEquals("mike@test.com", report.getTaskerEmail());
        assertFalse(report.isTaskerSuspended());
        assertTrue(report.isReporter());
        assertEquals(AdminStatus.PENDING, report.getAdminStatus());
    }

    @Test
    void testGetDetailedReportById_NonExistentReport() {
        // Act
        Optional<DetailedReport> reportOpt = reportDao.getDetailedReportById(999);
        
        // Assert
        assertFalse(reportOpt.isPresent());
    }

    @Test
    void testGetDetailedReportById_VerifySuspendedUser() {
        // Act
        Optional<DetailedReport> reportOpt = reportDao.getDetailedReportById(5);
        
        // Assert
        assertTrue(reportOpt.isPresent());
        DetailedReport report = reportOpt.get();
        
        // User 3 is suspended
        assertTrue(report.isUserSuspended());
    }

    @Test
    void testGetDetailedReportById_VerifySuspendedTasker() {
        // Act
        Optional<DetailedReport> reportOpt = reportDao.getDetailedReportById(5);
        
        // Assert
        assertTrue(reportOpt.isPresent());
        DetailedReport report = reportOpt.get();
        
        // Tasker 3 is suspended
        assertTrue(report.isTaskerSuspended());
    }

    @Test
    void testUpdateReportStatus_FromPendingToDone() {
        // Arrange
        int reportId = 1;
        
        // Verify initial status
        Optional<DetailedReport> reportBefore = reportDao.getDetailedReportById(reportId);
        assertTrue(reportBefore.isPresent());
        assertEquals(AdminStatus.PENDING, reportBefore.get().getAdminStatus());
        
        // Act
        reportDao.updateReportStatus(reportId, AdminStatus.DONE.getValue());
        
        // Assert
        Optional<DetailedReport> reportAfter = reportDao.getDetailedReportById(reportId);
        assertTrue(reportAfter.isPresent());
        assertEquals(AdminStatus.DONE, reportAfter.get().getAdminStatus());
    }

    @Test
    void testUpdateReportStatus_FromDoneToPending() {
        // Arrange
        int reportId = 3;
        
        // Verify initial status is done
        Optional<DetailedReport> reportBefore = reportDao.getDetailedReportById(reportId);
        assertTrue(reportBefore.isPresent());
        assertEquals(AdminStatus.DONE, reportBefore.get().getAdminStatus());
        
        // Act
        reportDao.updateReportStatus(reportId, AdminStatus.PENDING.getValue());
        
        // Assert
        Optional<DetailedReport> reportAfter = reportDao.getDetailedReportById(reportId);
        assertTrue(reportAfter.isPresent());
        assertEquals(AdminStatus.PENDING, reportAfter.get().getAdminStatus());
    }

    @Test
    void testUpdateReportStatus_NonExistentReport() {
        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> reportDao.updateReportStatus(999, AdminStatus.DONE.getValue()));
    }
}
