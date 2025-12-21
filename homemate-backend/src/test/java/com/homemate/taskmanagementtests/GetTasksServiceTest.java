package com.homemate.taskmanagementtests;


import com.homemate.taskmanagement.dao.GetTasksDao;
import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.service.GetTaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GetTasksServiceTest {


    @Mock
    private GetTasksDao taskDao;


    @InjectMocks
    private GetTaskService getTaskService;



    @Test
    void testThatGetUserTasksReturnsCorrectPaginatedResponseForAllStatus() {
        // Arrange
        Long userId = 1L;
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 5;
        Long totalCount = 12L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByUserID(userId)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListUserTasksByIDSortedByDate(userId, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(userId, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(5);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(3); // Math.ceilDiv(12, 5) = 3

        verify(taskDao).countTasksByUserID(userId);
        verify(taskDao).getListUserTasksByIDSortedByDate(userId, page, pageSize);
        verify(taskDao, never()).countTasksByUserIDAndStatus(anyLong(), any(StatusDto.class));
        verify(taskDao, never()).getListUserTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }

    @Test
    void testThatGetUserTasksReturnsCorrectPaginatedResponseForSpecificStatus() {
        // Arrange
        Long userId = 1L;
        StatusDto status = StatusDto.Done;
        int page = 1;
        int pageSize = 10;
        Long totalCount = 20L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(10);

        when(taskDao.countTasksByUserIDAndStatus(userId, status)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListUserTasksByIDAndStatusSortedByDate(userId, status, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(userId, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(10);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(2);

        verify(taskDao).countTasksByUserIDAndStatus(userId, status);
        verify(taskDao).getListUserTasksByIDAndStatusSortedByDate(userId, status, page, pageSize);
        verify(taskDao, never()).countTasksByUserID(anyLong());
        verify(taskDao, never()).getListUserTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetUserTasksReturnsEmptyWhenNoTasksExistForAllStatus() {
        // Arrange
        Long userId = 1L;
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByUserID(userId)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(userId, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByUserID(userId);
        verify(taskDao, never()).getListUserTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetUserTasksReturnsEmptyWhenNoTasksExistForSpecificStatus() {
        // Arrange
        Long userId = 1L;
        StatusDto status = StatusDto.InProgress;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByUserIDAndStatus(userId, status)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(userId, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByUserIDAndStatus(userId, status);
        verify(taskDao, never()).getListUserTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }
    @Test
    void testThatGetUserTasksCalculatesCorrectTotalPagesForSinglePage() {
        // Arrange
        Long userId = 1L;
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;
        Long totalCount = 5L; // Less than pageSize

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByUserID(userId)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListUserTasksByIDSortedByDate(userId, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(userId, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTotalPages()).isEqualTo(1); // Math.ceilDiv(5, 10) = 1
    }

    @Test
    void testThatGetTaskerTasksReturnsCorrectPaginatedResponseForAllStatus() {
        // Arrange
        Long taskerID = 1L;
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 5;
        Long totalCount = 12L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByTaskerID(taskerID)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListTaskerTasksByIDSortedByDate(taskerID, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(taskerID, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(5);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(3); // Math.ceilDiv(12, 5) = 3

        verify(taskDao).countTasksByTaskerID(taskerID);
        verify(taskDao).getListTaskerTasksByIDSortedByDate(taskerID, page, pageSize);
        verify(taskDao, never()).countTasksByTaskerIDAndStatus(anyLong(), any(StatusDto.class));
        verify(taskDao, never()).getListTaskerTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }

    @Test
    void testThatGeTaskerTasksReturnsCorrectPaginatedResponseForSpecificStatus() {
        // Arrange
        Long taskerID = 1L;
        StatusDto status = StatusDto.Done;
        int page = 1;
        int pageSize = 10;
        Long totalCount = 20L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(10);

        when(taskDao.countTasksByTaskerIDAndStatus(taskerID, status)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListTaskerTasksByIDAndStatusSortedByDate(taskerID, status, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(taskerID, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(10);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(2);

        verify(taskDao).countTasksByTaskerIDAndStatus(taskerID, status);
        verify(taskDao).getListTaskerTasksByIDAndStatusSortedByDate(taskerID, status, page, pageSize);
        verify(taskDao, never()).countTasksByTaskerID(anyLong());
        verify(taskDao, never()).getListTaskerTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetTaskerTasksReturnsEmptyWhenNoTasksExistForAllStatus() {
        // Arrange
        Long taskerID = 1L;
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByTaskerID(taskerID)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(taskerID, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByTaskerID(taskerID);
        verify(taskDao, never()).getListTaskerTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetTaskerTasksReturnsEmptyWhenNoTasksExistForSpecificStatus() {
        // Arrange
        Long taskerID = 1L;
        StatusDto status = StatusDto.InProgress;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByTaskerIDAndStatus(taskerID, status)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(taskerID, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByTaskerIDAndStatus(taskerID, status);
        verify(taskDao, never()).getListTaskerTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }
    @Test
    void testThatGetTaskerTasksCalculatesCorrectTotalPagesForSinglePage() {
        // Arrange
        Long taskerID = 1L;
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;
        Long totalCount = 5L; // Less than pageSize

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByTaskerID(taskerID)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListTaskerTasksByIDSortedByDate(taskerID, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(taskerID, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTotalPages()).isEqualTo(1); // Math.ceilDiv(5, 10) = 1
    }



    @Test
    void testThatGetTaskerTasksForNewViewReturnsAllTasksWhenStatusIsAll() {
        // Arrange
        Long taskerID = 1L;
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.All;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.getTaskerTasksByDateRange(taskerID, startDate, endDate)).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(taskerID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getTaskerTasksByDateRange(taskerID, startDate, endDate);
        verify(taskDao, never()).getTaskerTasksByDateRangeAndStatus(anyLong(), any(LocalDate.class), any(LocalDate.class), any(StatusDto.class));
    }

    @Test
    void testThatGetTaskerTasksForNewViewReturnsFilteredTasksWhenStatusIsSpecific() {
        // Arrange
        Long taskerID = 2L;
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        StatusDto statusDto = StatusDto.InProgress;

        List<TaskCardDto> mockTasks = createMockTaskCardList(3);

        when(taskDao.getTaskerTasksByDateRangeAndStatus(taskerID, startDate, endDate, statusDto)).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(taskerID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getTaskerTasksByDateRangeAndStatus(taskerID, startDate, endDate, statusDto);
        verify(taskDao, never()).getTaskerTasksByDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testThatGetTaskerTasksForNewViewReturnsEmptyListWhenNoTasksExistForDateRange() {
        // Arrange
        Long taskerID = 3L;
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);
        StatusDto statusDto = StatusDto.All;

        when(taskDao.getTaskerTasksByDateRange(taskerID, startDate, endDate)).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(taskerID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getTaskerTasksByDateRange(taskerID, startDate, endDate);
    }

    @Test
    void testThatGetTaskerTasksForNewViewReturnsEmptyListWhenNoTasksMatchStatus() {
        // Arrange
        Long taskerID = 4L;
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.Rejected;

        when(taskDao.getTaskerTasksByDateRangeAndStatus(taskerID, startDate, endDate, statusDto)).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(taskerID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getTaskerTasksByDateRangeAndStatus(taskerID, startDate, endDate, statusDto);
    }



    // ==================== TESTS FOR getUserTasksForNewView ====================

    @Test
    void testThatGetUserTasksForNewViewReturnsAllTasksWhenStatusIsAll() {
        // Arrange
        Long userID = 1L;
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.All;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.getUserTasksByDateRange(userID, startDate, endDate)).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(userID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getUserTasksByDateRange(userID, startDate, endDate);
        verify(taskDao, never()).getUserTasksByDateRangeAndStatus(anyLong(), any(LocalDate.class), any(LocalDate.class), any(StatusDto.class));
    }

    @Test
    void testThatGetUserTasksForNewViewReturnsFilteredTasksWhenStatusIsSpecific() {
        // Arrange
        Long userID = 2L;
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        StatusDto statusDto = StatusDto.Accepted;

        List<TaskCardDto> mockTasks = createMockTaskCardList(4);

        when(taskDao.getUserTasksByDateRangeAndStatus(userID, startDate, endDate, statusDto)).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(userID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(4);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getUserTasksByDateRangeAndStatus(userID, startDate, endDate, statusDto);
        verify(taskDao, never()).getUserTasksByDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testThatGetUserTasksForNewViewReturnsEmptyListWhenNoTasksExistForDateRange() {
        // Arrange
        Long userID = 3L;
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);
        StatusDto statusDto = StatusDto.All;

        when(taskDao.getUserTasksByDateRange(userID, startDate, endDate)).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(userID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getUserTasksByDateRange(userID, startDate, endDate);
    }

    @Test
    void testThatGetUserTasksForNewViewReturnsEmptyListWhenNoTasksMatchStatus() {
        // Arrange
        Long userID = 4L;
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.InReview;

        when(taskDao.getUserTasksByDateRangeAndStatus(userID, startDate, endDate, statusDto)).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(userID, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getUserTasksByDateRangeAndStatus(userID, startDate, endDate, statusDto);
    }


    // Helper method to create mock TaskCardDto list
    private List<TaskCardDto> createMockTaskCardList(int size) {
        List<TaskCardDto> tasks = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            TaskCardDto task = TaskCardDto.builder()
                    .taskID((long) (i + 1))
                    .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                    .status(StatusDto.InReview)
                    .userName("User " + i)
                    .taskerName("Tasker " + i)
                    .serviceName("Service " + i)
                    .addressCity("City " + i)
                    .build();
            tasks.add(task);
        }
        return tasks;
    }

}
