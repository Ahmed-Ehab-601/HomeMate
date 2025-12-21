package com.homemate.reports.service;

import com.homemate.reports.dao.ReportDao;
import com.homemate.reports.dto.DetailedReport;
import com.homemate.reports.dto.ReportFilterDto;
import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import com.homemate.reports.model.AdminStatus;
import com.homemate.reports.service.imp.ReportServiceImp;
import com.homemate.security.model.AppUserDetails;
import com.homemate.util.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportDao reportDao;

    @Mock
    private com.homemate.notification.service.EmailService emailService;

    @InjectMocks
    private ReportServiceImp reportService;

    private AppUserDetails userDetails;
    private AppUserDetails taskerDetails;

    @BeforeEach
    void setUp() {
        // Setup user details
        userDetails = new AppUserDetails(1L, "johndoe", "user@test.com", "ROLE_USER");

        // Setup tasker details
        taskerDetails = new AppUserDetails(1L, "mikej", "tasker@test.com", "ROLE_TASKER");
    }

    @Test
    void testGetAllShortReports_Success() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        List<ShortReport> mockReports = new ArrayList<>();
        mockReports.add(ShortReport.builder()
                .reportID(1)
                .header("Test Report")
                .taskID(1)
                .reporter(true)
                .adminStatus(AdminStatus.PENDING)
                .build());
        
        when(reportDao.getAllShortReports(anyLong(), anyLong(), any(ReportFilterDto.class)))
                .thenReturn(mockReports);
        when(reportDao.countAllReports(any(ReportFilterDto.class))).thenReturn(1L);

        // Act
        PaginatedResponse<ShortReport> result = reportService.getAllShortReports(0, 10, filterDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(0, result.getCurrentPage());
        assertEquals(1, result.getTotalPages());
        assertEquals(1L, result.getTotalElements());
        assertEquals(10, result.getPageSize());

        verify(reportDao, times(1)).getAllShortReports(10L, 0L, filterDto);
        verify(reportDao, times(1)).countAllReports(filterDto);
    }

    @Test
    void testGetAllShortReports_EmptyResult() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        when(reportDao.getAllShortReports(anyLong(), anyLong(), any(ReportFilterDto.class)))
                .thenReturn(Collections.emptyList());
        when(reportDao.countAllReports(any(ReportFilterDto.class))).thenReturn(0L);

        // Act
        PaginatedResponse<ShortReport> result = reportService.getAllShortReports(0, 10, filterDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getTotalPages());
        assertEquals(0L, result.getTotalElements());
    }

    @Test
    void testGetAllShortReports_WithPagination() {
        // Arrange
        ReportFilterDto filterDto = new ReportFilterDto();
        List<ShortReport> mockReports = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            mockReports.add(ShortReport.builder()
                    .reportID(i + 1)
                    .header("Report " + (i + 1))
                    .taskID(1)
                    .reporter(true)
                    .adminStatus(AdminStatus.PENDING)
                    .build());
        }
        
        when(reportDao.getAllShortReports(5L, 5L, filterDto)).thenReturn(mockReports);
        when(reportDao.countAllReports(filterDto)).thenReturn(15L);

        // Act
        PaginatedResponse<ShortReport> result = reportService.getAllShortReports(1, 5, filterDto);

        // Assert
        assertEquals(1, result.getCurrentPage());
        assertEquals(3, result.getTotalPages()); // 15 total / 5 per page = 3 pages
        assertEquals(15L, result.getTotalElements());
        
        verify(reportDao, times(1)).getAllShortReports(5L, 5L, filterDto);
    }

    @Test
    void testSubmitReport_ByUser_Success() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header("Test Header")
                .body("Test Body")
                .build();

        when(reportDao.taskExists(1L)).thenReturn(true);
        when(reportDao.taskOwnedByUser(1L, 1L)).thenReturn(true);
        when(reportDao.submitReport(any(SubmitReport.class), eq(true))).thenReturn(true);

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertTrue(result);
        verify(reportDao, times(1)).taskExists(1L);
        verify(reportDao, times(1)).taskOwnedByUser(1L, 1L);
        verify(reportDao, times(1)).submitReport(submitReport, true);
    }

    @Test
    void testSubmitReport_ByTasker_Success() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header("Test Header")
                .body("Test Body")
                .build();

        when(reportDao.taskExists(1L)).thenReturn(true);
        when(reportDao.taskOwnedByTasker(1L, 1L)).thenReturn(true);
        when(reportDao.submitReport(any(SubmitReport.class), eq(false))).thenReturn(true);

        // Act
        boolean result = reportService.submitReport(submitReport, taskerDetails);

        // Assert
        assertTrue(result);
        verify(reportDao, times(1)).taskExists(1L);
        verify(reportDao, times(1)).taskOwnedByTasker(1L, 1L);
        verify(reportDao, times(1)).submitReport(submitReport, false);
    }

    @Test
    void testSubmitReport_InvalidTaskID_Negative() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(-1)
                .header("Test Header")
                .body("Test Body")
                .build();

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_InvalidTaskID_Zero() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(0)
                .header("Test Header")
                .body("Test Body")
                .build();

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_HeaderNull() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header(null)
                .body("Test Body")
                .build();

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_HeaderTooLong() {
        // Arrange
        String longHeader = "a".repeat(101); // 101 characters
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header(longHeader)
                .body("Test Body")
                .build();

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_BodyNull() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header("Test Header")
                .body(null)
                .build();

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_BodyTooLong() {
        // Arrange
        String longBody = "a".repeat(501); // 501 characters
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header("Test Header")
                .body(longBody)
                .build();

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_TaskDoesNotExist() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(999)
                .header("Test Header")
                .body("Test Body")
                .build();

        when(reportDao.taskExists(999L)).thenReturn(false);

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, times(1)).taskExists(999L);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_UserDoesNotOwnTask() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header("Test Header")
                .body("Test Body")
                .build();

        when(reportDao.taskExists(1L)).thenReturn(true);
        when(reportDao.taskOwnedByUser(1L, 1L)).thenReturn(false);

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, times(1)).taskExists(1L);
        verify(reportDao, times(1)).taskOwnedByUser(1L, 1L);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testSubmitReport_TaskerDoesNotOwnTask() {
        // Arrange
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header("Test Header")
                .body("Test Body")
                .build();

        when(reportDao.taskExists(1L)).thenReturn(true);
        when(reportDao.taskOwnedByTasker(1L, 1L)).thenReturn(false);

        // Act
        boolean result = reportService.submitReport(submitReport, taskerDetails);

        // Assert
        assertFalse(result);
        verify(reportDao, times(1)).taskExists(1L);
        verify(reportDao, times(1)).taskOwnedByTasker(1L, 1L);
        verify(reportDao, never()).submitReport(any(), anyBoolean());
    }

    @Test
    void testGetDetailedReportById_Exists() {
        // Arrange
        DetailedReport mockReport = DetailedReport.builder()
                .reportID(1)
                .header("Test Report")
                .body("Test Body")
                .taskID(1)
                .userID(1)
                .userUsername("testuser")
                .userEmail("user@test.com")
                .userSuspended(false)
                .taskerID(1)
                .taskerUsername("testtasker")
                .taskerEmail("tasker@test.com")
                .taskerSuspended(false)
                .reporter(true)
                .adminStatus(AdminStatus.PENDING)
                .build();

        when(reportDao.getDetailedReportById(1)).thenReturn(Optional.of(mockReport));

        // Act
        Optional<DetailedReport> result = reportService.getDetailedReportById(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getReportID());
        assertEquals("Test Report", result.get().getHeader());
        verify(reportDao, times(1)).getDetailedReportById(1);
    }

    @Test
    void testGetDetailedReportById_NotExists() {
        // Arrange
        when(reportDao.getDetailedReportById(999)).thenReturn(Optional.empty());

        // Act
        Optional<DetailedReport> result = reportService.getDetailedReportById(999);

        // Assert
        assertFalse(result.isPresent());
        verify(reportDao, times(1)).getDetailedReportById(999);
    }

    @Test
    void testCompleteReport_Success() {
        // Arrange
        DetailedReport mockReport = DetailedReport.builder()
                .reportID(1)
                .header("Test Report")
                .body("Test Body")
                .taskID(1)
                .userID(1)
                .userUsername("testuser")
                .userEmail("user@test.com")
                .userSuspended(false)
                .taskerID(1)
                .taskerUsername("testtasker")
                .taskerEmail("tasker@test.com")
                .taskerSuspended(false)
                .reporter(true)
                .adminStatus(AdminStatus.DONE)
                .build();

        doNothing().when(reportDao).updateReportStatus(1, "done");
        when(reportDao.getDetailedReportById(1)).thenReturn(Optional.of(mockReport));

        // Act
        Optional<DetailedReport> result = reportService.completeReport(1);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(AdminStatus.DONE, result.get().getAdminStatus());
        verify(reportDao, times(1)).updateReportStatus(1, "done");
        verify(reportDao, times(1)).getDetailedReportById(1);
    }

    @Test
    void testCompleteReport_ReportNotFound() {
        // Arrange
        doNothing().when(reportDao).updateReportStatus(999, "done");
        when(reportDao.getDetailedReportById(999)).thenReturn(Optional.empty());

        // Act
        Optional<DetailedReport> result = reportService.completeReport(999);

        // Assert
        assertFalse(result.isPresent());
        verify(reportDao, times(1)).updateReportStatus(999, "done");
        verify(reportDao, times(1)).getDetailedReportById(999);
    }

    @Test
    void testSubmitReport_ValidMaxLengthHeader() {
        // Arrange - 100 characters is valid
        String validHeader = "a".repeat(100);
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header(validHeader)
                .body("Test Body")
                .build();

        when(reportDao.taskExists(1L)).thenReturn(true);
        when(reportDao.taskOwnedByUser(1L, 1L)).thenReturn(true);
        when(reportDao.submitReport(any(SubmitReport.class), eq(true))).thenReturn(true);

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertTrue(result);
        verify(reportDao, times(1)).submitReport(submitReport, true);
    }

    @Test
    void testSubmitReport_ValidMaxLengthBody() {
        // Arrange - 500 characters is valid
        String validBody = "a".repeat(500);
        SubmitReport submitReport = SubmitReport.builder()
                .taskID(1)
                .header("Test Header")
                .body(validBody)
                .build();

        when(reportDao.taskExists(1L)).thenReturn(true);
        when(reportDao.taskOwnedByUser(1L, 1L)).thenReturn(true);
        when(reportDao.submitReport(any(SubmitReport.class), eq(true))).thenReturn(true);

        // Act
        boolean result = reportService.submitReport(submitReport, userDetails);

        // Assert
        assertTrue(result);
        verify(reportDao, times(1)).submitReport(submitReport, true);
    }
    @Test
    void testRespondToReport_Success() {
        // Arrange
        DetailedReport mockReport = DetailedReport.builder()
                .reportID(1)
                .header("Test Report")
                .body("Test Body")
                .taskID(1)
                .userID(1)
                .userEmail("user@test.com")
                .taskerID(1)
                .taskerEmail("tasker@test.com")
                .adminStatus(AdminStatus.DONE)
                .build();

        when(reportDao.getDetailedReportById(1)).thenReturn(Optional.of(mockReport));

        // Act
        reportService.respondToReport(1, "Test Response");

        // Assert
        verify(emailService, times(1)).sendDirectEmail("user@test.com", "Admin Response: Test Report", "Test Response");
        verify(emailService, times(1)).sendDirectEmail("tasker@test.com", "Admin Response: Test Report", "Test Response");
    }

    @Test
    void testRespondToReport_NotDone_ThrowsException() {
        // Arrange
        DetailedReport mockReport = DetailedReport.builder()
                .reportID(1)
                .header("Test Report")
                .adminStatus(AdminStatus.PENDING)
                .build();

        when(reportDao.getDetailedReportById(1)).thenReturn(Optional.of(mockReport));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> reportService.respondToReport(1, "Test Response"));
        
        verify(emailService, never()).sendDirectEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testRespondToReport_NotFound() {
        // Arrange
        when(reportDao.getDetailedReportById(999)).thenReturn(Optional.empty());

        // Act
        reportService.respondToReport(999, "Test Response");

        // Assert
        verify(emailService, never()).sendDirectEmail(anyString(), anyString(), anyString());
    }
}
