package com.homemate.taskmanagementtests;

import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskRequestDto;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.DuplicateRequestException;
import com.homemate.taskmanagement.exceptions.RequestLimitExceededException;
import com.homemate.taskmanagement.mappers.TaskMapper;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.model.TaskEntity;
import com.homemate.taskmanagement.service.TaskManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskDaoImpl taskDao;

    @InjectMocks
    private TaskManagementService taskManagementService;

    private TaskRequestDto taskRequestDto;
    private TaskEntity taskEntity;
    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        taskRequestDto = new TaskRequestDto();
        taskRequestDto.setUserID(1L);
        taskRequestDto.setTaskerID(2L);
        taskRequestDto.setAddressID(3L);
        taskRequestDto.setServiceID(4L);
        taskRequestDto.setDescription("Test task description");
        taskRequestDto.setStartDate(LocalDateTime.of(2025, 11, 20, 10, 0));


        taskEntity = TaskEntity.builder()
                .taskID(100L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .status(Status.InReview)
                .description("Test task description")
                .userID(1L)
                .taskerID(2L)
                .serviceID(4L)
                .chatID(null)
                .addressID(3L)
                .build();


        taskDto = TaskDto.builder()
                .taskID(100L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .endDate(LocalDateTime.of(2025, 11, 20, 18, 0))
                .status(Status.InReview)
                .description("Test task description")
                .workedHours(0.0)
                .startInProgress(null)
                .bill(0.0)
                .userName("John Doe")
                .taskerName("Jane Smith")
                .serviceName("Plumbing")
                .chatID(50L)
                .addressDetails("123 Main St")
                .rate(null)
                .review(null)
                .reviewImageDtoList(new ArrayList<>())
                .userMail("john@example.com")
                .taskerMail("jane@example.com")
                .build();
    }

    @Test
    void testThatRequestTaskSuccessWithExistingChat() {
        // Arrange
        Long existingChatId = 50L;
        Long taskId = 100L;

        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.of(existingChatId));
        when(taskDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        Optional<TaskDto> result = taskManagementService.requestTask(taskRequestDto);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get())
                .isEqualTo(taskDto)
                .extracting(TaskDto::getTaskID, TaskDto::getUserName, TaskDto::getTaskerName, TaskDto::getStatus)
                .containsExactly(100L, "John Doe", "Jane Smith", Status.InReview);

        verify(taskDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskDao).checkIfTaskLimit(1L, 10);
        verify(taskDao).getChat(1L, 2L);
        verify(taskDao).insertTask(argThat(task ->
                task.getChatID().equals(existingChatId) &&
                        task.getUserID().equals(1L) &&
                        task.getTaskerID().equals(2L) &&
                        task.getAddressID().equals(3L)
        ));
        verify(taskDao).getTaskDetails(taskId);
        verify(taskDao, never()).insertChat(anyLong(), anyLong());
    }

    @Test
    void testThatRequestTaskSuccessWithNewChat() {
        // Arrange
        Long newChatId = 60L;
        Long taskId = 100L;

        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.empty());
        when(taskDao.insertChat(1L, 2L)).thenReturn(Optional.of(newChatId));
        when(taskDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        Optional<TaskDto> result = taskManagementService.requestTask(taskRequestDto);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get())
                .isEqualTo(taskDto)
                .hasFieldOrPropertyWithValue("description", "Test task description");

        verify(taskDao).getChat(1L, 2L);
        verify(taskDao).insertChat(1L, 2L);
        verify(taskDao).insertTask(argThat(task -> task.getChatID().equals(newChatId)));
        verify(taskDao).getTaskDetails(taskId);
    }

    @Test
    void testThatRequestTaskThrowsDuplicateRequestException() {
        // Arrange
        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.requestTask(taskRequestDto))
                .isInstanceOf(DuplicateRequestException.class);

        verify(taskDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskDao, never()).checkIfTaskLimit(anyLong(), anyInt());
        verify(taskMapper, never()).getTaskEntity(any());
        verify(taskDao, never()).insertTask(any(TaskEntity.class));
    }

    @Test
    void testThatRequestTaskThrowsRequestLimitExceededException() {
        // Arrange
        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.requestTask(taskRequestDto))
                .isInstanceOf(RequestLimitExceededException.class);

        verify(taskDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskDao).checkIfTaskLimit(1L, 10);
        verify(taskMapper, never()).getTaskEntity(any());
        verify(taskDao, never()).insertTask(any(TaskEntity.class));
    }

    @Test
    void testRequestTask_ThrowsIllegalArgumentException_WhenTaskNotCreated() {
        // Arrange
        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.of(50L));
        when(taskDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.requestTask(taskRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the task not created correctly");

        verify(taskDao).insertTask(any(TaskEntity.class));
        verify(taskDao, never()).getTaskDetails(anyLong());
    }

    @Test
    void testThatHandleChatWithExistingChat() {
        // Arrange
        Long existingChatId = 50L;
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.of(existingChatId));

        // Act
        Long result = taskManagementService.handleChat(taskRequestDto);

        // Assert
        assertThat(result).isEqualTo(existingChatId);

        verify(taskDao).getChat(1L, 2L);
        verify(taskDao, never()).insertChat(anyLong(), anyLong());
    }

    @Test
    void testThatHandleChatCreateNewChat() {
        // Arrange
        Long newChatId = 60L;
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.empty());
        when(taskDao.insertChat(1L, 2L)).thenReturn(Optional.of(newChatId));

        // Act
        Long result = taskManagementService.handleChat(taskRequestDto);

        // Assert
        assertThat(result).isEqualTo(newChatId);

        verify(taskDao, times(1)).getChat(1L, 2L);
        verify(taskDao).insertChat(1L, 2L);
    }

    @Test
    void testThatHandleChatThrowsBadTaskRequestExceptionWhenChatCreationFails() {
        // Arrange
        when(taskDao.getChat(1L, 2L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        when(taskDao.insertChat(1L, 2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.handleChat(taskRequestDto))
                .isInstanceOf(BadTaskRequestException.class);

        verify(taskDao, times(2)).getChat(1L, 2L);
        verify(taskDao).insertChat(1L, 2L);
    }

    @Test
    void testThatHandleChatRetriesChatRetrievalWhenInsertReturnsEmpty() {
        // Arrange
        Long retrievedChatId = 70L;
        when(taskDao.getChat(1L, 2L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(retrievedChatId));
        when(taskDao.insertChat(1L, 2L)).thenReturn(Optional.empty());

        // Act
        Long result = taskManagementService.handleChat(taskRequestDto);

        // Assert
        assertThat(result).isEqualTo(retrievedChatId);

        verify(taskDao, times(2)).getChat(1L, 2L);
        verify(taskDao).insertChat(1L, 2L);
    }

    @Test
    void testCheckRequest_Success() {
        // Arrange
        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);

        // Act & Assert
        assertThatCode(() -> taskManagementService.checkRequest(taskRequestDto))
                .doesNotThrowAnyException();

        verify(taskDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskDao).checkIfTaskLimit(1L, 10);
    }

    @Test
    void testCheckRequest_ThrowsDuplicateRequestException() {
        // Arrange
        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.checkRequest(taskRequestDto))
                .isInstanceOf(DuplicateRequestException.class);

        verify(taskDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskDao, never()).checkIfTaskLimit(anyLong(), anyInt());
    }

    @Test
    void testCheckRequest_ThrowsRequestLimitExceededException() {
        // Arrange
        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskManagementService.checkRequest(taskRequestDto))
                .isInstanceOf(RequestLimitExceededException.class);

        verify(taskDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskDao).checkIfTaskLimit(1L, 10);
    }

    @Test
    void testGetTaskDetails_Success() {
        // Arrange
        Long taskId = 100L;
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        Optional<TaskDto> result = taskManagementService.getTaskDetails(taskId);

        // Assert
        assertThat(result)
                .isPresent()
                .hasValue(taskDto)
                .get()
                .extracting(TaskDto::getTaskID, TaskDto::getUserName, TaskDto::getTaskerMail)
                .containsExactly(100L, "John Doe", "jane@example.com");

        verify(taskDao).getTaskDetails(taskId);
    }

    @Test
    void testGetTaskDetails_ReturnsEmpty() {
        // Arrange
        Long taskId = 999L;
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<TaskDto> result = taskManagementService.getTaskDetails(taskId);

        // Assert
        assertThat(result).isEmpty();

        verify(taskDao).getTaskDetails(taskId);
    }

    @Test
    void testThatRequestTaskVerifiesChatIDSetCorrectly() {
        // Arrange
        Long chatId = 75L;
        Long taskId = 100L;

        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.of(chatId));
        when(taskDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        taskManagementService.requestTask(taskRequestDto);

        // Assert
        verify(taskDao).insertTask(argThat(entity -> entity.getChatID().equals(chatId)));
    }

    @Test
    void testThatRequestTaskVerifiesTaskEntityMappedCorrectly() {
        // Arrange
        TaskEntity customEntity = TaskEntity.builder()
                .startDate(LocalDateTime.of(2025, 12, 1, 9, 0))
                .endDate(LocalDateTime.of(2025, 12, 1, 17, 0))
                .status(Status.InReview)
                .description("Custom description")
                .userID(1L)
                .taskerID(2L)
                .serviceID(4L)
                .addressID(3L)
                .build();

        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(customEntity);
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.of(50L));
        when(taskDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(100L));
        when(taskDao.getTaskDetails(100L)).thenReturn(Optional.of(taskDto));

        // Act
        taskManagementService.requestTask(taskRequestDto);

        // Assert
        verify(taskMapper).getTaskEntity(taskRequestDto);
    }
}