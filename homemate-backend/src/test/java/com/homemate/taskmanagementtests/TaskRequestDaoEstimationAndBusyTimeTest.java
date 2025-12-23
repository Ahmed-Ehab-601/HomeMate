package com.homemate.taskmanagementtests;

import com.homemate.TaskerProfile.models.TaskerAvailability;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dto.TaskTimeDto;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.mappers.TaskerBusyTimeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskRequestDaoEstimationAndBusyTimeTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private com.homemate.taskmanagement.dao.TaskRowMapper taskRowMapper;

    @Mock
    private TaskerBusyTimeMapper taskerBusyTimeMapper;

    @InjectMocks
    private TaskRequestDao taskRequestDao;

    private Long taskId;
    private Long taskerId;
    private LocalDate testDay;

    @BeforeEach
    void setUp() {
        taskId = 100L;
        taskerId = 2L;
        testDay = LocalDate.of(2025, 11, 20);
    }

    // ========== addEstimation Tests ==========

    @Test
    void testAddEstimation_Success() {
        int estimationMinutes = 120;

        when(jdbcTemplate.update(anyString(), eq(estimationMinutes), eq(taskId))).thenReturn(1);

        assertThatCode(() -> taskRequestDao.addEstimation(taskId, estimationMinutes))
                .doesNotThrowAnyException();

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).update(
                sqlCaptor.capture(),
                eq(estimationMinutes),
                eq(taskId)
        );

        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("UPDATE Task");
        assertThat(sql).contains("SET estimation = ?");
        assertThat(sql).contains("WHERE taskID = ?");
    }

    @Test
    void testAddEstimation_WithDifferentEstimationValues() {
        int[] estimations = {30, 60, 120, 240, 480, 1440};

        when(jdbcTemplate.update(anyString(), anyInt(), eq(taskId))).thenReturn(1);

        for (int estimation : estimations) {
            assertThatCode(() -> taskRequestDao.addEstimation(taskId, estimation))
                    .doesNotThrowAnyException();
        }

        verify(jdbcTemplate, times(estimations.length)).update(anyString(), anyInt(), eq(taskId));
    }

    @Test
    void testAddEstimation_WithZeroEstimation() {
        int estimationMinutes = 0;

        when(jdbcTemplate.update(anyString(), eq(estimationMinutes), eq(taskId))).thenReturn(1);

        assertThatCode(() -> taskRequestDao.addEstimation(taskId, estimationMinutes))
                .doesNotThrowAnyException();

        verify(jdbcTemplate).update(anyString(), eq(0), eq(taskId));
    }

    @Test
    void testAddEstimation_WithLargeEstimation() {
        int estimationMinutes = 2880;

        when(jdbcTemplate.update(anyString(), eq(estimationMinutes), eq(taskId))).thenReturn(1);

        assertThatCode(() -> taskRequestDao.addEstimation(taskId, estimationMinutes))
                .doesNotThrowAnyException();

        verify(jdbcTemplate).update(anyString(), eq(2880), eq(taskId));
    }

    // ========== getBusytime Tests ==========

    @Test
    void testGetBusytime_Success_WithTasks() {
        List<TaskTimeDto> expectedBusyTimes = new ArrayList<>();
        expectedBusyTimes.add(TaskTimeDto.builder()
                .taskID(1L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .estimation(120)
                .build());
        expectedBusyTimes.add(TaskTimeDto.builder()
                .taskID(2L)
                .startDate(LocalDateTime.of(2025, 11, 20, 14, 0))
                .estimation(60)
                .build());

        LocalDateTime startOfDay = testDay.atStartOfDay();
        LocalDateTime endOfDay = testDay.plusDays(1).atStartOfDay();

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        )).thenReturn(expectedBusyTimes);

        List<TaskTimeDto> result = taskRequestDao.getBusytime(taskerId, testDay);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTaskID()).isEqualTo(1L);
        assertThat(result.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 11, 20, 10, 0));
        assertThat(result.get(0).getEstimation()).isEqualTo(120);
        assertThat(result.get(1).getTaskID()).isEqualTo(2L);
        assertThat(result.get(1).getStartDate()).isEqualTo(LocalDateTime.of(2025, 11, 20, 14, 0));
        assertThat(result.get(1).getEstimation()).isEqualTo(60);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).query(
                sqlCaptor.capture(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        );

        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("SELECT");
        assertThat(sql).contains("startDate");
        assertThat(sql).contains("estimation");
        assertThat(sql).contains("FROM Task");
        assertThat(sql).contains("WHERE taskerID = ?");
        assertThat(sql).contains("startDate >= ?");
        assertThat(sql).contains("startDate < ?");
    }

    @Test
    void testGetBusytime_ReturnsEmptyList_WhenNoTasks() {
        List<TaskTimeDto> emptyBusyTimes = new ArrayList<>();
        LocalDateTime startOfDay = testDay.atStartOfDay();
        LocalDateTime endOfDay = testDay.plusDays(1).atStartOfDay();

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        )).thenReturn(emptyBusyTimes);

        List<TaskTimeDto> result = taskRequestDao.getBusytime(taskerId, testDay);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        );
    }

    @Test
    void testGetBusytime_WithDifferentDays() {
        LocalDate day1 = LocalDate.of(2025, 11, 20);
        LocalDate day2 = LocalDate.of(2025, 11, 21);

        List<TaskTimeDto> busyTimesDay1 = new ArrayList<>();
        busyTimesDay1.add(TaskTimeDto.builder()
                .taskID(1L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .estimation(120)
                .build());

        List<TaskTimeDto> busyTimesDay2 = new ArrayList<>();
        busyTimesDay2.add(TaskTimeDto.builder()
                .taskID(2L)
                .startDate(LocalDateTime.of(2025, 11, 21, 14, 0))
                .estimation(60)
                .build());

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(busyTimesDay1, busyTimesDay2);

        List<TaskTimeDto> result1 = taskRequestDao.getBusytime(taskerId, day1);
        List<TaskTimeDto> result2 = taskRequestDao.getBusytime(taskerId, day2);

        assertThat(result1).hasSize(1);
        assertThat(result1.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 11, 20, 10, 0));
        assertThat(result1.get(0).getEstimation()).isEqualTo(120);

        assertThat(result2).hasSize(1);
        assertThat(result2.get(0).getStartDate()).isEqualTo(LocalDateTime.of(2025, 11, 21, 14, 0));
        assertThat(result2.get(0).getEstimation()).isEqualTo(60);

        verify(jdbcTemplate, times(2)).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                any(Timestamp.class),
                any(Timestamp.class)
        );
    }

    @Test
    void testGetBusytime_WithMultipleTasksOnSameDay() {
        List<TaskTimeDto> expectedBusyTimes = new ArrayList<>();
        expectedBusyTimes.add(TaskTimeDto.builder()
                .taskID(1L)
                .startDate(LocalDateTime.of(2025, 11, 20, 8, 0))
                .estimation(60)
                .build());
        expectedBusyTimes.add(TaskTimeDto.builder()
                .taskID(2L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .estimation(120)
                .build());
        expectedBusyTimes.add(TaskTimeDto.builder()
                .taskID(3L)
                .startDate(LocalDateTime.of(2025, 11, 20, 14, 0))
                .estimation(90)
                .build());
        expectedBusyTimes.add(TaskTimeDto.builder()
                .taskID(4L)
                .startDate(LocalDateTime.of(2025, 11, 20, 16, 0))
                .estimation(30)
                .build());

        LocalDateTime startOfDay = testDay.atStartOfDay();
        LocalDateTime endOfDay = testDay.plusDays(1).atStartOfDay();

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(startOfDay)),
                eq(Timestamp.valueOf(endOfDay))
        )).thenReturn(expectedBusyTimes);

        List<TaskTimeDto> result = taskRequestDao.getBusytime(taskerId, testDay);

        assertThat(result).hasSize(4);
        assertThat(result).isEqualTo(expectedBusyTimes);
    }

    @Test
    void testGetBusytime_VerifiesCorrectDateRange() {
        LocalDate day = LocalDate.of(2025, 11, 20);
        LocalDateTime expectedStartOfDay = day.atStartOfDay();
        LocalDateTime expectedEndOfDay = day.plusDays(1).atStartOfDay();

        List<TaskTimeDto> emptyBusyTimes = new ArrayList<>();
        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(emptyBusyTimes);

        taskRequestDao.getBusytime(taskerId, day);

        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId),
                eq(Timestamp.valueOf(expectedStartOfDay)),
                eq(Timestamp.valueOf(expectedEndOfDay))
        );
    }

    @Test
    void testGetBusytime_WithDifferentTaskerIds() {
        Long taskerId1 = 1L;
        Long taskerId2 = 2L;

        List<TaskTimeDto> busyTimes1 = new ArrayList<>();
        busyTimes1.add(TaskTimeDto.builder()
                .taskID(1L)
                .startDate(LocalDateTime.of(2025, 11, 20, 10, 0))
                .estimation(120)
                .build());

        List<TaskTimeDto> busyTimes2 = new ArrayList<>();
        busyTimes2.add(TaskTimeDto.builder()
                .taskID(2L)
                .startDate(LocalDateTime.of(2025, 11, 20, 14, 0))
                .estimation(60)
                .build());

        when(jdbcTemplate.query(
                anyString(),
                eq(taskerBusyTimeMapper),
                anyLong(),
                any(Timestamp.class),
                any(Timestamp.class)
        )).thenReturn(busyTimes1, busyTimes2);

        List<TaskTimeDto> result1 = taskRequestDao.getBusytime(taskerId1, testDay);
        List<TaskTimeDto> result2 = taskRequestDao.getBusytime(taskerId2, testDay);

        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);

        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId1),
                any(Timestamp.class),
                any(Timestamp.class)
        );
        verify(jdbcTemplate).query(
                anyString(),
                eq(taskerBusyTimeMapper),
                eq(taskerId2),
                any(Timestamp.class),
                any(Timestamp.class)
        );
    }

    @Test
    void testCheckAvailability_ReturnsAvailable() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(taskerId)))
                .thenReturn("AVAILABLE");

        TaskerAvailability result = taskRequestDao.CheckAvailability(taskerId);

        assertThat(result).isEqualTo(TaskerAvailability.AVAILABLE);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(String.class), eq(taskerId));

        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("SELECT availability");
        assertThat(sql).contains("FROM Tasker");
        assertThat(sql).contains("WHERE taskerID = ?");
    }

    @Test
    void testCheckAvailability_ReturnsUnavailable() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(taskerId)))
                .thenReturn("UNAVAILABLE");

        TaskerAvailability result = taskRequestDao.CheckAvailability(taskerId);

        assertThat(result).isEqualTo(TaskerAvailability.UNAVAILABLE);
        verify(jdbcTemplate).queryForObject(anyString(), eq(String.class), eq(taskerId));
    }

    @Test
    void testCheckAvailability_ReturnsUnavailable_WhenNull() {
        when(jdbcTemplate.queryForObject(anyString(), eq(String.class), eq(taskerId)))
                .thenReturn(null);

        TaskerAvailability result = taskRequestDao.CheckAvailability(taskerId);

        assertThat(result).isEqualTo(TaskerAvailability.UNAVAILABLE);
        verify(jdbcTemplate).queryForObject(anyString(), eq(String.class), eq(taskerId));
    }

    @Test
    void testGetEstimation_ReturnsEstimation_WhenTaskExists() {
        int expectedEstimation = 120;

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(taskId)))
                .thenReturn(expectedEstimation);

        int result = taskRequestDao.getEstimation(taskId);

        assertThat(result).isEqualTo(expectedEstimation);

        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate).queryForObject(sqlCaptor.capture(), eq(Integer.class), eq(taskId));

        String sql = sqlCaptor.getValue();
        assertThat(sql).contains("SELECT estimation");
        assertThat(sql).contains("FROM Task");
        assertThat(sql).contains("WHERE taskID = ?");
    }

    @Test
    void testGetEstimation_ReturnsZero_WhenEstimationIsNull() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(taskId)))
                .thenReturn(null);

        int result = taskRequestDao.getEstimation(taskId);

        assertThat(result).isEqualTo(0);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(taskId));
    }

    @Test
    void testGetEstimation_ReturnsZero_WhenTaskNotFound() {
        Long nonExistentTaskId = 999L;

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(nonExistentTaskId)))
                .thenThrow(new EmptyResultDataAccessException(1));

        int result = taskRequestDao.getEstimation(nonExistentTaskId);

        assertThat(result).isEqualTo(0);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(nonExistentTaskId));
    }

    @Test
    void testAddEstimation_ThrowsException_WhenTaskNotFound() {
        int estimationMinutes = 120;
        Long nonExistentTaskId = 999L;

        when(jdbcTemplate.update(anyString(), eq(estimationMinutes), eq(nonExistentTaskId)))
                .thenReturn(0);

        assertThatThrownBy(() -> taskRequestDao.addEstimation(nonExistentTaskId, estimationMinutes))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task not found with ID: " + nonExistentTaskId);

        verify(jdbcTemplate).update(anyString(), eq(estimationMinutes), eq(nonExistentTaskId));
    }
}