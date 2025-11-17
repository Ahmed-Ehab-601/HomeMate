package com.homemate.Service;

import com.homemate.Dto.ObjectMapper;
import com.homemate.Dao.ServiceDao;
import com.homemate.Dto.ServiceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ServiceDiscoveryService {
    private final ServiceDao serviceDao;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServiceDiscoveryService(ServiceDao serviceDao, ObjectMapper objectMapper) {
        this.serviceDao = serviceDao;
        this.objectMapper = objectMapper;
    }

    public List<ServiceDto> getAllServices() {
        List<com.homemate.Model.Service> services = serviceDao.getAllServices();

        List<ServiceDto> serviceDtos = new ArrayList<>();
        for ( com.homemate.Model.Service service : services) {
            ServiceDto dto = objectMapper.mapToServiceDto(service);
            serviceDtos.add(dto);
        }

        return serviceDtos;
    }

}
