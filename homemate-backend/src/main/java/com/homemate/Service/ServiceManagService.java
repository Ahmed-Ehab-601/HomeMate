package com.homemate.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.homemate.Dto.ServiceDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.Dto.ServiceDto;
import com.homemate.Mapper.ServiceMapper;
import com.homemate.Model.ServiceEntity;
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


    public void editService(long id ,ServiceDto serviceDto) throws SQLException {
        ServiceEntity service=serviceMapper.mapFromDto(serviceDto);
        serviceDAO.update(id,service);

    }

    public void deleteService(long id) throws SQLException {
        serviceDAO.delete(id);
    }

    public List<ServiceDto> getAllService() throws SQLException{
        List<ServiceEntity> listOfEntities=serviceDAO.getAll();
        List<ServiceDto> listOfDtos=new ArrayList<>(listOfEntities.size());
        for (ServiceEntity entity : listOfEntities) {
            listOfDtos.add(serviceMapper.mapToDto(entity));
        }

        return listOfDtos;
    }

    public ServiceDetailsDto getdetails(long serviceID) throws SQLException{
         ServiceEntity service= serviceDAO.get(serviceID);
         int count = serviceDAO.countCompletedTasksByServiceId(serviceID);
         int taskers=serviceDAO.countTasker(serviceID);
        return ServiceDetailsDto.builder().service(service).taskers(taskers).completedTasks(count)
                .build();
    }
}