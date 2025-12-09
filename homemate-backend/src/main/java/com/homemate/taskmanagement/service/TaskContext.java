package com.homemate.taskmanagement.service;

import com.homemate.taskmanagement.exceptions.BadStateUpdateException;

public class TaskContext {
    TaskState state;

    public TaskContext(TaskState initialState){
        state = initialState;
    }
    public void contextChange(TaskState newState){
        TaskState oldState = state;
        state = newState;
        oldState.changeContext(this);
    }

    public void sendEmail() {
        this.state.sendEmail();

    }

    public void updateWorkedHours() {
        this.state.updateWorkedHours();
    }

    public void updateStatus() {
        this.state.updateStatus();
    }



}
