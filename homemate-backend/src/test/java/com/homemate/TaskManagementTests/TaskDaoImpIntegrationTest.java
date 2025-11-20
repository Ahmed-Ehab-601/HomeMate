package com.homemate.TaskManagementTests;

import com.homemate.TaskManagement.Dao.impl.TaskDaoImpl;
import com.homemate.TaskManagement.Dto.TaskDto;
import com.homemate.TaskManagement.exceptions.DuplicateChatException;
import com.homemate.TaskManagement.model.Status;
import com.homemate.TaskManagement.model.TaskEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ExtendWith(SpringExtension.class)
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
        // Given - User 1 and Tasker 1 who already have a chat (from data.sql)
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
}