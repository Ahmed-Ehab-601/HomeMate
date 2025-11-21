package com.homemate.DaoTests;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.MapRow.ServiceMapRow;
import com.homemate.Model.ServiceEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
    public class updateTest {


        @Mock
        private JdbcTemplate jdbcTemplate;

        @InjectMocks
        private ServiceDaoImpl undertest;
        @Test
    public void testCorrectUpdate() throws Exception {
        long id = 5L;
        ServiceEntity serviceTest = ServiceEntity.builder()
                .id(id)
                .name("clean")
                .description("good")
                .imageData(null)
                .imageType(null)
                .imageName(null)
                .build();

        undertest.update(id, serviceTest);
        assertEquals(id,serviceTest.getId());
        verify(jdbcTemplate).update(
                anyString(),
                eq(serviceTest.getName()),
                eq(serviceTest.getDescription()),
                eq(serviceTest.getImageData()),
                eq(serviceTest.getImageName()),
                eq(serviceTest.getImageType()),
                eq(id)
        );
    }
}


