package com.homemate.Controller;

import java.sql.SQLException;
import java.util.List;

import com.homemate.Dto.ServiceDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.homemate.Dto.ServiceDto;
import com.homemate.Service.ServiceManagService;
@RestController
@RequestMapping("/service")
@CrossOrigin(origins = "http://localhost:5175")
public class ServiceController {

        private final ServiceManagService serviceManagService;
        private ServiceController(ServiceManagService serviceManagService) {
           this.serviceManagService = serviceManagService;
        }
        @PostMapping("/create")
        public String createService(@RequestBody ServiceDto serviceDto) throws SQLException {
            serviceManagService.createService(serviceDto);
            return "Service created successfully!";
        }
        @PostMapping("/edit/{id}")
        public String editService(@PathVariable long id,@RequestBody ServiceDto serviceDto) throws SQLException {
            serviceManagService.editService(id,serviceDto);
            return "Service edited successfully!";
        }
        @DeleteMapping("/delete/{id}")
        public String editService(@PathVariable long id) throws SQLException {
            serviceManagService.deleteService(id);
            return "Service deleted successfully!";
        }
        @GetMapping("/getallservices")
        public List<ServiceDto> getAllService() throws SQLException {
            return serviceManagService.getAllService();
        }
       @GetMapping("/getservicedetails/{serviceID}")
       public ResponseEntity<ServiceDetailsDto> getDetails(@PathVariable long serviceID) {
           try {
               ServiceDetailsDto details = serviceManagService.getdetails(serviceID);
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