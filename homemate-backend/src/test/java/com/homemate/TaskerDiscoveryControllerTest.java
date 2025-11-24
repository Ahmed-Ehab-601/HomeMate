package com.homemate;

import com.homemate.controller.TaskerDiscoveryController;
import com.homemate.dto.FindTaskerCriteriaDto;
import com.homemate.dto.TaskerCardDto;
import com.homemate.service.TaskerDiscoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class TaskerDiscoveryControllerTest {

    @Mock
    private TaskerDiscoveryService taskerDiscoveryService;

    @InjectMocks
    private TaskerDiscoveryController taskerDiscoveryController;

    private FindTaskerCriteriaDto criteria;
    private TaskerCardDto dto;

    @BeforeEach
    void setUp() {
        criteria = new FindTaskerCriteriaDto();
        criteria.setServiceID(1);

        dto = new TaskerCardDto();
        dto.setTaskerId(1);
        dto.setFirstName("John");
    }

    @Test
    void findTaskers_ShouldReturnOkWithTaskerList() {
        List<TaskerCardDto> dtos = Arrays.asList(dto);
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(dtos);

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("John", response.getBody().get(0).getFirstName());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }

    @Test
    void findTaskers_ShouldReturnEmptyList_WhenNoTaskersFound() {
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList());

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void findTaskers_ShouldHandleDifferentPageSizes() {
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 2, 20)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 2, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 2, 20);
    }


    @Test
    void findTaskers_ShouldFilterByAvailability() {
        criteria.setAvailability("available");
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }

    @Test
    void findTaskers_ShouldFilterByGender() {
        criteria.setGender("M");
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().get(0).getFirstName());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }

    @Test
    void findTaskers_ShouldFilterByRatingRange() {
        criteria.setMinRating(3.5);
        criteria.setMaxRating(5.0);
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }

    @Test
    void findTaskers_ShouldFilterByHourlyRate() {
        criteria.setMinHourRate(10.0);
        criteria.setMaxHourRate(50.0);
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }

    @Test
    void findTaskers_ShouldFilterByCity() {
        criteria.setCity("Cairo");
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John", response.getBody().get(0).getFirstName());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }

    @Test
    void findTaskers_ShouldFilterBySearchTerm() {
        criteria.setSearch("John");
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("John", response.getBody().get(0).getFirstName());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }

    @Test
    void findTaskers_ShouldSortByNameAscending() {
        criteria.setSortBy("name");
        criteria.setSortOrder("asc");
        when(taskerDiscoveryService.getTaskerCardsByFilters(criteria, 1, 10)).thenReturn(Arrays.asList(dto));

        ResponseEntity<List<TaskerCardDto>> response = taskerDiscoveryController.findTaskers(criteria, 1, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(taskerDiscoveryService, times(1)).getTaskerCardsByFilters(criteria, 1, 10);
    }
}
