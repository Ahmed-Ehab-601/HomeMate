
package com.homemate.DaoTests;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.MapRow.ServiceMapRow;
import com.homemate.Model.ServiceEntity;

import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FindServicesTest {
    @Mock
    private JdbcTemplate jdbcTemplate;


    @InjectMocks
    private ServiceDaoImpl undertest;

    @Test
    public void testCorrectnessOfFindServices() throws Exception {
        List<ServiceEntity> list = new ArrayList<>();
        ServiceEntity serviceTest = ServiceEntity.builder()
                .id(5L)
                .name("CLEANING")
                .description("GREAT")
                .imageData(null)
                .imageType(null)
                .imageName(null)
                .build();
        list.add(serviceTest);
        serviceTest = ServiceEntity.builder()
                .id(5L)
                .name("CLEAN")
                .description("GREAT")
                .imageData(null)
                .imageType(null)
                .imageName(null)
                .build();
        list.add(serviceTest);

        doReturn(list)
                .when(jdbcTemplate)
                .query(anyString(), any(ServiceMapRow.class));
        List<ServiceEntity> res = undertest.getAll();
        assertEquals(res, list);
        verify(jdbcTemplate).query(anyString(), any(ServiceMapRow.class));
    }
    @Test
    public void testGetAllReturnsEmptyList() throws Exception{
        when(jdbcTemplate.query(anyString(), any(ServiceMapRow.class)))
                .thenReturn(Collections.emptyList());

        List<ServiceEntity> result = undertest.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    @Test
    void testGetAllThrowsException() {

        when(jdbcTemplate.query(anyString(), any(ServiceMapRow.class)))
                .thenThrow(new RuntimeException("DB error"));

        SQLException ex = assertThrows(
                SQLException.class,
                () -> undertest.getAll()
        );

        assertEquals("Failed to fetch services", ex.getMessage());
    }

}