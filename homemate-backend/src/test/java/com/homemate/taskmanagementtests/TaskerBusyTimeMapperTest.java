package com.homemate.taskmanagementtests;

import com.homemate.taskmanagement.dto.TaskTimeDto;
import com.homemate.taskmanagement.mappers.TaskerBusyTimeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskerBusyTimeMapperTest {

    private TaskerBusyTimeMapper mapper;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        mapper = new TaskerBusyTimeMapper();
        resultSet = mock(ResultSet.class);
    }

    // ========== extractData Tests ==========

    @Test
    void testExtractData_Success_WithSingleRow() throws SQLException {
        // Arrange
        Long taskId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 20, 10, 0);
        int estimation = 120;

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("taskID")).thenReturn(taskId);
        when(resultSet.getTimestamp("startDate")).thenReturn(Timestamp.valueOf(startDate));
        when(resultSet.getInt("estimation")).thenReturn(estimation);

        // Act
        List<TaskTimeDto> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTaskID()).isEqualTo(taskId);
        assertThat(result.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(result.get(0).getEstimation()).isEqualTo(estimation);

        verify(resultSet, times(2)).next();
        verify(resultSet).getLong("taskID");
        verify(resultSet).getTimestamp("startDate");
        verify(resultSet).getInt("estimation");
    }

    @Test
    void testExtractData_Success_WithMultipleRows() throws SQLException {
        // Arrange
        LocalDateTime startDate1 = LocalDateTime.of(2025, 11, 20, 10, 0);
        LocalDateTime startDate2 = LocalDateTime.of(2025, 11, 20, 14, 0);
        LocalDateTime startDate3 = LocalDateTime.of(2025, 11, 20, 16, 30);

        when(resultSet.next()).thenReturn(true, true, true, false);
        when(resultSet.getLong("taskID")).thenReturn(1L, 2L, 3L);
        when(resultSet.getTimestamp("startDate"))
                .thenReturn(Timestamp.valueOf(startDate1))
                .thenReturn(Timestamp.valueOf(startDate2))
                .thenReturn(Timestamp.valueOf(startDate3));
        when(resultSet.getInt("estimation"))
                .thenReturn(120)
                .thenReturn(60)
                .thenReturn(90);

        // Act
        List<TaskTimeDto> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        assertThat(result.get(0).getTaskID()).isEqualTo(1L);
        assertThat(result.get(0).getStartDate()).isEqualTo(startDate1);
        assertThat(result.get(0).getEstimation()).isEqualTo(120);

        assertThat(result.get(1).getTaskID()).isEqualTo(2L);
        assertThat(result.get(1).getStartDate()).isEqualTo(startDate2);
        assertThat(result.get(1).getEstimation()).isEqualTo(60);

        assertThat(result.get(2).getTaskID()).isEqualTo(3L);
        assertThat(result.get(2).getStartDate()).isEqualTo(startDate3);
        assertThat(result.get(2).getEstimation()).isEqualTo(90);

        verify(resultSet, times(4)).next();
        verify(resultSet, times(3)).getLong("taskID");
        verify(resultSet, times(3)).getTimestamp("startDate");
        verify(resultSet, times(3)).getInt("estimation");
    }

    @Test
    void testExtractData_ReturnsEmptyList_WhenNoRows() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false);

        // Act
        List<TaskTimeDto> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(resultSet).next();
        verify(resultSet, never()).getLong(anyString());
        verify(resultSet, never()).getTimestamp(anyString());
        verify(resultSet, never()).getInt(anyString());
    }

    @Test
    void testExtractData_WithZeroEstimation() throws SQLException {
        // Arrange
        Long taskId = 5L;
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 20, 10, 0);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("taskID")).thenReturn(taskId);
        when(resultSet.getTimestamp("startDate")).thenReturn(Timestamp.valueOf(startDate));
        when(resultSet.getInt("estimation")).thenReturn(0);

        // Act
        List<TaskTimeDto> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTaskID()).isEqualTo(taskId);
        assertThat(result.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(result.get(0).getEstimation()).isEqualTo(0);
    }

    @Test
    void testExtractData_WithVariousTimeSlots() throws SQLException {
        // Arrange
        LocalDateTime morning = LocalDateTime.of(2025, 11, 20, 8, 0);
        LocalDateTime noon = LocalDateTime.of(2025, 11, 20, 12, 0);
        LocalDateTime afternoon = LocalDateTime.of(2025, 11, 20, 15, 30);
        LocalDateTime evening = LocalDateTime.of(2025, 11, 20, 18, 45);

        when(resultSet.next()).thenReturn(true, true, true, true, false);
        when(resultSet.getLong("taskID")).thenReturn(10L, 11L, 12L, 13L);
        when(resultSet.getTimestamp("startDate"))
                .thenReturn(Timestamp.valueOf(morning))
                .thenReturn(Timestamp.valueOf(noon))
                .thenReturn(Timestamp.valueOf(afternoon))
                .thenReturn(Timestamp.valueOf(evening));
        when(resultSet.getInt("estimation"))
                .thenReturn(60)
                .thenReturn(120)
                .thenReturn(90)
                .thenReturn(30);

        // Act
        List<TaskTimeDto> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(4);

        assertThat(result.get(0).getTaskID()).isEqualTo(10L);
        assertThat(result.get(0).getStartDate()).isEqualTo(morning);
        assertThat(result.get(0).getEstimation()).isEqualTo(60);

        assertThat(result.get(1).getTaskID()).isEqualTo(11L);
        assertThat(result.get(1).getStartDate()).isEqualTo(noon);
        assertThat(result.get(1).getEstimation()).isEqualTo(120);

        assertThat(result.get(2).getTaskID()).isEqualTo(12L);
        assertThat(result.get(2).getStartDate()).isEqualTo(afternoon);
        assertThat(result.get(2).getEstimation()).isEqualTo(90);

        assertThat(result.get(3).getTaskID()).isEqualTo(13L);
        assertThat(result.get(3).getStartDate()).isEqualTo(evening);
        assertThat(result.get(3).getEstimation()).isEqualTo(30);
    }

    @Test
    void testExtractData_ThrowsSQLException_WhenColumnNotFound() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("taskID")).thenThrow(new SQLException("Column not found"));

        // Act & Assert
        assertThatThrownBy(() -> mapper.extractData(resultSet))
                .isInstanceOf(SQLException.class)
                .hasMessage("Column not found");

        verify(resultSet).next();
        verify(resultSet).getLong("taskID");
    }

    @Test
    void testExtractData_WithNullEstimation() throws SQLException {
        // Arrange
        Long taskId = 7L;
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 20, 10, 0);

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("taskID")).thenReturn(taskId);
        when(resultSet.getTimestamp("startDate")).thenReturn(Timestamp.valueOf(startDate));
        when(resultSet.getInt("estimation")).thenReturn(0); // SQL NULL returns 0 for getInt

        // Act
        List<TaskTimeDto> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTaskID()).isEqualTo(taskId);
        assertThat(result.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(result.get(0).getEstimation()).isEqualTo(0);
    }

    @Test
    void testExtractData_WithLargeEstimation() throws SQLException {
        // Arrange
        Long taskId = 8L;
        LocalDateTime startDate = LocalDateTime.of(2025, 11, 20, 10, 0);
        int largeEstimation = 2880; // 48 hours

        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("taskID")).thenReturn(taskId);
        when(resultSet.getTimestamp("startDate")).thenReturn(Timestamp.valueOf(startDate));
        when(resultSet.getInt("estimation")).thenReturn(largeEstimation);

        // Act
        List<TaskTimeDto> result = mapper.extractData(resultSet);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTaskID()).isEqualTo(taskId);
        assertThat(result.get(0).getStartDate()).isEqualTo(startDate);
        assertThat(result.get(0).getEstimation()).isEqualTo(largeEstimation);
    }
}