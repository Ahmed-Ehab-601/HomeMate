package com.homemate.DaoTests;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.Dto.ServiceDto;
import com.homemate.Mapper.ServiceMapper;
import com.homemate.Model.ServiceEntity;
import com.homemate.Service.ServiceManagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServiceManagTest {

    @Mock
    private ServiceDaoImpl serviceDAO;

    @Mock
    private ServiceMapper serviceMapper;

    private ServiceManagService serviceManagService;

    @BeforeEach
    void setUp() {
        serviceManagService = new ServiceManagService(serviceDAO, serviceMapper);
    }

    @Test
    void testGetAllService() throws Exception {
        ServiceEntity e1 = ServiceEntity.builder()
                .id(1L).name("Cleaning").description("Clean house").build();

        ServiceEntity e2 = ServiceEntity.builder()
                .id(2L).name("Plumb").description("Plumbing").build();

        ServiceDto dto1 = ServiceDto.builder()
                .id(1L).name("Cleaning").description("Clean house").build();

        ServiceDto dto2 = ServiceDto.builder()
                .id(2L).name("Plumb").description("Plumbing").build();

        when(serviceDAO.getAll()).thenReturn(Arrays.asList(e1, e2));
        when(serviceMapper.mapToDto(e1)).thenReturn(dto1);
        when(serviceMapper.mapToDto(e2)).thenReturn(dto2);

        List<ServiceDto> result = serviceManagService.getAllService();

        assertNotNull(result);
        assertEquals("Cleaning", result.get(0).getName());
        assertEquals("Plumb", result.get(1).getName());

        verify(serviceDAO).getAll();
        verify(serviceMapper, times(2)).mapToDto(any(ServiceEntity.class));
    }

    @Test
    void testEditService() throws Exception {
        long serviceId = 1L;

        ServiceDto dto = ServiceDto.builder()
                .id(1L)
                .name("Updated Cleaning")
                .description("Updated description")
                .build();

        ServiceEntity entity = ServiceEntity.builder()
                .id(1L)
                .name("Updated Cleaning")
                .description("Updated description")
                .build();

        when(serviceMapper.mapFromDto(dto)).thenReturn(entity);

        serviceManagService.editService(serviceId, dto);

        verify(serviceMapper).mapFromDto(dto);
        verify(serviceDAO).update(serviceId, entity);
    }

    @Test
    void testDeleteService() throws Exception {
        long serviceId = 1L;
        ServiceEntity mockService = new ServiceEntity();
        mockService.setId(serviceId);
        mockService.setName("Test Service");

        when(serviceDAO.findById(serviceId)).thenReturn((mockService));
        doNothing().when(serviceDAO).delete(serviceId);

        serviceManagService.deleteService(serviceId);

        verify(serviceDAO).delete(serviceId);
    }
}
