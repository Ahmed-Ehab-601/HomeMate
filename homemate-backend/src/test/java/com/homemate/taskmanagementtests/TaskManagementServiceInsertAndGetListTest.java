package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.notification.service.imp.EmailServiceImp;
import com.homemate.reviews.service.ReviewsService;
import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.exceptions.*;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementServiceInsertAndGetListTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskDaoImpl taskDao;

    @Mock
    private EmailServiceImp emailServiceImp;

    @Mock
    private ReviewDao reviewDao;

    @InjectMocks
    private TaskManagementService taskManagementService;
    @InjectMocks
    private ReviewsService reviewService;

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

        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(taskEntity);
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.of(existingChatId));
        when(taskDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(1L));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(2L));
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
        verify(taskDao, atLeastOnce()).getTaskDetails(taskId); // fixed
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
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(1L));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(2L));
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
        verify(taskDao, atLeastOnce()).getTaskDetails(taskId); // fixed
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
        Long viewerId = 200L; // the user requesting the task details
        Long userId = 200L;   // owner user
        Long taskerId = 300L; // assigned tasker

        // mocks for authorization check
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(userId));

        // mock actual task details
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        Optional<TaskDto> result = taskManagementService.getTaskDetails(taskId, viewerId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get())
                .extracting(TaskDto::getTaskID, TaskDto::getUserName, TaskDto::getTaskerMail)
                .containsExactly(100L, "John Doe", "jane@example.com");

        // Verify all DAO calls
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao).getUserID(taskId);
        verify(taskDao).getTaskDetails(taskId);
    }

    @Test
    void testGetTaskDetails_ReturnsEmpty() {
        // Arrange
        Long taskId = 999L;
        Long viewerId = 200L;   // user requesting details
        Long userId = 200L;     // owner user
        Long taskerId = 300L;   // assigned tasker

        // Required authorization mocks
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(userId));

        // Task not found
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.empty());

        // Act
        Optional<TaskDto> result = taskManagementService.getTaskDetails(taskId, viewerId);

        // Assert
        assertThat(result).isEmpty();

        // Verify all interactions
        verify(taskDao).getTaskerID(taskId);
        verify(taskDao).getUserID(taskId);
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
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(1L));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(2L));
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

        Long taskId = 100L;
        Long userId = 1L;
        Long taskerId = 2L;

        when(taskDao.checkIfTaskExist(1L, 2L, 3L)).thenReturn(false);
        when(taskDao.checkIfTaskLimit(1L, 10)).thenReturn(false);
        when(taskMapper.getTaskEntity(taskRequestDto)).thenReturn(customEntity);
        when(taskDao.getChat(1L, 2L)).thenReturn(Optional.of(50L));
        when(taskDao.insertTask(any(TaskEntity.class))).thenReturn(Optional.of(taskId));
        when(taskDao.getUserID(taskId)).thenReturn(Optional.of(userId));
        when(taskDao.getTaskerID(taskId)).thenReturn(Optional.of(taskerId));
        when(taskDao.getTaskDetails(taskId)).thenReturn(Optional.of(taskDto));

        // Act
        taskManagementService.requestTask(taskRequestDto);

        // Assert
        verify(taskMapper).getTaskEntity(taskRequestDto);
        verify(taskDao, atLeastOnce()).getTaskDetails(taskId); // fixed
    }

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
        Optional<PaginatedResponse> result = taskManagementService.getUserTasks(userId, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getUserTasks(userId, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getUserTasks(userId, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getUserTasks(userId, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getUserTasks(userId, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getTaskerTasks(taskerID, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getTaskerTasks(taskerID, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getTaskerTasks(taskerID, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getTaskerTasks(taskerID, status, page, pageSize);

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
        Optional<PaginatedResponse> result = taskManagementService.getTaskerTasks(taskerID, status, page, pageSize);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTotalPages()).isEqualTo(1); // Math.ceilDiv(5, 10) = 1
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