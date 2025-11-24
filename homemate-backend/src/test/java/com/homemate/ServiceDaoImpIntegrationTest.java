package com.homemate;

import com.homemate.dao.ServiceDao;
import com.homemate.model.Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles("tasker")
public class ServiceDaoImpIntegrationTest {

    @Autowired
    private ServiceDao serviceDao;

    @Test
    void getAllServices() {
        List<Service> services = serviceDao.getAllServices();
        assertThat(services).isNotNull();
        assertThat(services).hasSize(10);
        assertThat(services).extracting(Service::getServiceName).contains("Plumbing");
    }

    @Test
    void getTotalTasksForService_countsCorrectly() {
        int service1 = serviceDao.getTotalTasksForService(1);
        int service50 = serviceDao.getTotalTasksForService(50);
        assertThat(service1).isEqualTo(2);
        assertThat(service50).isEqualTo(0);

    }
}
