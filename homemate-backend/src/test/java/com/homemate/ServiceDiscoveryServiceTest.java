package com.homemate;
import com.homemate.dao.ServiceDao;
import com.homemate.mapper.ServiceMapper;
import com.homemate.dto.ServiceDto;
import com.homemate.model.Service;
import com.homemate.service.ServiceDiscoveryService;
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
                new byte[]{1, 2, 3}, "plumbing.jpg", "image/jpeg");
        testService2 = new Service(2, "Cleaning", "Cleaning services",
                new byte[]{4, 5, 6}, "cleaning.jpg", "image/jpeg");

        testServiceDto1 = new ServiceDto();
        testServiceDto1.setServiceid(1);
        testServiceDto1.setServicename("Plumbing");
        testServiceDto1.setTotalTasks(10);

        testServiceDto2 = new ServiceDto();
        testServiceDto2.setServiceid(2);
        testServiceDto2.setServicename("Cleaning");
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
        assertEquals("Plumbing", result.get(0).getServicename());
        assertEquals("Cleaning", result.get(1).getServicename());
        verify(serviceDao, times(1)).getAllServices();
        verify(serviceDao, times(1)).getTotalTasksForService(1);
        verify(serviceDao, times(1)).getTotalTasksForService(2);
    }

    @Test
    void getAllServices_ShouldReturnEmptyList_WhenNoServicesExist() {

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


