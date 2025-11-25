package com.homemate.taskerdiscovery.service;

import com.homemate.taskerdiscovery.mapper.ServiceMapper;
import com.homemate.taskerdiscovery.dto.ServiceDto;
import com.homemate.taskerdiscovery.dao.ServiceDao;
import com.homemate.taskerdiscovery.model.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class ServiceDiscoveryService {

    private final ServiceDao serviceDao;
    private final ServiceMapper serviceMapper;

    @Autowired
    public ServiceDiscoveryService(ServiceDao serviceDao, ServiceMapper serviceMapper) {
        this.serviceDao = serviceDao;
        this.serviceMapper = serviceMapper;
    }

    public List<ServiceDto> getAllServices() {
        List<Service> services = serviceDao.getAllServices();
        List<ServiceDto> serviceDtos = new ArrayList<>();

        for (Service service : services) {
            int totalTasks = serviceDao.getTotalTasksForService(service.getServiceId());
            System.out.println(totalTasks);
            ServiceDto dto = serviceMapper.mapToServiceDto(service, totalTasks);
            serviceDtos.add(dto);
        }

        return serviceDtos;
    }
}
