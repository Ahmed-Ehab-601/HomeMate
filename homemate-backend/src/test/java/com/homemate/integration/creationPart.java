package com.homemate.integration;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.Model.ServiceEntity;
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
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

@SpringBootTest
@ActiveProfiles("service")  // ← Uses application-test.properties
@Transactional  // ← Rolls back after each test
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class creationPart {
    @Autowired
    private ServiceDaoImpl serviceDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        // Delete in correct order: children first, then parents
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0"); // Temporarily disable
            jdbcTemplate.execute("DELETE FROM task");            // Delete tasks first
            jdbcTemplate.execute("DELETE FROM service");         // Then delete services
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1"); // Re-enable
        } catch (Exception e) {
            // Log but don't fail if tables are already empty
            System.out.println("Cleanup warning: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            jdbcTemplate.execute("DELETE FROM task");
            jdbcTemplate.execute("DELETE FROM service");
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (Exception e) {
            System.out.println("Teardown warning: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    public void testCreateServiceAndVerifyInDatabase() throws Exception{
        ServiceEntity service = ServiceEntity.builder()
                .name("PLUMBING")
                .description("Professional plumbing services")
                .imageData(null)
                .imageType("png")
                .imageName("plumbing-icon")
                .build();

        serviceDao.save(service);
        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                "SELECT * FROM service WHERE name = ?", "PLUMBING"
        );

        assertEquals(1, results.size());
        Map<String, Object> row = results.getFirst();
        assertEquals("PLUMBING", row.get("name"));
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
