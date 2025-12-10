package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskCardDto;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.DuplicateChatException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.model.TaskEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("task")
public class TaskDaoImpIntegrationTest {

    private final TaskDaoImpl underTest;

    @Autowired
    public TaskDaoImpIntegrationTest(TaskDaoImpl underTest) {
        this.underTest = underTest;
    }

    @Test
    void testThatTaskCreatedAndRecalled() {
        // Given - Create a new task entity
        TaskEntity taskEntity = TaskEntity.builder()
                .startDate(LocalDateTime.of(2024, 11, 20, 10, 0))
                .workedHours(0.0)
                .userID(1L)
                .taskerID(1L)
                .serviceID(1L)
                .endDate(null)
                .chatID(1L)
                .bill(0.0)
                .status(Status.InReview)
                .startInProgress(null)
                .addressID(1L)
                .description("Test task for house cleaning")
                .build();

        Optional<Long> taskIdOptional = underTest.insertTask(taskEntity);

        assertThat(taskIdOptional).isPresent();
        Long taskId = taskIdOptional.get();

        Optional<TaskDto> taskDtoOptional = underTest.getTaskDetails(taskId);

        assertThat(taskDtoOptional).isPresent();
        TaskDto taskDto = taskDtoOptional.get();

        assertThat(taskDto.getTaskID()).isEqualTo(taskId);
        assertThat(taskDto.getStartDate()).isEqualTo(LocalDateTime.of(2024, 11, 20, 10, 0));
        assertThat(taskDto.getWorkedHours()).isEqualTo(0.0);
        assertThat(taskDto.getStatus()).isEqualTo(Status.InReview);
        assertThat(taskDto.getDescription()).isEqualTo("Test task for house cleaning");
        assertThat(taskDto.getBill()).isEqualTo(0.0);
        assertThat(taskDto.getChatID()).isEqualTo(1L);
        assertThat(taskDto.getEndDate()).isNull();
        assertThat(taskDto.getStartInProgress()).isNull();


        assertThat(taskDto.getUserName()).isEqualTo("John Smith");
        assertThat(taskDto.getTaskerName()).isEqualTo("Tom Anderson");
        assertThat(taskDto.getServiceName()).isEqualTo("House Cleaning");
        assertThat(taskDto.getUserMail()).isEqualTo("john.smith@email.com");
        assertThat(taskDto.getTaskerMail()).isEqualTo("tom.a@tasker.com");
        assertThat(taskDto.getAddressDetails()).contains("123 Main Street");
        assertThat(taskDto.getAddressDetails()).contains("New York");
        assertThat(taskDto.getAddressDetails()).contains("USA");
    }

    @Test
    void testThatChatCreatedAndRecalled() {
        // Given - User and Tasker who don't have an existing chat
        Long userId = 3L;
        Long taskerId = 5L;

        // When - Check if chat exists (should not exist)
        Optional<Long> existingChatId = underTest.getChat(userId, taskerId);

        // Then - Verify no existing chat
        assertThat(existingChatId).isEmpty();

        // When - Create new chat
        Optional<Long> newChatId = underTest.insertChat(userId, taskerId);

        // Then - Verify chat was created
        assertThat(newChatId).isPresent();

        // When - Retrieve the chat again
        Optional<Long> retrievedChatId = underTest.getChat(userId, taskerId);

        // Then - Verify chat can be retrieved
        assertThat(retrievedChatId).isPresent();
        assertThat(retrievedChatId.get()).isEqualTo(newChatId.get());
    }

    @Test
    void testThatExistingChatIsReturned() {
        // Given - User 1 and Tasker 1 who already have a chat (from taskData.sql)
        Long userId = 1L;
        Long taskerId = 1L;

        // When - Get existing chat
        Optional<Long> chatId = underTest.getChat(userId, taskerId);

        // Then - Verify existing chat is returned
        assertThat(chatId).isPresent();
        assertThat(chatId.get()).isEqualTo(1L);

        // When - Try to insert chat for existing pair

        assertThatThrownBy(()->underTest.insertChat(userId,taskerId)).isInstanceOf(DuplicateChatException.class);
    }

    @Test
    void testThatDuplicateRequestDetected() {
        //Given - Existing task data (User 1, Tasker 10, Address 1) and is underReview
        Long userId = 7L;
        Long taskerId = 7L;
        Long addressId = 8L;

        boolean taskExists = underTest.checkIfTaskExist(userId, taskerId, addressId);
        assertThat(taskExists).isTrue();

        // Given - Non-existing task combination
        Long newUserId = 2L;
        Long newTaskerId = 5L;
        Long newAddressId = 3L;

        // When - Check if task exists
        boolean newTaskExists = underTest.checkIfTaskExist(newUserId, newTaskerId, newAddressId);

        // Then - Should not detect task
        assertThat(newTaskExists).isFalse();
    }

    @Test
    void testThatTaskLimitDetected() {

        Long userId = 1L;
        for(int i = 1 ;i<=5;i++){
            TaskEntity taskEntity = TaskEntity.builder()
                    .startDate(LocalDateTime.of(2024, 11, 20, 10, 0))
                    .userID(userId)
                    .taskerID(Integer.valueOf(i).longValue())
                    .serviceID(Integer.valueOf(i).longValue())
                    .chatID(1L)
                    .bill(0.0)
                    .status(Status.InReview)
                    .startInProgress(null)
                    .addressID(1L)
                    .description("Test task for house cleaning")
                    .build();

            underTest.insertTask(taskEntity);

        }

        boolean notExceeded = underTest.checkIfTaskLimit(userId,8);
        assertThat(notExceeded).isFalse();

        boolean exceeded = underTest.checkIfTaskLimit(userId,5);
        assertThat(exceeded).isTrue();

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
        assertThat(count).isPresent();
        assertThat(count.get()).isEqualTo(0L);
    }



}