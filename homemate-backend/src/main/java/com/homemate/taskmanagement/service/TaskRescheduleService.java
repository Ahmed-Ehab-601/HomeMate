package com.homemate.taskmanagement.service;

import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.domains.model.RecipientType;
import com.homemate.notification.service.EmailService;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskRescheduleDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.RescheduleRequestDto;
import com.homemate.taskmanagement.dto.RescheduleResponseDto;
import com.homemate.taskmanagement.dto.StatusDto;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadRescheduleException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import com.homemate.taskmanagement.model.Status;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskRescheduleService {

    private final TaskRescheduleDao taskRescheduleDao;
    private final TaskStatusDao taskStatusDao;
    private final EmailService emailService;
    private final TaskRequestDao taskRequestDao;
    private final SimpMessagingTemplate simpMessagingTemplate;


    public RescheduleResponseDto rescheduleTask(Long taskID, RescheduleRequestDto rescheduleRequestDto , Long requestID) {

        validateReschedule(taskID, requestID, rescheduleRequestDto);

        LocalDateTime newStart = rescheduleRequestDto.getNewStartDate();
        boolean updated = taskRescheduleDao.updateTaskStartDate(taskID, newStart);
        if (!updated) {
            throw new IllegalStateException("Task could not be rescheduled");
        }

        Optional<TaskDto> taskDto = taskRequestDao.getTaskDetails(taskID);
        assert taskDto.isPresent();


        sendRescheduleEmail(taskDto.get());

        simpMessagingTemplate.convertAndSend("/send/task/"+ taskID,taskDto);

        return RescheduleResponseDto.builder()
                .taskID(taskID)
                .newStartDate(newStart)
                .rescheduleStatus(StatusDto.Accepted)
                .build();


    }

    private void sendRescheduleEmail(TaskDto taskDto) {
        EmailRequest userMail = EmailRequest.builder()
                .recipientEmail((taskDto.getUserMail()))
                .task(taskDto)
                .emailType(EmailType.TASK_RESCHEDULE)
                .recipientType(RecipientType.USER)
                .build();
        emailService.sendEmail(userMail);

        EmailRequest taskerMail = EmailRequest.builder()
                .recipientEmail(taskDto.getTaskerMail())
                .task(taskDto)
                .emailType(EmailType.TASK_RESCHEDULE)
                .recipientType(RecipientType.TASKER)
                .build();
        emailService.sendEmail(taskerMail);

    }

    private void validateReschedule(Long taskID,Long viewerID,RescheduleRequestDto rescheduleRequestDto){
        Optional<Long> taskerID = taskStatusDao.getTaskerID(taskID);
        Optional<Long> userID = taskRescheduleDao.getUserID(taskID);

        if (userID.isEmpty() || taskerID.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + taskID + " not found");
        }

        if (!viewerID.equals(taskerID.get()) && !viewerID.equals(userID.get())) {
            throw new BadRescheduleException("You are neither the user nor the tasker for this task");
        }

        Optional<Status> status = taskStatusDao.getStatus(taskID);
        if (status.isEmpty() ||
                (status.get() != Status.InReview && status.get() != Status.Accepted)) {
            throw new BadRescheduleException("Task must be InReview or Accepted");
        }
        if(checkValidEstimation(taskID,rescheduleRequestDto.getNewStartDate())==false) {
            throw new BadRescheduleException("Task must be Rescheduled to valid Time");
        }
        LocalDateTime newStart = rescheduleRequestDto.getNewStartDate();
        if (newStart.isBefore(LocalDateTime.now())) {
            throw new BadRescheduleException("New date cannot be in the past");
        }
    }
    public Boolean checkValidEstimation(Long taskId, LocalDateTime newStartDateTime) {
        Optional<TaskDto> taskDto = taskRequestDao.getTaskDetails(taskId);
        if (taskDto.isEmpty()) return false;

        int estimation = taskRequestDao.getEstimation(taskId);

        Map<LocalDateTime, Integer> mp = taskRequestDao.getBusytime(
                taskDto.get().getTaskerID(),
                newStartDateTime.toLocalDate()
        );

        LocalDateTime endDateTime = newStartDateTime.plusMinutes(estimation);

        for (Map.Entry<LocalDateTime, Integer> entry : mp.entrySet()) {
            LocalDateTime busyStart = entry.getKey();
            LocalDateTime busyEnd = busyStart.plusMinutes(entry.getValue());

            if (newStartDateTime.isBefore(busyEnd) && endDateTime.isAfter(busyStart)) {
                return false;
            }
        }
        return true;
    }


}
