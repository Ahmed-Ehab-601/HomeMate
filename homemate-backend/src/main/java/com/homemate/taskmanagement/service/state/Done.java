package com.homemate.taskmanagement.service.state;

import com.homemate.TaskerProfile.Dao.TaskerDao;
import com.homemate.TaskerProfile.models.Tasker;
import com.homemate.payment.dao.PaymentDao;
import com.homemate.payment.model.Payment;
import com.homemate.payment.service.StripeCustomerService;
import com.homemate.payment.service.StripePaymentService;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.exceptions.BadStateUpdateException;
import com.homemate.taskmanagement.model.Status;
import com.stripe.model.PaymentIntent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
public class Done implements TaskState{
    private Long taskID;
    private Long taskerID;
    private TaskStatusDao taskDao;

    public Done (long taskID, long taskerID, TaskStatusDao taskDao){
           this.taskID=taskID;
           this.taskerID=taskerID;
           this.taskDao=taskDao;
    }


    @Override
    public void updateWorkedHours() {
        Timestamp startInProgress = taskDao.getStartInProgress(taskID);
        if(startInProgress != null){
            Duration duration = Duration.between(startInProgress.toInstant(), Instant.now());
            double workedHours = duration.toMinutes() / 60.0 ;
            taskDao.updateTaskWorkedHours(taskID,workedHours);
            taskDao.updateTaskStartInProgress(taskID,null);
        }

        Double hourRate = taskDao.getTaskerHourRate(taskerID);
        Double workedHours = taskDao.getTaskWorkedHours(taskID);
        taskDao.updateTaskerWorkedHours(taskerID,workedHours);

        double bill = hourRate * workedHours;
        taskDao.updateTaskBill(taskID,bill);
        taskDao.updateTaskEndData(taskID,Timestamp.valueOf(LocalDateTime.now()));


    }

    @Override
    public void updateStatus() {
        taskDao.updateStatus(taskID,Status.Done);
    }

    @Override
    public void changeContext(TaskContext newContext) {
        throw new BadStateUpdateException("can't change done status");

    }
}
