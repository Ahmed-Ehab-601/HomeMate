package com.homemate.Controller;

import java.sql.SQLException;
import java.util.List;

import com.homemate.Dto.ServiceDetailsDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.homemate.Dto.ServiceDto;
import com.homemate.Service.ServiceManagService;
@RestController
@RequestMapping("/service")
@CrossOrigin(origins = "http://localhost:5174")
public class ServiceController {

        private final ServiceManagService serviceManagService;
        private ServiceController(ServiceManagService serviceManagService) {
           this.serviceManagService = serviceManagService;
        }
        @PostMapping("/create")
        public ResponseEntity<String> createService(@RequestBody ServiceDto serviceDto) throws SQLException {
          try{  serviceManagService.createService(serviceDto);
              return ResponseEntity.status(HttpStatus.CREATED)
                      .body("Service created successfully!");
        } catch (SQLException e) {
              return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .body("Failed to create service");
          }}
              @PostMapping("/edit/{id}")
        public ResponseEntity<String> editService(@PathVariable long id, @RequestBody ServiceDto serviceDto) throws SQLException {
        try{    serviceManagService.editService(id,serviceDto);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Service edited successfully!");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to edit service");
        }}
            @DeleteMapping("/delete/{id}")
            public ResponseEntity<String> deleteService(@PathVariable long id) {
                try {
                    serviceManagService.deleteService(id);
                    return ResponseEntity.ok("Service deleted successfully!");
                } catch (SQLException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to delete service: " + e.getMessage());
                }
            }
        @GetMapping("/getallservices")
        public ResponseEntity<List<ServiceDto>> getAllService() throws SQLException {
           try { List<ServiceDto>serviceDtoList= serviceManagService.getAllService();
            if (serviceDtoList != null) {
                return ResponseEntity.ok(serviceDtoList);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
        }
       @GetMapping("/getservicedetails/{serviceID}")
       public ResponseEntity<ServiceDetailsDto> getDetails(@PathVariable long serviceID) {
           try {
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