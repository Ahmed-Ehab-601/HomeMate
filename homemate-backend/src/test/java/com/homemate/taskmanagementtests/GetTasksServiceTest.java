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


    public static final Long USER_ID_TEST = 4L;
    public static final Long TASKER_ID_TEST = 1L;
    @Mock
    private GetTasksDao taskDao;


    @InjectMocks
    private GetTaskService getTaskService;



    @Test
    void testThatGetUserTasksReturnsCorrectPaginatedResponseForAllStatus() {

        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 5;
        Long totalCount = 12L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByUserID(USER_ID_TEST)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListUserTasksByIDSortedByDate(USER_ID_TEST, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(USER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(5);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(3); // Math.ceilDiv(12, 5) = 3

        verify(taskDao).countTasksByUserID(USER_ID_TEST);
        verify(taskDao).getListUserTasksByIDSortedByDate(USER_ID_TEST, page, pageSize);
        verify(taskDao, never()).countTasksByUserIDAndStatus(anyLong(), any(StatusDto.class));
        verify(taskDao, never()).getListUserTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }

    @Test
    void testThatGetUserTasksReturnsCorrectPaginatedResponseForSpecificStatus() {
        // Arrange

        StatusDto status = StatusDto.Done;
        int page = 1;
        int pageSize = 10;
        Long totalCount = 20L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(10);

        when(taskDao.countTasksByUserIDAndStatus(USER_ID_TEST, status)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListUserTasksByIDAndStatusSortedByDate(USER_ID_TEST, status, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(USER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(10);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(2);

        verify(taskDao).countTasksByUserIDAndStatus(USER_ID_TEST, status);
        verify(taskDao).getListUserTasksByIDAndStatusSortedByDate(USER_ID_TEST, status, page, pageSize);
        verify(taskDao, never()).countTasksByUserID(anyLong());
        verify(taskDao, never()).getListUserTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetUserTasksReturnsEmptyWhenNoTasksExistForAllStatus() {
        // Arrange

        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByUserID(USER_ID_TEST)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(USER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByUserID(USER_ID_TEST);
        verify(taskDao, never()).getListUserTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetUserTasksReturnsEmptyWhenTotalCountIsZero() {
        // Arrange

        StatusDto status = StatusDto.All;
        int page = 2;
        int pageSize = 10;

        when(taskDao.countTasksByUserID(USER_ID_TEST)).thenReturn(Optional.of(0L));

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(USER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByUserID(USER_ID_TEST);
        verify(taskDao, never()).getListUserTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetUserTasksReturnsEmptyWhenNoTasksExistForSpecificStatus() {
        // Arrange
        StatusDto status = StatusDto.InProgress;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByUserIDAndStatus(USER_ID_TEST, status)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(USER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByUserIDAndStatus(USER_ID_TEST, status);
        verify(taskDao, never()).getListUserTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }
    @Test
    void testThatGetUserTasksCalculatesCorrectTotalPagesForSinglePage() {
        // Arrange
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;
        Long totalCount = 5L; // Less than pageSize

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByUserID(USER_ID_TEST)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListUserTasksByIDSortedByDate(USER_ID_TEST, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getUserTasks(USER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTotalPages()).isEqualTo(1); // Math.ceilDiv(5, 10) = 1
    }

    @Test
    void testThatGetTaskerTasksReturnsCorrectPaginatedResponseForAllStatus() {
        // Arrange
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 5;
        Long totalCount = 12L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByTaskerID(TASKER_ID_TEST)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListTaskerTasksByIDSortedByDate(TASKER_ID_TEST, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(TASKER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(5);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(3); // Math.ceilDiv(12, 5) = 3

        verify(taskDao).countTasksByTaskerID(TASKER_ID_TEST);
        verify(taskDao).getListTaskerTasksByIDSortedByDate(TASKER_ID_TEST, page, pageSize);
        verify(taskDao, never()).countTasksByTaskerIDAndStatus(anyLong(), any(StatusDto.class));
        verify(taskDao, never()).getListTaskerTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }

    @Test
    void testThatGeTaskerTasksReturnsCorrectPaginatedResponseForSpecificStatus() {
        // Arrange

        StatusDto status = StatusDto.Done;
        int page = 1;
        int pageSize = 10;
        Long totalCount = 20L;

        List<TaskCardDto> mockTasks = createMockTaskCardList(10);

        when(taskDao.countTasksByTaskerIDAndStatus(TASKER_ID_TEST, status)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListTaskerTasksByIDAndStatusSortedByDate(TASKER_ID_TEST, status, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(TASKER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTasks()).hasSize(10);
        assertThat(result.get().getPage()).isEqualTo(page);
        assertThat(result.get().getPageSize()).isEqualTo(pageSize);
        assertThat(result.get().getTotalCount()).isEqualTo(totalCount);
        assertThat(result.get().getTotalPages()).isEqualTo(2);

        verify(taskDao).countTasksByTaskerIDAndStatus(TASKER_ID_TEST, status);
        verify(taskDao).getListTaskerTasksByIDAndStatusSortedByDate(TASKER_ID_TEST, status, page, pageSize);
        verify(taskDao, never()).countTasksByTaskerID(anyLong());
        verify(taskDao, never()).getListTaskerTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetTaskerTasksReturnsEmptyWhenNoTasksExistForAllStatus() {
        // Arrange
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByTaskerID(TASKER_ID_TEST)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(TASKER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByTaskerID(TASKER_ID_TEST);
        verify(taskDao, never()).getListTaskerTasksByIDSortedByDate(anyLong(), anyInt(), anyInt());
    }

    @Test
    void testThatGetTaskerTasksReturnsEmptyWhenNoTasksExistForSpecificStatus() {
        // Arrange
        StatusDto status = StatusDto.InProgress;
        int page = 0;
        int pageSize = 10;

        when(taskDao.countTasksByTaskerIDAndStatus(TASKER_ID_TEST, status)).thenReturn(Optional.empty());

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(TASKER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).countTasksByTaskerIDAndStatus(TASKER_ID_TEST, status);
        verify(taskDao, never()).getListTaskerTasksByIDAndStatusSortedByDate(anyLong(), any(StatusDto.class), anyInt(), anyInt());
    }
    @Test
    void testThatGetTaskerTasksCalculatesCorrectTotalPagesForSinglePage() {
        // Arrange
        StatusDto status = StatusDto.All;
        int page = 0;
        int pageSize = 10;
        Long totalCount = 5L; // Less than pageSize

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.countTasksByTaskerID(TASKER_ID_TEST)).thenReturn(Optional.of(totalCount));
        when(taskDao.getListTaskerTasksByIDSortedByDate(TASKER_ID_TEST, page, pageSize)).thenReturn(mockTasks);

        // Act
        Optional<PaginatedResponse> result = getTaskService.getTaskerTasks(TASKER_ID_TEST, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTotalPages()).isEqualTo(1); // Math.ceilDiv(5, 10) = 1
    }



    @Test
    void testThatGetTaskerTasksForNewViewReturnsAllTasksWhenStatusIsAll() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.All;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.getTaskerTasksByDateRange(eq(TASKER_ID_TEST), eq(startDate), eq(endDate))).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(TASKER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getTaskerTasksByDateRange(TASKER_ID_TEST, startDate, endDate);
        verify(taskDao, never()).getTaskerTasksByDateRangeAndStatus(anyLong(), any(LocalDate.class), any(LocalDate.class), any(StatusDto.class));
    }

    @Test
    void testThatGetTaskerTasksForNewViewReturnsFilteredTasksWhenStatusIsSpecific() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        StatusDto statusDto = StatusDto.InProgress;

        List<TaskCardDto> mockTasks = createMockTaskCardList(3);

        when(taskDao.getTaskerTasksByDateRangeAndStatus(eq(TASKER_ID_TEST), eq(startDate), eq(endDate), eq(statusDto))).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(TASKER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getTaskerTasksByDateRangeAndStatus(TASKER_ID_TEST, startDate, endDate, statusDto);
        verify(taskDao, never()).getTaskerTasksByDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testThatGetTaskerTasksForNewViewReturnsEmptyListWhenNoTasksExistForDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);
        StatusDto statusDto = StatusDto.All;

        when(taskDao.getTaskerTasksByDateRange(eq(TASKER_ID_TEST), eq(startDate), eq(endDate))).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(TASKER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getTaskerTasksByDateRange(TASKER_ID_TEST, startDate, endDate);
    }

    @Test
    void testThatGetTaskerTasksForNewViewReturnsEmptyListWhenNoTasksMatchStatus() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.Rejected;

        when(taskDao.getTaskerTasksByDateRangeAndStatus(eq(TASKER_ID_TEST), eq(startDate), eq(endDate), eq(statusDto))).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getTaskerTasksForNewView(TASKER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getTaskerTasksByDateRangeAndStatus(TASKER_ID_TEST, startDate, endDate, statusDto);
    }





    @Test
    void testThatGetUserTasksForNewViewReturnsAllTasksWhenStatusIsAll() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.All;

        List<TaskCardDto> mockTasks = createMockTaskCardList(5);

        when(taskDao.getUserTasksByDateRange(eq(USER_ID_TEST), eq(startDate), eq(endDate))).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(USER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getUserTasksByDateRange(USER_ID_TEST, startDate, endDate);
        verify(taskDao, never()).getUserTasksByDateRangeAndStatus(anyLong(), any(LocalDate.class), any(LocalDate.class), any(StatusDto.class));
    }

    @Test
    void testThatGetUserTasksForNewViewReturnsFilteredTasksWhenStatusIsSpecific() {
        // Arrange

        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        StatusDto statusDto = StatusDto.Accepted;

        List<TaskCardDto> mockTasks = createMockTaskCardList(4);

        when(taskDao.getUserTasksByDateRangeAndStatus(eq(USER_ID_TEST), eq(startDate), eq(endDate), eq(statusDto))).thenReturn(mockTasks);

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(USER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).hasSize(4);
        assertThat(result).isEqualTo(mockTasks);

        verify(taskDao).getUserTasksByDateRangeAndStatus(USER_ID_TEST, startDate, endDate, statusDto);
        verify(taskDao, never()).getUserTasksByDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testThatGetUserTasksForNewViewReturnsEmptyListWhenNoTasksExistForDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 10, 1);
        LocalDate endDate = LocalDate.of(2025, 10, 31);
        StatusDto statusDto = StatusDto.All;

        when(taskDao.getUserTasksByDateRange(eq(USER_ID_TEST), eq(startDate), eq(endDate))).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(USER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getUserTasksByDateRange(USER_ID_TEST, startDate, endDate);
    }

    @Test
    void testThatGetUserTasksForNewViewReturnsEmptyListWhenNoTasksMatchStatus() {
        // Arrange
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 30);
        StatusDto statusDto = StatusDto.InReview;

        when(taskDao.getUserTasksByDateRangeAndStatus(eq(USER_ID_TEST), eq(startDate), eq(endDate), eq(statusDto))).thenReturn(new ArrayList<>());

        // Act
        List<TaskCardDto> result = getTaskService.getUserTasksForNewView(USER_ID_TEST, startDate, endDate, statusDto);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getUserTasksByDateRangeAndStatus(USER_ID_TEST, startDate, endDate, statusDto);
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
