package com.homemate.integration;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.Model.ServiceEntity;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest
@ActiveProfiles("service")  // uses application-test.properties
@Transactional  // Rolls back after each test
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FindServicePart {

    @Autowired
    private ServiceDaoImpl serviceDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Order(1)
    public void testCreateServiceAndVerifyInDatabase() throws Exception {
        ServiceEntity service = ServiceEntity.builder()
                .name("PLUMB")
                .description("Professional plumbing services")
                .imageData(null)
                .imageType("png")
                .imageName("plumbing-icon")
                .build();

        // Save service
        serviceDao.save(service);


        ServiceEntity res = serviceDao.get(serviceDao.findIdByName(service.getName()));

        // Verify fields
        assertEquals("PLUMB", res.getName());
        assertEquals("Professional plumbing services", res.getDescription());
        assertEquals("png", res.getImageType());
        assertEquals("plumbing-icon", res.getImageName());
    }
}
