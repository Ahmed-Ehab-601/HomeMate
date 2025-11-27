package com.homemate.controller;

import com.homemate.dto.ServiceDto;
import com.homemate.service.ServiceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // IMPORTANT: Allow frontend CORS
public class ServiceManagementController {

    @Autowired
    private ServiceManagementService serviceManagService;

    // Get all services - Frontend calls: GET http://localhost:8080/getallservices
    @GetMapping("/getallservices")
    public List<ServiceDto> getAllService() throws SQLException {
        return serviceManagService.getAllService();
    }

    // Get service details - Frontend calls: GET http://localhost:8080/getservicedetails/{serviceID}
    @GetMapping("/getservicedetails/{serviceID}")
    public ServiceDetailsDto getdetails(@PathVariable long serviceID) throws SQLException {
        return serviceManagService.getdetails(serviceID);
    }

    // Create service - Frontend calls: POST http://localhost:8080/create
    @PostMapping("/create")
    public String createService(@RequestBody ServiceDto serviceDto) throws SQLException {
        serviceManagService.createService(serviceDto);
        return "Service created successfully!";
    }

    // Edit service - Frontend calls: POST http://localhost:8080/edit/{id}
    @PostMapping("/edit/{id}")
    public String editService(@PathVariable long id, @RequestBody ServiceDto serviceDto) throws SQLException {
        serviceManagService.editService(id, serviceDto);
        return "Service edited successfully!";
    }

    // Delete service - Frontend calls: DELETE http://localhost:8080/delete/{id}
    @DeleteMapping("/delete/{id}")
    public String deleteService(@PathVariable long id) throws SQLException {
        serviceManagService.deleteService(id);
        return "Service deleted successfully!";
    }
}

