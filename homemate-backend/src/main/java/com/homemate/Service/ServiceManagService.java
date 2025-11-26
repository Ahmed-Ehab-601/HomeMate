package com.homemate.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.homemate.Dto.ServiceDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.Dto.ServiceDto;
import com.homemate.Mapper.ServiceMapper;
import com.homemate.Model.ServiceEntity;

import javax.management.ServiceNotFoundException;

import static org.springframework.web.servlet.function.ServerResponse.badRequest;

@Service
public class ServiceManagService {
    @Autowired
    private final ServiceDaoImpl serviceDAO;
    private final ServiceMapper serviceMapper;

    public ServiceManagService(ServiceDaoImpl serviceDAO, ServiceMapper serviceMapper) {
        this.serviceDAO = serviceDAO;
        this.serviceMapper = serviceMapper;
    }

    public void createService(ServiceDto serviceDto) throws SQLException {
        ServiceEntity service=serviceMapper.mapFromDto(serviceDto);
        serviceDAO.save(service);
        }


    public void editService(long id ,ServiceDto serviceDto) throws Exception {

        ServiceEntity service=serviceMapper.mapFromDto(serviceDto);
        serviceDAO.update(id,service);

    }

    public void deleteService(long id) throws Exception {
        if (serviceDAO.findById(id) == null) {
            throw new ServiceNotFoundException("Service with ID " + id + " not found");
        }

        // Check if service is in use (has active tasks or taskers)
        if (serviceDAO.countTasker(id)>0||serviceDAO.isServiceInUse(id)>0) {
            throw new Exception(
                    "Cannot delete this service.Taskers currently using it."
            );}
        if(serviceDAO.isServiceInUse(id)>0){throw new Exception(
                "Cannot delete this service. It has active tasks"
        );}
        serviceDAO.delete(id);
    }

    public List<ServiceDto> getAllService() throws Exception{
        List<ServiceEntity> listOfEntities=serviceDAO.getAll();
        List<ServiceDto> listOfDtos=new ArrayList<>(listOfEntities.size());
        for (ServiceEntity entity : listOfEntities) {
            listOfDtos.add(serviceMapper.mapToDto(entity));
        }

        return listOfDtos;
    }

    public ServiceDetailsDto getDetails(long serviceID) throws Exception {
         ServiceEntity service= serviceDAO.get(serviceID);
         ServiceDto serviceDto=serviceMapper.mapToDto(service);
         int count = serviceDAO.countCompletedTasksByServiceId(serviceID);
         int taskers=serviceDAO.countTasker(serviceID);
        return ServiceDetailsDto.builder().service(serviceDto).taskers(taskers).completedTasks(count)
                .build();
    }
}