package com.homemate.service.DaoTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homemate.service.controller.ServiceController;
import com.homemate.service.dto.ServiceDetailsDto;
import com.homemate.service.dto.ServiceDto;
import com.homemate.service.service.ServiceManagService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.management.ServiceNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@AutoConfigureMockMvc(addFilters = false) // disables Spring Security
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ServiceManagService serviceManagService;


    @Autowired
    private ObjectMapper objectMapper;

    // ---------------------------
    // CREATE SERVICE
    // ---------------------------
    @Test
    void testCreateService_Success() throws Exception {
        ServiceDto Dto = ServiceDto.builder()
                .name("Clean")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();
        mockMvc.perform(post("/service/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Dto)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Service created successfully!"));
    }

    @Test
    void testCreateService_Failure() throws Exception {
        ServiceDto Dto = ServiceDto.builder()
                .name("Cleaning")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();
        doThrow(new SQLException("Service not found"))
                .when(serviceManagService)
                .editService(eq(5L), any(ServiceDto.class));

        mockMvc.perform(post("/service/edit/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to edit service: Service not found"));

        verify(serviceManagService).editService(eq(5L), any(ServiceDto.class));
    }
    @Test
    void testEditService_Success() throws Exception {
        ServiceDto dto = ServiceDto.builder()
                .id(5L)
                .name("Cleaning")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();

        mockMvc.perform(post("/service/edit/5")
                        .contentType(MediaType.APPLICATION_JSON)  // Add this
                        .content(objectMapper.writeValueAsString(dto)))  // Add this
                .andExpect(status().isOk())  // Changed from isCreated() to isOk()
                .andExpect(content().string("Service edited successfully!"));

        verify(serviceManagService).editService(eq(5L), any(ServiceDto.class));
    }
    @Test
    void testEditService_Failure() throws Exception {
        ServiceDto dto = ServiceDto.builder()
                .id(5L)
                .name("Cleaning")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();

        doThrow(new SQLException("Service not found"))
                .when(serviceManagService)
                .editService(eq(5L), any(ServiceDto.class));

        mockMvc.perform(post("/service/edit/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to edit service: Service not found"));

        verify(serviceManagService).editService(eq(5L), any(ServiceDto.class));

    }

    @Test
    void testDeleteService_Success() throws Exception {
        mockMvc.perform(delete("/service/delete/5"))
                .andExpect(status().isOk())
                .andExpect(content().string("Service deleted successfully!"));
    }

    @Test
    void testDeleteService_Failure() throws Exception {
        doThrow(new SQLException("Error"))
                .when(serviceManagService)
                .deleteService(5L);

        mockMvc.perform(delete("/service/delete/5"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to delete service: Error"));
    }
    @Test
    void testDeleteService_NotFound() throws Exception {
        doThrow(new ServiceNotFoundException("Service with ID 5 not found"))
                .when(serviceManagService)
                .deleteService(5L);

        mockMvc.perform(delete("/service/delete/5"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Service with ID 5 not found"));

        verify(serviceManagService).deleteService(5L);
    }
    @Test
    void testGetAllServices_Success() throws Exception {
        ServiceDto Dto = ServiceDto.builder()
                .id(5L)
                .name("Cleaning")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();
        ServiceDto Dto2 = ServiceDto.builder()
                .id(5L)
                .name("Clean")
                .description("Best cleaning service")
                .imageData(null)
                .imageName("img1")
                .imageType("png")
                .build();
        List<ServiceDto> list = new ArrayList<>();
        list.add(Dto2);
        list.add(Dto);

        when(serviceManagService.getAllService()).thenReturn(list);

        mockMvc.perform(get("/service/getallservices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Clean"))
                .andExpect(jsonPath("$[1].name").value("Cleaning"));
    }

    @Test
    void testGetAllServices_Failure() throws Exception {
        when(serviceManagService.getAllService()).thenThrow(new SQLException());

        mockMvc.perform(get("/service/getallservices"))
                .andExpect(status().isInternalServerError());
    }
   @Test
    void testGetDetails_Success() throws Exception {
        ServiceDto Dto = ServiceDto.builder()
                .id(5L)
                .name("Cleaning")
                .description("Best cleaning service")
                .build();
        ServiceDetailsDto serviceDetailsDto=ServiceDetailsDto.builder().service(Dto).taskers(0).completedTasks(0)
        .build();
        when(serviceManagService.getDetails(5L)).thenReturn(serviceDetailsDto);
        mockMvc.perform(get("/service/getservicedetails/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service.id").value(5))
                .andExpect(jsonPath("$.service.name").value("Cleaning"))
                .andExpect(jsonPath("$.service.description").value("Best cleaning service"))
                .andExpect(jsonPath("$.taskers").value(0))
                .andExpect(jsonPath("$.completedTasks").value(0));

    }

    @Test
    void testGetDetails_NotFound() throws Exception {
        when(serviceManagService.getDetails(1L)).thenReturn(null);

        mockMvc.perform(get("/service/getservicedetails/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDetails_Error() throws Exception {
        when(serviceManagService.getDetails(1L)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/service/getservicedetails/1"))
                .andExpect(status().isInternalServerError());
    }
}
