package com.homemate.service.controller;

import java.sql.SQLException;
import java.util.List;

import com.homemate.service.dto.ServiceDetailsDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.homemate.service.dto.ServiceDto;
import com.homemate.service.service.ServiceManagService;
import javax.management.ServiceNotFoundException;

@RestController
@RequestMapping("/api/service")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceManagService serviceManagService;

    private ServiceController(ServiceManagService serviceManagService) {
        this.serviceManagService = serviceManagService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createService(@Valid @RequestBody ServiceDto serviceDto) throws SQLException {
        try {
            if (serviceDto.getId() != null && serviceDto.getId() != 0) {
                return ResponseEntity.badRequest().body("ID should not be provided when creating a service");
            }

            serviceManagService.createService(serviceDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Service created successfully!");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create service: " + e.getMessage());
        }
    }

    @PostMapping("/edit/{id}")
    public ResponseEntity<String> editService(@PathVariable long id, @Valid @RequestBody ServiceDto serviceDto) throws Exception {
        try {
            if (id <= 0) {
                throw new ServiceNotFoundException("Service with ID " + id + " not found");
            }
            serviceManagService.editService(id, serviceDto);
            return ResponseEntity.ok("Service edited successfully!");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to edit service: " + e.getMessage());
        } catch (ServiceNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteService(@PathVariable long id) throws Exception {
        try {
            if (id <= 0) {
                return ResponseEntity.badRequest().body("Invalid ID");
            }

            serviceManagService.deleteService(id);
            return ResponseEntity.ok("Service deleted successfully!");
        } catch (ServiceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete service: " + e.getMessage());
        }
    }

    @GetMapping("/getallservices")
    public ResponseEntity<List<ServiceDto>> getAllService() throws Exception {
        try {
            List<ServiceDto> serviceDtoList = serviceManagService.getAllService();
            if (serviceDtoList != null && !serviceDtoList.isEmpty()) {
                return ResponseEntity.ok(serviceDtoList);
            } else {
                return ResponseEntity.ok(List.of());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getservicedetails/{serviceID}")
    public ResponseEntity<ServiceDetailsDto> getDetails(@PathVariable long serviceID) throws Exception{
        try {
            if (serviceID <= 0) {
                return ResponseEntity.badRequest().build();
            }

            ServiceDetailsDto details = serviceManagService.getDetails(serviceID);
            if (details != null) {
                return ResponseEntity.ok(details);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}