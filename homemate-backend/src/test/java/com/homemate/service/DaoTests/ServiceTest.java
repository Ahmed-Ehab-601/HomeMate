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
public class ServiceTest {

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
        when(undertest.findIdByName("cleaning")).thenReturn(1L);
        when(undertest.isServiceInUse(1L)).thenReturn(0);
        undertest.save(serviceEntity);
        undertest.isServiceInUse(undertest.findIdByName("cleaning"));
        verify(jdbcTemplate).update(
                "INSERT INTO service(name, description, imageData, imageName, imageType) VALUES (?, ?, ?, ?, ?)",
                "cleaning",
                "great cleaning",
                null,
                "img",
                "jpeg"
        );

    }
    @Test
    public void testCreationWithImageData() throws Exception {
        String imageData = "1234";

        ServiceEntity serviceEntity = ServiceEntity.builder()
                .name("plumbing")
                .description("professional plumbing")
                .imageData(imageData)
                .imageType("png")
                .imageName("plumbing-icon")
                .build();

        undertest.save(serviceEntity);

        verify(jdbcTemplate).update(
                "INSERT INTO service(name, description, imageData, imageName, imageType) VALUES (?, ?, ?, ?, ?)",
                "plumbing",
                "professional plumbing",
                imageData,
                "plumbing-icon",
                "png"
        );
    }
}
