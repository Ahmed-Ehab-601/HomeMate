package com.homemate.taskmanagement.service.state;

import com.homemate.taskmanagement.exceptions.BadStateUpdateException;


public class Accepted implements TaskState{
    @Override
    public void updateWorkedHours() {
    }

    @Override
    public void updateStatus() {
    }

    @Override
    public void changeContext(TaskContext newContext) {
        if(! (newContext.state instanceof InProgress )){
            throw new BadStateUpdateException("accepted can be changed to in progress only");
        }

    }
}
