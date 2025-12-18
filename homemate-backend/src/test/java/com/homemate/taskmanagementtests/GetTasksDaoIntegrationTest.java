package com.homemate.taskmanagementtests;

import org.junit.jupiter.api.Test;

import com.homemate.taskmanagement.dao.GetTasksDao;
import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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



}
