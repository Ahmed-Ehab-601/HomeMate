package com.homemate.taskmanagement.service;

public interface TaskState {
    void sendEmail();
    void updateWorkedHours();
    void updateStatus();
    void changeContext(TaskContext newContext);

}
