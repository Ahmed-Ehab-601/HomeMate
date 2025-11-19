package com.homemate.DaoTests;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.Model.ServiceEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class serviceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private ServiceDaoImpl undertest;

    @Test
    public void testCreation() throws Exception {

        ServiceEntity serviceEntity = ServiceEntity.builder()
                .name("cleaning")
                .description("great cleaning")
                .imageData(null)
                .imageType("jpeg")
                .imageName("img")
                .build();

        undertest.save(serviceEntity);

        verify(jdbcTemplate).update(
                "INSERT INTO service(name, description, imageData, imageName, imageType) VALUES (?, ?, ?, ?, ?)",
                "cleaning",
                "great cleaning",
                null,
                "img",
                "jpeg"
        );

    }
}
