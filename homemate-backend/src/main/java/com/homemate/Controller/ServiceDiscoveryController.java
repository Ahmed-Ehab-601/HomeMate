package com.homemate.Controller;


import com.homemate.Dto.ServiceDto;
import com.homemate.Service.ServiceDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceDiscoveryController {

    private final ServiceDiscoveryService serviceDiscoveryService;

    @Autowired
    public ServiceDiscoveryController(ServiceDiscoveryService serviceDiscoveryService) {
        this.serviceDiscoveryService = serviceDiscoveryService;
    }
    @GetMapping
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        List<ServiceDto> services = serviceDiscoveryService.getAllServices();
        return ResponseEntity.ok(services);
    }


}
