package com.homemate;

import com.homemate.dao.TaskerDao;
import com.homemate.dto.FindTaskerCriteriaDto;
import com.homemate.dto.TaskerCardDto;
import com.homemate.mapper.TaskerCardMapper;
import com.homemate.model.Tasker;
import com.homemate.service.TaskerDiscoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class TaskerDiscoveryServiceTest {

    @Mock
    private TaskerDao taskerDao;

    @Mock
    private TaskerCardMapper taskerCardMapper;

    @InjectMocks
    private TaskerDiscoveryService taskerDiscoveryService;

    private FindTaskerCriteriaDto criteria;
    private Tasker tasker1;
    private Tasker tasker2;
    private TaskerCardDto dto1;
    private TaskerCardDto dto2;

    @BeforeEach
    void setUp() {
        criteria = new FindTaskerCriteriaDto();
        criteria.setServiceID(1);
        criteria.setMinRating(4.0);
        criteria.setMaxRating(5.0);

        tasker1 = new Tasker();
        tasker1.setTaskerID(1);
        tasker1.setFirstName("John");
        tasker1.setLastName("Doe");
        tasker1.setRating(4.5);

        tasker2 = new Tasker();
        tasker2.setTaskerID(2);
        tasker2.setFirstName("Jane");
        tasker2.setLastName("Smith");
        tasker2.setRating(4.8);

        dto1 = new TaskerCardDto();
        dto1.setTaskerId(1);
        dto1.setFirstName("John");

        dto2 = new TaskerCardDto();
        dto2.setTaskerId(2);
        dto2.setFirstName("Jane");
    }

    @Test
    void getTaskerCardsByFiltersShouldReturnTaskerList() {

        List<Tasker> taskers = Arrays.asList(tasker1, tasker2);
        when(taskerDao.findTaskersWithFilters(criteria, 1, 10)).thenReturn(taskers);
        when(taskerCardMapper.mapToDto(tasker1)).thenReturn(dto1);
        when(taskerCardMapper.mapToDto(tasker2)).thenReturn(dto2);

        List<TaskerCardDto> result = taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Jane", result.get(1).getFirstName());
        verify(taskerDao, times(1)).findTaskersWithFilters(criteria, 1, 10);
    }

    @Test
    void getTaskerCardsByFiltersShouldThrowExceptionWhenFilterIsNull() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerDiscoveryService.getTaskerCardsByFilters(null, 1, 10);
        });
        assertEquals("Filter cannot be null", exception.getMessage());
    }

    @Test
    void getTaskerCardsByFiltersShouldThrowExceptionWhenServiceIdIsNull() {

        criteria.setServiceID(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10);
        });
        assertEquals("Service ID is required", exception.getMessage());
    }

    @Test
    void getTaskerCardsByFiltersShouldThrowExceptionWhenMinRatingGreaterThanMaxRating() {

        criteria.setMinRating(5.0);
        criteria.setMaxRating(3.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10);
        });
        assertEquals("minRating cannot be greater than maxRating", exception.getMessage());
    }

    @Test
    void getTaskerCardsByFiltersShouldThrowExceptionWhenMinHourRateGreaterThanMaxHourRate() {

        criteria.setMinHourRate(100.0);
        criteria.setMaxHourRate(50.0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10);
        });
        assertEquals("minHourRate cannot be greater than maxHourRate", exception.getMessage());
    }

    @Test
    void getTaskerCardsByFiltersShouldReturnEmptyListWhenNoTaskersFound() {

        when(taskerDao.findTaskersWithFilters(criteria, 1, 10)).thenReturn(Arrays.asList());

        List<TaskerCardDto> result = taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
