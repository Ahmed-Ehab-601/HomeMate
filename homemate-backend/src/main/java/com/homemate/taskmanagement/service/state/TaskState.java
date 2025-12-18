package com.homemate.taskmanagement.service.state;

public interface TaskState {
    void updateWorkedHours();
    void updateStatus();
    void changeContext(TaskContext newContext);

}
