package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.notification.service.EmailService;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskRescheduleDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.exceptions.*;
import com.homemate.taskmanagement.mappers.TaskMapper;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.model.TaskEntity;
import com.homemate.taskmanagement.service.TaskRequestService;
import com.homemate.taskmanagement.service.TaskViewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRequestServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskRequestDao taskRequestDao;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TaskRequestService taskRequestService;

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
                .userMail("john@example.com")
                .taskerMail("jane@example.com")
                .build();
    }

    @Test
    void testThatRequestTaskSuccessWithExistingChat() {
        // Arrange
        Long existingChatId = 50L;
        Long taskId = 100L;

        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskRequestDao.getChat(1L, 2L)).thenReturn(Optional.of(existingChatId));
        when(taskRequestDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        Optional<TaskDto> result = taskRequestService.requestTask(taskRequestDto);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get())
                .isEqualTo(taskDto)
                .extracting(TaskDto::getTaskID, TaskDto::getUserName, TaskDto::getTaskerName, TaskDto::getStatus)
                .containsExactly(100L, "John Doe", "Jane Smith", Status.InReview);

        verify(taskRequestDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskRequestDao).checkIfTaskLimit(1L, 10);
        verify(taskRequestDao).getChat(1L, 2L);
        verify(taskRequestDao).insertTask(argThat(task ->
                task.getChatID().equals(existingChatId) &&
                        task.getUserID().equals(1L) &&
                        task.getTaskerID().equals(2L) &&
                        task.getAddressID().equals(3L)
        ));
        verify(taskRequestDao).getTaskDetails(taskId);
        verify(taskRequestDao, never()).insertChat(anyLong(), anyLong());
        verify(emailService).sendTaskerEmail(any());
    }

    @Test
    void testThatRequestTaskSuccessWithNewChat() {
        // Arrange
        Long newChatId = 60L;
        Long taskId = 100L;

        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskRequestDao.getChat(1L, 2L)).thenReturn(Optional.empty());
        when(taskRequestDao.insertChat(1L, 2L)).thenReturn(Optional.of(newChatId));
        when(taskRequestDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        Optional<TaskDto> result = taskRequestService.requestTask(taskRequestDto);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get())
                .isEqualTo(taskDto)
                .hasFieldOrPropertyWithValue("description", "Test task description");

        verify(taskRequestDao).getChat(1L, 2L);
        verify(taskRequestDao).insertChat(1L, 2L);
        verify(taskRequestDao).insertTask(argThat(task -> task.getChatID().equals(newChatId)));
        verify(taskRequestDao).getTaskDetails(taskId);
        verify(emailService).sendTaskerEmail(any());
    }

    @Test
    void testThatRequestTaskThrowsDuplicateRequestException() {
        // Arrange
        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.requestTask(taskRequestDto))
                .isInstanceOf(DuplicateRequestException.class);

        verify(taskRequestDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskRequestDao, never()).checkIfTaskLimit(anyLong(), anyInt());
        verify(taskMapper, never()).getTaskEntity(any());
        verify(taskRequestDao, never()).insertTask(any(TaskEntity.class));
    }

    @Test
    void testThatRequestTaskThrowsRequestLimitExceededException() {
        // Arrange
        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.requestTask(taskRequestDto))
                .isInstanceOf(RequestLimitExceededException.class);

        verify(taskRequestDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskRequestDao).checkIfTaskLimit(1L, 10);
        verify(taskMapper, never()).getTaskEntity(any());
        verify(taskRequestDao, never()).insertTask(any(TaskEntity.class));
    }

    @Test
    void testRequestTask_ThrowsIllegalArgumentException_WhenTaskNotCreated() {
        // Arrange
        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskRequestDao.getChat(1L, 2L)).thenReturn(Optional.of(50L));
        when(taskRequestDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.requestTask(taskRequestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("the task not created correctly");

        verify(taskRequestDao).insertTask(any(TaskEntity.class));
        verify(taskRequestDao, never()).getTaskDetails(anyLong());
    }

    @Test
    void testThatHandleChatWithExistingChat() {
        // Arrange
        Long existingChatId = 50L;
        when(taskRequestDao.getChat(1L, 2L)).thenReturn(Optional.of(existingChatId));

        // Act
        Long result = taskRequestService.handleChat(taskRequestDto);

        // Assert
        assertThat(result).isEqualTo(existingChatId);

        verify(taskRequestDao).getChat(1L, 2L);
        verify(taskRequestDao, never()).insertChat(anyLong(), anyLong());
    }

    @Test
    void testThatHandleChatCreateNewChat() {
        // Arrange
        Long newChatId = 60L;
        when(taskRequestDao.getChat(1L, 2L)).thenReturn(Optional.empty());
        when(taskRequestDao.insertChat(1L, 2L)).thenReturn(Optional.of(newChatId));

        // Act
        Long result = taskRequestService.handleChat(taskRequestDto);

        // Assert
        assertThat(result).isEqualTo(newChatId);

        verify(taskRequestDao, times(1)).getChat(1L, 2L);
        verify(taskRequestDao).insertChat(1L, 2L);
    }

    @Test
    void testThatHandleChatThrowsBadTaskRequestExceptionWhenChatCreationFails() {
        // Arrange
        when(taskRequestDao.getChat(1L, 2L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        when(taskRequestDao.insertChat(1L, 2L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.handleChat(taskRequestDto))
                .isInstanceOf(BadTaskRequestException.class);

        verify(taskRequestDao, times(2)).getChat(1L, 2L);
        verify(taskRequestDao).insertChat(1L, 2L);
    }

    @Test
    void testThatHandleChatRetriesChatRetrievalWhenInsertReturnsEmpty() {
        // Arrange
        Long retrievedChatId = 70L;
        when(taskRequestDao.getChat(1L, 2L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(retrievedChatId));
        when(taskRequestDao.insertChat(1L, 2L)).thenReturn(Optional.empty());

        // Act
        Long result = taskRequestService.handleChat(taskRequestDto);

        // Assert
        assertThat(result).isEqualTo(retrievedChatId);

        verify(taskRequestDao, times(2)).getChat(1L, 2L);
        verify(taskRequestDao).insertChat(1L, 2L);
    }

    @Test
    void testCheckRequest_Success() {
        // Arrange
        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(false);

        // Act & Assert
        assertThatCode(() -> taskRequestService.checkRequest(taskRequestDto))
                .doesNotThrowAnyException();

        verify(taskRequestDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskRequestDao).checkIfTaskLimit(1L, 10);
    }

    @Test
    void testCheckRequest_ThrowsDuplicateRequestException() {
        // Arrange
        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.checkRequest(taskRequestDto))
                .isInstanceOf(DuplicateRequestException.class);

        verify(taskRequestDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskRequestDao, never()).checkIfTaskLimit(anyLong(), anyInt());
    }

    @Test
    void testCheckRequest_ThrowsRequestLimitExceededException() {
        // Arrange
        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> taskRequestService.checkRequest(taskRequestDto))
                .isInstanceOf(RequestLimitExceededException.class);

        verify(taskRequestDao).checkIfTaskExist(1L, 2L, 3L);
        verify(taskRequestDao).checkIfTaskLimit(1L, 10);
    }

    @Test
    void testThatRequestTaskVerifiesChatIDSetCorrectly() {
        // Arrange
        Long chatId = 75L;
        Long taskId = 100L;

        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskRequestDao.getChat(1L, 2L)).thenReturn(Optional.of(chatId));
        when(taskRequestDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        taskRequestService.requestTask(taskRequestDto);

        // Assert
        verify(taskRequestDao).insertTask(argThat(entity -> entity.getChatID().equals(chatId)));
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

        Long taskId = 100L;

        when(taskRequestDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskRequestDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(customEntity);
        when(taskRequestDao.getChat(1L, 2L)).thenReturn(Optional.of(50L));
        when(taskRequestDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskRequestDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        taskRequestService.requestTask(taskRequestDto);

        // Assert
        verify(taskMapper).getTaskEntity(taskRequestDto);
        verify(taskRequestDao).getTaskDetails(taskId);
    }
}

