package com.homemate.Service;

import com.homemate.Dto.ObjectMapper;
import com.homemate.Dto.ServiceDto;
import com.homemate.Dao.ServiceDao;
import com.homemate.Model.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceDiscoveryService {

    private final ServiceDao serviceDao;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServiceDiscoveryService(ServiceDao serviceDao, ObjectMapper objectMapper) {
        this.serviceDao = serviceDao;
        this.objectMapper = objectMapper;
    }

    public List<ServiceDto> getAllServices() {
        List<Service> services = serviceDao.getAllServices();
        List<ServiceDto> serviceDtos = new ArrayList<>();

        for (Service service : services) {
            int totalTasks = serviceDao.getTotalTasksForService(service.getServiceid());
            ServiceDto dto = objectMapper.mapToServiceDto(service, totalTasks);
            serviceDtos.add(dto);
        }

        return serviceDtos;
    }
}
