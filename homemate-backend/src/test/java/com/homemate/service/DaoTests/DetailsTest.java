package com.homemate.service.DaoTests;

import com.homemate.service.dao.ServiceDaoImpl;
import com.homemate.service.mapRow.ServiceMapRow;
import com.homemate.service.model.ServiceEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class DetailsTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ServiceDaoImpl undertest;

    @Test
    public void testCorrectnessOfNumOFTaskers() throws Exception {
        long serviceId = 5L;

        
        doReturn(3)
                .when(jdbcTemplate)
                .queryForObject(anyString(), eq(Integer.class), eq(serviceId));

        int taskerCount = undertest.countTasker(serviceId);

        assertEquals(3, taskerCount);

        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(serviceId));
    }

    @Test
    public void testCorrectnessOfNumOFTasksDone() throws Exception {
        long serviceId = 5L;

        // Stub query for completed tasks using doReturn()
        doReturn(2)
                .when(jdbcTemplate)
                .queryForObject(anyString(), eq(Integer.class), eq(serviceId));

        int completedCount = undertest.countCompletedTasksByServiceId(serviceId);

        assertEquals(2, completedCount);

        verify(jdbcTemplate).queryForObject(anyString(), eq(Integer.class), eq(serviceId));
    }

    @Test
    public void testGetService() throws Exception {
        ServiceEntity serviceTest = ServiceEntity.builder()
                .id(5L)
                .name("CLEANING")
                .description("GREAT")
                .imageData(null)
                .imageType(null)
                .imageName(null)
                .build();

        // Stub queryForObject using any varargs
        doReturn(serviceTest)
                .when(jdbcTemplate)
                .queryForObject(anyString(), any(ServiceMapRow.class), any(Object[].class));

        // Call method under test
        ServiceEntity serviceEntity = undertest.get(5L);

        // Assertions
        assertEquals(serviceTest.getName(), serviceEntity.getName());
        assertEquals(serviceTest.getDescription(), serviceEntity.getDescription());

        // Verify call
        verify(jdbcTemplate).queryForObject(anyString(), any(ServiceMapRow.class), any(Object[].class));
    }


}
