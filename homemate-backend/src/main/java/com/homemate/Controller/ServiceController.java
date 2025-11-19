package com.homemate.Controller;

import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.homemate.Dto.ServiceDto;
import com.homemate.Service.ServiceManagService;
@RestController
@RequestMapping("/service")
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
        public List<ServiceDto> editService() throws SQLException {
            return serviceManagService.getAllService();
        }

}