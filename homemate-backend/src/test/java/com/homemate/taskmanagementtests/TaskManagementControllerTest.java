package com.homemate.taskmanagementtests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homemate.taskmanagement.controller.GlobalExceptionHandler;
import com.homemate.taskmanagement.controller.TaskManagementController;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskRequestDto;
import com.homemate.taskmanagement.exceptions.BadTaskRequestException;
import com.homemate.taskmanagement.exceptions.DuplicateRequestException;
import com.homemate.taskmanagement.exceptions.RequestLimitExceededException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.TaskManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(MockitoExtension.class)
class TaskManagementControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskManagementService taskManagementService;

    @InjectMocks
    private TaskManagementController taskManagementController;

    private ObjectMapper objectMapper;
    private TaskRequestDto validTaskRequest;
    private TaskDto taskDto;

    @BeforeEach
    void setUp() {
        // Setup MockMvc with the controller and exception handler
        mockMvc = MockMvcBuilders.standaloneSetup(taskManagementController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        validTaskRequest = new TaskRequestDto();
        validTaskRequest.setUserID(1L);
        validTaskRequest.setTaskerID(2L);
        validTaskRequest.setAddressID(3L);
        validTaskRequest.setServiceID(4L);
        validTaskRequest.setDescription("Test task description");
        validTaskRequest.setStartDate(LocalDateTime.of(2025, 11, 20, 10, 0));

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
    void testThatRequestTaskReturnsCreatedWhenValidRequest() throws Exception {
        // Arrange
        when(taskManagementService.requestTask(any(TaskRequestDto.class)))
                .thenReturn(Optional.of(taskDto));

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.taskID", is(100)))
                .andExpect(jsonPath("$.userName", is("John Doe")))
                .andExpect(jsonPath("$.taskerName", is("Jane Smith")))
                .andExpect(jsonPath("$.description", is("Test task description")))
                .andExpect(jsonPath("$.serviceName", is("Plumbing")))
                .andExpect(jsonPath("$.chatID", is(50)))
                .andExpect(jsonPath("$.addressDetails", is("123 Main St")));
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWhenServiceReturnsEmpty() throws Exception {
        // Arrange
        when(taskManagementService.requestTask(any(TaskRequestDto.class)))
                .thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWhenUserIDIsNull() throws Exception {
        // Arrange
        validTaskRequest.setUserID(null);

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWhenTaskerIDIsNull() throws Exception {
        // Arrange
        validTaskRequest.setTaskerID(null);

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWhenServiceIDIsNull() throws Exception {
        // Arrange
        validTaskRequest.setServiceID(null);

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWhenAddressIDIsNull() throws Exception {
        // Arrange
        validTaskRequest.setAddressID(null);

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWhenStartDateIsNull() throws Exception {
        // Arrange
        validTaskRequest.setStartDate(null);

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void testThatRequestTaskHandlesDuplicateRequestException() throws Exception {
        // Arrange
        when(taskManagementService.requestTask(any(TaskRequestDto.class)))
                .thenThrow(new DuplicateRequestException());

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("DUPLICATE_REQUEST")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testThatRequestTaskHandlesRequestLimitExceededException() throws Exception {
        // Arrange
        when(taskManagementService.requestTask(any(TaskRequestDto.class)))
                .thenThrow(new RequestLimitExceededException());

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error", is("REQUEST_LIMIT_EXCEEDED")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testThatRequestTaskHandlesBadTaskRequestException() throws Exception {
        // Arrange
        when(taskManagementService.requestTask(any(TaskRequestDto.class)))
                .thenThrow(new BadTaskRequestException());

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Task Not Created")))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testThatRequestTaskAcceptsValidRequestWithoutDescription() throws Exception {
        // Arrange
        validTaskRequest.setDescription(null);
        when(taskManagementService.requestTask(any(TaskRequestDto.class)))
                .thenReturn(Optional.of(taskDto));

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.taskID", is(100)));
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWithInvalidJson() throws Exception {
        // Arrange
        String invalidJson = "{\"userID\": \"not a number\"}";

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testThatRequestTaskReturnsBadRequestWithEmptyBody() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException())
                        .isInstanceOf(MethodArgumentNotValidException.class));
    }

    @Test
    void testThatRequestTaskVerifiesCorrectEndpoint() throws Exception {
        // Arrange
        when(taskManagementService.requestTask(any(TaskRequestDto.class)))
                .thenReturn(Optional.of(taskDto));

        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isCreated());

        // Verify wrong endpoint returns 404
        mockMvc.perform(post("/task/wrong-endpoint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testThatRequestTaskVerifiesContentTypeIsRequired() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/user/task/request")
                        .content(objectMapper.writeValueAsString(validTaskRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }
}