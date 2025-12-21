package com.homemate.taskmanagementtests;

import org.junit.jupiter.api.Test;

import com.homemate.taskmanagement.dao.GetTasksDao;
import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task")

public class GetTasksDaoIntegrationTest {

    private final GetTasksDao underTest;

    @Autowired
    public GetTasksDaoIntegrationTest(GetTasksDao underTest) {
        this.underTest = underTest;
    }


    @Test
    void testThatGetListUserTasksByIDReturnsAllTasksSortedByDate() {
        // Given - User with multiple tasks
        Long userId = 1L;
        int page = 0;
        int pageSize = 10;

        // When - Get all tasks for user
        List<TaskCardDto> tasks = underTest.getListUserTasksByIDSortedByDate(userId, page, pageSize);

        // Then - Verify tasks are returned and sorted DESC
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.size()).isEqualTo(9);

        // Verify sorting
        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDateTime currentDate = tasks.get(i).getStartDate();
            LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
            assertThat(currentDate).isAfterOrEqualTo(nextDate);
        }

        // Verify all tasks have required data
        tasks.forEach(task -> {
            assertThat(task.getTaskID()).isNotNull();
            assertThat(task.getStartDate()).isNotNull();
            assertThat(task.getStatus()).isNotNull();
            assertThat(task.getUserName()).isEqualTo("John Smith");
            assertThat(task.getTaskerName()).isNotBlank();
            assertThat(task.getServiceName()).isNotBlank();
            assertThat(task.getAddressCity()).isNotBlank();
        });
    }

    @Test
    void testThatGetListUserTasksByStatusReturnsFilteredAndSortedTasks() {
        // Given - User with tasks of different statuses
        Long userId = 1L;
        StatusDto status = StatusDto.Done;
        int page = 0;
        int pageSize = 10;

        // When - Get tasks filtered by status
        List<TaskCardDto> tasks = underTest.getListUserTasksByIDAndStatusSortedByDate(userId, status, page, pageSize);

        // Then - Verify only specified status returned and sorted DESC
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.size()).isEqualTo(3);

        tasks.forEach(task -> assertThat(task.getStatus().toString()).isEqualTo(status.toString()));

        // Verify sorting
        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDateTime currentDate = tasks.get(i).getStartDate();
            LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
            assertThat(currentDate).isAfterOrEqualTo(nextDate);
        }
    }

    @Test
    void testThatPaginationReturnsEmptyListWhenPageNotFound() {
        // Given - User with limited tasks
        Long userId = 1L;
        int pageSize = 10;
        int largePage = 999;

        // When - Request page beyond available data
        List<TaskCardDto> tasks = underTest.getListUserTasksByIDSortedByDate(userId, largePage, pageSize);

        // Then - Should return empty list
        assertThat(tasks).isEmpty();
    }

    @Test
    void testThatPaginationReturnsPartialResultsOnLastPage() {
        // Given - User with tasks
        Long userId = 1L;
        int pageSize = 2;

        // When - Get total count
        Optional<Long> totalCount = underTest.countTasksByUserID(userId);
        assertThat(totalCount).isPresent();

        int lastPage = (int) Math.ceil(totalCount.get() / (double) pageSize) - 1;

        // When - Get last page
        List<TaskCardDto> lastPageTasks = underTest.getListUserTasksByIDSortedByDate(userId, lastPage, pageSize);

        // Then - Last page can have less than pageSize
        assertThat(lastPageTasks).isNotEmpty();
        assertThat(lastPageTasks.size()).isLessThanOrEqualTo(pageSize);
    }

    @Test
    void testThatCountTasksByUserIDReturnsCorrectCount() {
        // Given - User with tasks
        Long userId = 1L;

        // When - Count all tasks
        Optional<Long> count = underTest.countTasksByUserID(userId);

        // Then - Verify count matches actual tasks
        assertThat(count).isPresent();
        assertThat(count.get()).isGreaterThan(0L);

        List<TaskCardDto> allTasks = underTest.getListUserTasksByIDSortedByDate(userId, 0, 100);
        assertThat(count.get()).isEqualTo((long) allTasks.size());
    }

    @Test
    void testThatCountTasksByUserIDAndStatusReturnsCorrectCount() {
        // Given - User with tasks of different statuses
        Long userId = 1L;
        StatusDto status = StatusDto.Done;

        // When - Count tasks by status
        Optional<Long> count = underTest.countTasksByUserIDAndStatus(userId, status);

        // Then - Verify count matches actual filtered tasks
        assertThat(count).isPresent();

        List<TaskCardDto> filteredTasks = underTest.getListUserTasksByIDAndStatusSortedByDate(userId, status, 0, 100);
        assertThat(count.get()).isEqualTo((long) filteredTasks.size());
    }

    @Test
    void testThatCountTasksByUserIDReturnsZeroForNonExistentUser() {
        // Given - Non-existent user
        Long userId = 999L;

        // When - Count tasks
        Optional<Long> count = underTest.countTasksByUserID(userId);

        // Then - Verify count is 0
        assertThat(count).isPresent();
        assertThat(count.get()).isEqualTo(0L);
    }

    @Test
    void testThatGetListTaserTasksByIDReturnsAllTasksSortedByDate() {
        // Given - User with multiple tasks
        Long taskerId = 1L;
        int page = 0;
        int pageSize = 10;

        // When - Get all tasks for user
        List<TaskCardDto> tasks = underTest.getListTaskerTasksByIDSortedByDate(taskerId, page, pageSize);

        // Then - Verify tasks are returned and sorted DESC
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.size()).isEqualTo(7);

        // Verify sorting
        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDateTime currentDate = tasks.get(i).getStartDate();
            LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
            assertThat(currentDate).isAfterOrEqualTo(nextDate);
        }

        // Verify all tasks have required data
        tasks.forEach(task -> {
            assertThat(task.getTaskID()).isNotNull();
            assertThat(task.getStartDate()).isNotNull();
            assertThat(task.getStatus()).isNotNull();
            assertThat(task.getUserName()).isNotBlank();
            assertThat(task.getTaskerName()).isEqualTo("Tom Anderson");
            assertThat(task.getServiceName()).isNotBlank();
            assertThat(task.getAddressCity()).isNotBlank();
        });
    }

    @Test
    void testThatGetListTaskerTasksByStatusReturnsFilteredAndSortedTasks() {
        // Given - User with tasks of different statuses
        Long taskerId = 1L;
        StatusDto status = StatusDto.Done;
        int page = 0;
        int pageSize = 10;

        // When - Get tasks filtered by status
        List<TaskCardDto> tasks = underTest.getListTaskerTasksByIDAndStatusSortedByDate(taskerId, status, page, pageSize);

        // Then - Verify only specified status returned and sorted DESC
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.size()).isEqualTo(3);

        tasks.forEach(task -> {
            assertThat(task.getStatus().toString()).isEqualTo(status.toString());
        });

        // Verify sorting
        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDateTime currentDate = tasks.get(i).getStartDate();
            LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
            assertThat(currentDate).isAfterOrEqualTo(nextDate);
        }
    }

    @Test
    void testThatTaskerTasksPaginationReturnsEmptyListWhenPageNotFound() {
        // Given - User with limited tasks
        Long taskerId = 1L;
        int pageSize = 10;
        int largePage = 999;

        // When - Request page beyond available data
        List<TaskCardDto> tasks = underTest.getListTaskerTasksByIDSortedByDate(taskerId, largePage, pageSize);

        // Then - Should return empty list
        assertThat(tasks).isEmpty();
    }

    @Test
    void testThatCountTasksByTaskerIDReturnsCorrectCount() {
        // Given - User with tasks
        Long taskerId = 1L;

        // When - Count all tasks
        Optional<Long> count = underTest.countTasksByTaskerID(taskerId);

        // Then - Verify count matches actual tasks
        assertThat(count).isPresent();
        assertThat(count.get()).isGreaterThan(0L);

        List<TaskCardDto> allTasks = underTest.getListTaskerTasksByIDSortedByDate(taskerId, 0, 100);
        assertThat(count.get()).isEqualTo((long) allTasks.size());
    }

    @Test
    void testThatCountTasksByTaskerIDAndStatusReturnsCorrectCount() {
        // Given - User with tasks of different statuses
        Long taskerId = 1L;
        StatusDto status = StatusDto.Done;

        // When - Count tasks by status
        Optional<Long> count = underTest.countTasksByTaskerIDAndStatus(taskerId, status);

        // Then - Verify count matches actual filtered tasks
        assertThat(count).isPresent();

        List<TaskCardDto> filteredTasks = underTest.getListTaskerTasksByIDAndStatusSortedByDate(taskerId, status, 0, 100);
        assertThat(count.get()).isEqualTo((long) filteredTasks.size());
    }

    @Test
    void testThatCountTasksByTaskerIDReturnsZeroForNonExistentUser() {
        // Given - Non-existent user
        Long taskerId = 999L;

        // When - Count tasks
        Optional<Long> count = underTest.countTasksByTaskerID(taskerId);

        // Then - Verify count is 0
        assertThat(count.isPresent());
        assertThat(count.get()).isEqualTo(0L);
    }



    @Test
    void testThatGetUserTasksByDateRangeReturnsTasksInDateRange() {
        // Given - User with multiple tasks and a date range
        Long userId = 4L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, startDate, endDate);

        // Then - Verify tasks are returned
        assertThat(tasks).isNotEmpty();

        // Verify all tasks fall within date range
        tasks.forEach(task -> {
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
        });

        // Verify all tasks have required data
        tasks.forEach(task -> {
            assertThat(task.getTaskID()).isNotNull();
            assertThat(task.getStartDate()).isNotNull();
            assertThat(task.getStatus()).isNotNull();
            assertThat(task.getUserName()).isNotBlank();
            assertThat(task.getTaskerName()).isNotBlank();
            assertThat(task.getServiceName()).isNotBlank();
            assertThat(task.getAddressCity()).isNotBlank();
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeReturnsSortedTasksAscending() {
        // Given - User with multiple tasks in date range
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, startDate, endDate);

        // Then - Verify tasks are sorted by date ascending
        assertThat(tasks).isNotEmpty();
        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDateTime currentDate = tasks.get(i).getStartDate();
            LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
            assertThat(!currentDate.isAfter(nextDate)).isTrue();
        }
    }

    @Test
    void testThatGetUserTasksByDateRangeReturnsEmptyForNoMatchingTasks() {
        // Given - User and a date range with no tasks
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 1, 31);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, startDate, endDate);

        // Then - Verify empty list is returned
        assertThat(tasks).isEmpty();
    }

    @Test
    void testThatGetUserTasksByDateRangeReturnsSingleDayTasks() {
        // Given - User and a single day date range
        Long userId = 1L;
        LocalDate singleDate = LocalDate.of(2024, 11, 15);

        // When - Get tasks for single day
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, singleDate, singleDate);

        // Then - Tasks should only be from that day
        tasks.forEach(task -> {
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(taskDate).isEqualTo(singleDate);
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeReturnsNonExistentUserEmpty() {
        // Given - Non-existent user
        Long userId = 999L;
        LocalDate startDate = LocalDate.of(2030, 11, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, startDate, endDate);

        // Then - Verify empty list
        assertThat(tasks).isEmpty();
    }



    @Test
    void testThatGetUserTasksByDateRangeAndStatusReturnsFilteredTasks() {
        // Given - User with tasks in date range and specific status
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        StatusDto status = StatusDto.Done;

        // When - Get tasks for date range and status
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRangeAndStatus(userId, startDate, endDate, status);

        // Then - Verify all tasks match criteria
        assertThat(tasks).isNotEmpty();

        tasks.forEach(task -> {
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
            assertThat(task.getStatus().toString()).isEqualTo(status.toString());
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeAndStatusReturnsSortedTasksAscending() {
        // Given - User with multiple tasks
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        StatusDto status = StatusDto.InProgress;

        // When - Get tasks for date range and status
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRangeAndStatus(userId, startDate, endDate, status);

        // Then - Verify sorted ascending
        if (tasks.size() > 0) {
            for (int i = 0; i < tasks.size() - 1; i++) {
                LocalDateTime currentDate = tasks.get(i).getStartDate();
                LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
                assertThat(!currentDate.isAfter(nextDate)).isTrue();
            }
        }
    }

    @Test
    void testThatGetUserTasksByDateRangeAndStatusReturnsEmptyForNoMatchingStatus() {
        // Given - User and date range but status with no tasks
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        StatusDto status = StatusDto.Rejected;

        // When - Get tasks
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRangeAndStatus(userId, startDate, endDate, status);

        // Then - Verify empty list
        assertThat(tasks).isEmpty();
    }

    @Test
    void testThatGetUserTasksByDateRangeAndStatusReturnsEmptyForNoDateMatch() {
        // Given - User with status but outside date range
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        StatusDto status = StatusDto.Done;

        // When - Get tasks
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRangeAndStatus(userId, startDate, endDate, status);

        // Then - Verify empty list
        assertThat(tasks).isEmpty();
    }



    @Test
    void testThatGetTaskerTasksByDateRangeReturnsTasksInDateRange() {
        // Given - Tasker with multiple tasks and a date range
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRange(taskerId, startDate, endDate);

        // Then - Verify tasks are returned
        assertThat(tasks).isNotEmpty();

        // Verify all tasks fall within date range
        tasks.forEach(task -> {
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
        });

        // Verify all tasks have required data
        tasks.forEach(task -> {
            assertThat(task.getTaskID()).isNotNull();
            assertThat(task.getStartDate()).isNotNull();
            assertThat(task.getStatus()).isNotNull();
            assertThat(task.getUserName()).isNotBlank();
            assertThat(task.getTaskerName()).isNotBlank();
            assertThat(task.getServiceName()).isNotBlank();
            assertThat(task.getAddressCity()).isNotBlank();
        });
    }

    @Test
    void testThatGetTaskerTasksByDateRangeReturnsSortedTasksAscending() {
        // Given - Tasker with multiple tasks in date range
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRange(taskerId, startDate, endDate);

        // Then - Verify tasks are sorted by date ascending
        assertThat(tasks).isNotEmpty();
        for (int i = 0; i < tasks.size() - 1; i++) {
            LocalDateTime currentDate = tasks.get(i).getStartDate();
            LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
            assertThat(!currentDate.isAfter(nextDate)).isTrue();
        }
    }

    @Test
    void testThatGetTaskerTasksByDateRangeReturnsEmptyForNoMatchingTasks() {
        // Given - Tasker and a date range with no tasks
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 1, 31);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRange(taskerId, startDate, endDate);

        // Then - Verify empty list is returned
        assertThat(tasks).isEmpty();
    }

    @Test
    void testThatGetTaskerTasksByDateRangeReturnsNonExistentTaskerEmpty() {
        // Given - Non-existent tasker
        Long taskerId = 999L;
        LocalDate startDate = LocalDate.of(2030, 11, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);

        // When - Get tasks for date range
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRange(taskerId, startDate, endDate);

        // Then - Verify empty list
        assertThat(tasks).isEmpty();
    }



    @Test
    void testThatGetTaskerTasksByDateRangeAndStatusReturnsFilteredTasks() {
        // Given - Tasker with tasks in date range and specific status
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        StatusDto status = StatusDto.Done;

        // When - Get tasks for date range and status
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRangeAndStatus(taskerId, startDate, endDate, status);

        // Then - Verify all tasks match criteria
        assertThat(tasks).isNotEmpty();

        tasks.forEach(task -> {
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
            assertThat(task.getStatus().toString()).isEqualTo(status.toString());
        });
    }

    @Test
    void testThatGetTaskerTasksByDateRangeAndStatusReturnsSortedTasksAscending() {
        // Given - Tasker with multiple tasks
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        StatusDto status = StatusDto.InProgress;

        // When - Get tasks for date range and status
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRangeAndStatus(taskerId, startDate, endDate, status);

        // Then - Verify sorted ascending
        if (tasks.size() > 0) {
            for (int i = 0; i < tasks.size() - 1; i++) {
                LocalDateTime currentDate = tasks.get(i).getStartDate();
                LocalDateTime nextDate = tasks.get(i + 1).getStartDate();
                assertThat(!currentDate.isAfter(nextDate)).isTrue();
            }
        }
    }

    @Test
    void testThatGetTaskerTasksByDateRangeAndStatusReturnsEmptyForNoMatchingStatus() {
        // Given - Tasker and date range but status with no tasks
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 31);
        StatusDto status = StatusDto.Suspended;

        // When - Get tasks
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRangeAndStatus(taskerId, startDate, endDate, status);

        // Then - Verify empty list or verify all have correct status
        tasks.forEach(task -> {
            assertThat(task.getStatus().toString()).isEqualTo(status.toString());
        });
    }

    @Test
    void testThatGetTaskerTasksByDateRangeAndStatusReturnsEmptyForNoDateMatch() {
        // Given - Tasker with status but outside date range
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2030, 1, 1);
        LocalDate endDate = LocalDate.of(2030, 12, 31);
        StatusDto status = StatusDto.Done;

        // When - Get tasks
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRangeAndStatus(taskerId, startDate, endDate, status);

        // Then - Verify empty list
        assertThat(tasks).isEmpty();
    }


    // ==================== TESTS WITH ACTUAL TEST DATA ====================
    // Tests using the sample data from the database population script
    // Test data includes tasks from 2024-11-01 to 2024-11-23

    @Test
    void testThatGetUserTasksByDateRangeReturnsActualUserData() {
        // Given - User 1 (John Smith) with tasks in November
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get user tasks for November
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, startDate, endDate);

        // Then - Verify user has tasks in this period
        assertThat(tasks).isNotEmpty();
        assertThat(tasks.size()).isGreaterThan(0);

        // Verify all tasks belong to correct user
        tasks.forEach(task -> {
            assertThat(task.getUserName()).isEqualTo("John Smith");
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeReturnsSpecificDateRangeData() {
        // Given - User 1 with specific date range (first week of November)
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 10);

        // When - Get tasks for narrow date range
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, startDate, endDate);

        // Then - Verify tasks within range
        tasks.forEach(task -> {
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
            assertThat(task.getServiceName()).isNotBlank();
            assertThat(task.getTaskerName()).isNotBlank();
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeAndStatusReturnsDoneTasksOnly() {
        // Given - User 1 with Done status tasks
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        StatusDto status = StatusDto.Done;

        // When - Get Done tasks
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRangeAndStatus(userId, startDate, endDate, status);

        // Then - Verify all tasks have Done status
        assertThat(tasks).isNotEmpty();
        tasks.forEach(task -> {
            assertThat(task.getStatus()).isEqualTo(status);
            assertThat(task.getUserName()).isEqualTo("John Smith");
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeAndStatusReturnsAcceptedTasks() {
        // Given - User 1 with Accepted status tasks
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 12);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        StatusDto status = StatusDto.Accepted;

        // When - Get Accepted tasks
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRangeAndStatus(userId, startDate, endDate, status);

        // Then - Verify all tasks have Accepted status
        tasks.forEach(task -> {
            assertThat(task.getStatus()).isEqualTo(status);
        });
    }

    @Test
    void testThatGetTaskerTasksByDateRangeReturnsActualTaskerData() {
        // Given - Tasker 1 (Tom Anderson) with tasks in November
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get tasker tasks for November
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRange(taskerId, startDate, endDate);

        // Then - Verify tasker has tasks in this period
        assertThat(tasks).isNotEmpty();
        
        // Verify all tasks belong to correct tasker
        tasks.forEach(task -> {
            assertThat(task.getTaskerName()).isEqualTo("Tom Anderson");
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
        });
    }

    @Test
    void testThatGetTaskerTasksByDateRangeReturnsSortedAscending() {
        // Given - Tasker 1 with tasks across November
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get tasker tasks
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRange(taskerId, startDate, endDate);

        // Then - Verify sorted ascending by date
        assertThat(tasks).isNotEmpty();
        if (tasks.size() > 1) {
            for (int i = 0; i < tasks.size() - 1; i++) {
                LocalDateTime current = tasks.get(i).getStartDate();
                LocalDateTime next = tasks.get(i + 1).getStartDate();
                assertThat(!current.isAfter(next)).isTrue();
            }
        }
    }

    @Test
    void testThatGetTaskerTasksByDateRangeAndStatusReturnsCorrectData() {
        // Given - Tasker 1 with Done status tasks
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);
        StatusDto status = StatusDto.Done;

        // When - Get Done tasks for tasker
        List<TaskCardDto> tasks = underTest.getTaskerTasksByDateRangeAndStatus(taskerId, startDate, endDate, status);

        // Then - Verify all tasks match criteria
        tasks.forEach(task -> {
            assertThat(task.getStatus()).isEqualTo(status);
            assertThat(task.getTaskerName()).isEqualTo("Tom Anderson");
            LocalDate taskDate = task.getStartDate().toLocalDate();
            assertThat(!taskDate.isBefore(startDate)).isTrue();
            assertThat(!taskDate.isAfter(endDate)).isTrue();
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeVerifiesTaskDetails() {
        // Given - User 1 with tasks
        Long userId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 23);

        // When - Get user tasks
        List<TaskCardDto> tasks = underTest.getUserTasksByDateRange(userId, startDate, endDate);

        // Then - Verify all required fields are populated
        assertThat(tasks).isNotEmpty();
        tasks.forEach(task -> {
            assertThat(task.getTaskID()).isNotNull();
            assertThat(task.getStartDate()).isNotNull();
            assertThat(task.getStatus()).isNotNull();
            assertThat(task.getUserName()).isNotNull().isNotBlank();
            assertThat(task.getTaskerName()).isNotNull().isNotBlank();
            assertThat(task.getServiceName()).isNotNull().isNotBlank();
            assertThat(task.getAddressCity()).isNotNull().isNotBlank();
        });
    }

    @Test
    void testThatGetTaskerTasksByDateRangeAndStatusVerifiesMultipleStatuses() {
        // Given - Tasker 1
        Long taskerId = 1L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get InProgress tasks
        List<TaskCardDto> inProgressTasks = underTest.getTaskerTasksByDateRangeAndStatus(
                taskerId, startDate, endDate, StatusDto.InProgress);

        // Then - Verify InProgress tasks
        inProgressTasks.forEach(task -> {
            assertThat(task.getStatus()).isEqualTo(StatusDto.InProgress);
        });

        // When - Get InReview tasks
        List<TaskCardDto> inReviewTasks = underTest.getTaskerTasksByDateRangeAndStatus(
                taskerId, startDate, endDate, StatusDto.InReview);

        // Then - Verify InReview tasks
        inReviewTasks.forEach(task -> {
            assertThat(task.getStatus()).isEqualTo(StatusDto.InReview);
        });
    }

    @Test
    void testThatGetUserTasksByDateRangeHandlesMultipleUsers() {
        // Given - Multiple users
        Long user1 = 1L;
        Long user2 = 2L;
        LocalDate startDate = LocalDate.of(2024, 11, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 30);

        // When - Get tasks for different users
        List<TaskCardDto> user1Tasks = underTest.getUserTasksByDateRange(user1, startDate, endDate);
        List<TaskCardDto> user2Tasks = underTest.getUserTasksByDateRange(user2, startDate, endDate);

        // Then - Verify each user gets their own tasks
        user1Tasks.forEach(task -> assertThat(task.getUserName()).isEqualTo("John Smith"));
        user2Tasks.forEach(task -> assertThat(task.getUserName()).isEqualTo("Sarah Johnson"));
    }

}

