package com.homemate.controller;

import com.homemate.dto.ServiceDto;
import com.homemate.service.ServiceManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/services") // Optional: if you want a base path
@CrossOrigin(origins = "http://localhost:5173") // Allow frontend to access
public class ServiceManagementController {

    @Autowired
    private ServiceManagementService serviceManagService;

    // Get all services
    @GetMapping("/getallservices")
    public ResponseEntity<List<ServiceDto>> getAllService() {
        try {
            List<ServiceDto> services = serviceManagService.getAllService();
            return ResponseEntity.ok(services);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get service details by ID
    @GetMapping("/getservicedetails/{serviceID}")
    public ResponseEntity<ServiceDetailsDto> getdetails(@PathVariable long serviceID) {
        try {
            ServiceDetailsDto details = serviceManagService.getdetails(serviceID);
            if (details != null) {
                return ResponseEntity.ok(details);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Create a new service
    @PostMapping("/create")
    public ResponseEntity<String> createService(@RequestBody ServiceDto serviceDto) {
        try {
            serviceManagService.createService(serviceDto);
            return ResponseEntity.ok("Service created successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create service: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create service: " + e.getMessage());
        }
    }

    // Edit/Update a service
    @PostMapping("/edit/{id}")
    public ResponseEntity<String> editService(@PathVariable long id, @RequestBody ServiceDto serviceDto) {
        try {
            serviceManagService.editService(id, serviceDto);
            return ResponseEntity.ok("Service edited successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to edit service: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to edit service: " + e.getMessage());
        }
    }

    // Delete a service
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteService(@PathVariable long id) {
        try {
            serviceManagService.deleteService(id);
            return ResponseEntity.ok("Service deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete service: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete service: " + e.getMessage());
        }
    }
}

