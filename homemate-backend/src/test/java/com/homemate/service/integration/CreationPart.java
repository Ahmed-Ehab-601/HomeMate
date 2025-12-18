package com.homemate.service.integration;

import com.homemate.service.dao.ServiceDaoImpl;
import com.homemate.service.model.ServiceEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@ActiveProfiles("service")  // ← Uses application-admin.properties
@Transactional  // ← Rolls back after each test
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreationPart {
    @Autowired
    private ServiceDaoImpl serviceDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    @Order(1)
    public void testCreateServiceAndVerifyInDatabase() throws Exception{
        ServiceEntity service = ServiceEntity.builder()
                .name("PLUMB")
                .description("Professional plumbing services")
                .imageData(null)
                .imageType("png")
                .imageName("plumbing-icon")
                .build();

        serviceDao.save(service);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT * FROM service WHERE name = ?", "PLUMB"
        );

        assertEquals(1, results.size());
        Map<String, Object> row = results.getFirst();
        assertEquals("PLUMB", row.get("name"));
        assertEquals("Professional plumbing services", row.get("description"));
        assertEquals("png", row.get("imageType"));
        assertEquals("plumbing-icon", row.get("imageName"));

    }
    @Test
    @Order(2)
    public void testCreateServiceWithImageAndVerifyInDatabase() throws Exception{
        byte[] imageData = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};

        ServiceEntity service = ServiceEntity.builder()
                .name("Electrical")
                .description("Professional plumbing services")
                .imageData(imageData)
                .imageType("png")
                .imageName("plumbing-icon")
                .build();

        serviceDao.save(service);
        byte[] savedImageData = jdbcTemplate.queryForObject(
                "SELECT imageData FROM service WHERE name = ?",
                byte[].class,
                "Electrical"
        );
        assertNotNull(savedImageData);
        assertArrayEquals(imageData, savedImageData);

        jdbcTemplate.execute("DELETE FROM service WHERE name = 'Electrical'");

    }
}
