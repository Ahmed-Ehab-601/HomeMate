package com.homemate.Dao;

import com.homemate.Model.Service;
import java.util.List;

public interface ServiceDao {
    List<Service> getAllServices();                  // fetch base service data
    int getTotalTasksForService(int serviceId);      // fetch task count for a single service
}
