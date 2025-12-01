package com.homemate.taskerdiscovery.dao;

import com.homemate.taskerdiscovery.model.Service;
import java.util.List;

public interface ServiceDao {
    List<Service> getAllServices();
    int getTotalTasksForService(int serviceId);
}
