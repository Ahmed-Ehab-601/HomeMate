package com.homemate.dao;

import com.homemate.model.Service;
import java.util.List;

public interface ServiceDao {
    List<Service> getAllServices();
    int getTotalTasksForService(int serviceId);
}
