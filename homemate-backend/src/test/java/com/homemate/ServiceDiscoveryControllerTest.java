package com.homemate;

import com.homemate.controller.ServiceDiscoveryController;
import com.homemate.dto.ServiceDto;
import com.homemate.service.ServiceDiscoveryService;
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

class ServiceDiscoveryControllerTest {

    @Mock
    private ServiceDiscoveryService serviceDiscoveryService;

    @InjectMocks
    private ServiceDiscoveryController serviceDiscoveryController;

    private List<ServiceDto> serviceList;

    @BeforeEach
    void setUp() {

        ServiceDto service1 = new ServiceDto();
        service1.setServiceId(1);
        service1.setServiceName("Plumbing");
        service1.setTotalTasks(10);

        ServiceDto service2 = new ServiceDto();
        service2.setServiceId(2);
        service2.setServiceName("Electrician");
        service2.setTotalTasks(8);

        ServiceDto service3 = new ServiceDto();
        service3.setServiceId(3);
        service3.setServiceName("Carpentry");
        service3.setTotalTasks(12);

        ServiceDto service4 = new ServiceDto();
        service4.setServiceId(4);
        service4.setServiceName("Cleaning");
        service4.setTotalTasks(20);

        ServiceDto service5 = new ServiceDto();
        service5.setServiceId(5);
        service5.setServiceName("Painting");
        service5.setTotalTasks(5);

        serviceList = Arrays.asList(service1, service2, service3, service4, service5);
    }

    @Test
    void getAllServicesShouldReturnOkWithFiveServices() {

        when(serviceDiscoveryService.getAllServices()).thenReturn(serviceList);
        ResponseEntity<List<ServiceDto>> response = serviceDiscoveryController.getAllServices();


        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().size());
        assertEquals("Plumbing", response.getBody().get(0).getServiceName());
        assertEquals("Electrician", response.getBody().get(1).getServiceName());
        assertEquals("Carpentry", response.getBody().get(2).getServiceName());
        assertEquals("Cleaning", response.getBody().get(3).getServiceName());
        assertEquals("Painting", response.getBody().get(4).getServiceName());

        verify(serviceDiscoveryService, times(1)).getAllServices();
    }

    @Test
    void getAllServicesShouldReturnEmptyList() {

        when(serviceDiscoveryService.getAllServices()).thenReturn(Arrays.asList());
        ResponseEntity<List<ServiceDto>> response = serviceDiscoveryController.getAllServices();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());

        verify(serviceDiscoveryService, times(1)).getAllServices();
    }
}
