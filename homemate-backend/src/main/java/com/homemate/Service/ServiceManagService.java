package com.homemate.Service;

import java.sql.SQLException;

import org.springframework.stereotype.Service;

import com.homemate.Dao.ServiceDaoImpl;
import com.homemate.Dto.ServiceDto;
import com.homemate.Mapper.ServiceMapper;
import com.homemate.Model.ServiceEntity;
@Service
public class ServiceManagService {
    private final ServiceDaoImpl serviceDAO;

    public ServiceManagService(ServiceDaoImpl serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    public void createService(ServiceDto serviceDto) throws SQLException {
        ServiceEntity service=ServiceMapper.mapFromDto(serviceDto); 
        serviceDAO.save(service);
        }


    public void editService(long id ,ServiceDto serviceDto) throws SQLException {
        ServiceEntity service=ServiceMapper.mapFromDto(serviceDto);
        serviceDAO.update(id,service);

    }

    public void deleteService(long id) throws SQLException {
        serviceDAO.delete(id);
    }
}