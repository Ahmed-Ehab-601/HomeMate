package com.homemate.service.DaoTests;

import com.homemate.service.dao.ServiceDaoImpl;
import com.homemate.service.model.ServiceEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
    public class UpdateTest {


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
            when(jdbcTemplate.update(
                    anyString(),
                    anyString(),
                    anyString(),
                    any(),
                    any(),
                    any(),
                    eq(id)
            )).thenReturn(1);

        undertest.update(id, serviceTest);
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


