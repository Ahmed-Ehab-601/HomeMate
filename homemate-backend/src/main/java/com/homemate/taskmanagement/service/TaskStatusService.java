package com.homemate.taskmanagement.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.service.EmailService;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskRequestResponseDto;
import com.homemate.taskmanagement.exceptions.BadAcceptRejectException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.service.state.TaskContext;
import com.homemate.taskmanagement.service.state.TaskState;
import com.homemate.taskmanagement.service.stateFactory.StatusFactory;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@AllArgsConstructor
@Service
public class TaskStatusService {

    private final StatusFactory statusFactory;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final EmailService emailService;
    private final TaskStatusDao taskStatusDao;
    private final TaskRequestDao taskRequestDao;
    private final String WEBSOCKET = "/send/task/";


    public TaskRequestResponseDto acceptOrReject(Long taskID, Long taskerID, Status newStatus) {

        Status status = validateTaskExistAndTaskerOwner(taskID, taskerID);
        if (!status.equals(Status.InReview)) {
            throw new BadAcceptRejectException("bad request the task must be inReview");
        }
        taskStatusDao.updateStatus(taskID, newStatus);
        Optional<TaskDto> taskDtoOptional = taskRequestDao.getTaskDetails(taskID);
        assert taskDtoOptional.isPresent();
        TaskDto taskDto = taskDtoOptional.get();


        sendStatusEmail(taskDto);
        simpMessagingTemplate.convertAndSend(WEBSOCKET + taskID, taskDto);

        return new TaskRequestResponseDto(taskID, newStatus);


    }

    public void sendStatusEmail(TaskDto taskDto) {
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(taskDto.getUserMail())
                .task(taskDto)
                .emailType(EmailType.TASK_STATUS)
                .build();

        emailService.sendEmail(emailRequest);
    }

    public Status validateTaskExistAndTaskerOwner(Long taskID, Long taskerID) {
        Optional<Status> status = taskStatusDao.getStatus(taskID);
        if (status.isEmpty()) {
            throw new TaskNotFoundException("wrong task id");
        }
        Optional<Long> id = taskStatusDao.getTaskerID(taskID);
        if (id.isEmpty() || !id.get().equals(taskerID)) {
            throw new BadAcceptRejectException("the task id does not belong to this tasker");
        }
        return status.get();
    }

    @Transactional
    public TaskDto updateTaskStatus(Long taskID, Long taskerID, Status newStatus) {
        Status status = validateTaskExistAndTaskerOwner(taskID, taskerID);

        TaskState taskState = statusFactory.createTaskState(status, taskID, taskerID);
        TaskContext taskContext = new TaskContext(taskState);
        TaskState newTaskState = statusFactory.createTaskState(newStatus, taskID, taskerID);
        taskContext.contextChange(newTaskState);
        taskContext.updateWorkedHours();
        taskContext.updateStatus();

        Optional<TaskDto> taskDtoOptional = taskRequestDao.getTaskDetails(taskID);
        assert taskDtoOptional.isPresent();
        TaskDto taskDto = taskDtoOptional.get();
        sendStatusEmail(taskDto);

        simpMessagingTemplate.convertAndSend(WEBSOCKET + taskID, taskDto);

        return taskDto;
    }
}
