package com.homemate.taskerdiscovery;
import com.homemate.taskerdiscovery.dao.ServiceDao;
import com.homemate.taskerdiscovery.mapper.ServiceMapper;
import com.homemate.taskerdiscovery.dto.ServiceDto;
import com.homemate.taskerdiscovery.model.Service;
import com.homemate.taskerdiscovery.service.ServiceDiscoveryService;
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

class ServiceDiscoveryServiceTest {

    @Mock
    private ServiceDao serviceDao;

    @Mock
    private ServiceMapper serviceMapper;

    @InjectMocks
    private ServiceDiscoveryService serviceDiscoveryService;

    private Service testService1;
    private Service testService2;
    private ServiceDto testServiceDto1;
    private ServiceDto testServiceDto2;

    @BeforeEach
    void setUp() {
        testService1 = new Service(1, "Plumbing", "Plumbing services",
                "1234", "plumbing.jpg", "image/jpeg");
        testService2 = new Service(2, "Cleaning", "Cleaning services",
                "1234", "cleaning.jpg", "image/jpeg");

        testServiceDto1 = new ServiceDto();
        testServiceDto1.setServiceId(1);
        testServiceDto1.setServiceName("Plumbing");
        testServiceDto1.setTotalTasks(10);

        testServiceDto2 = new ServiceDto();
        testServiceDto2.setServiceId(2);
        testServiceDto2.setServiceName("Cleaning");
        testServiceDto2.setTotalTasks(15);
    }

    @Test
    void getAllServices_ShouldReturnListOfServiceDtos() {

        List<Service> services = Arrays.asList(testService1, testService2);
        when(serviceDao.getAllServices()).thenReturn(services);
        when(serviceDao.getTotalTasksForService(1)).thenReturn(10);
        when(serviceDao.getTotalTasksForService(2)).thenReturn(15);
        when(serviceMapper.mapToServiceDto(testService1, 10)).thenReturn(testServiceDto1);
        when(serviceMapper.mapToServiceDto(testService2, 15)).thenReturn(testServiceDto2);

        List<ServiceDto> result = serviceDiscoveryService.getAllServices();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Plumbing", result.get(0).getServiceName());
        assertEquals("Cleaning", result.get(1).getServiceName());
        verify(serviceDao, times(1)).getAllServices();
        verify(serviceDao, times(1)).getTotalTasksForService(1);
        verify(serviceDao, times(1)).getTotalTasksForService(2);
    }

    @Test
    void getAllServicesShouldReturnEmptyListWhenNoServicesExist() {

        when(serviceDao.getAllServices()).thenReturn(Arrays.asList());

        List<ServiceDto> result = serviceDiscoveryService.getAllServices();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(serviceDao, times(1)).getAllServices();
        verify(serviceDao, never()).getTotalTasksForService(anyInt());
    }

    @Test
    void getAllServices_ShouldHandleZeroTasks() {

        List<Service> services = Arrays.asList(testService1);
        when(serviceDao.getAllServices()).thenReturn(services);
        when(serviceDao.getTotalTasksForService(1)).thenReturn(0);
        when(serviceMapper.mapToServiceDto(testService1, 0)).thenReturn(testServiceDto1);

        List<ServiceDto> result = serviceDiscoveryService.getAllServices();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(serviceDao, times(1)).getTotalTasksForService(1);
    }
}


