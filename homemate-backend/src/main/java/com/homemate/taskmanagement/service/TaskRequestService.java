package com.homemate.taskmanagement.service;

import com.homemate.TaskerProfile.models.TaskerAvailability;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.domains.model.EmailType;
import com.homemate.notification.service.EmailService;
import com.homemate.security.model.AppUserDetails;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.dto.TaskRequestDto;
import com.homemate.taskmanagement.dto.TaskTimeDto;
import com.homemate.taskmanagement.exceptions.*;
import com.homemate.taskmanagement.mappers.TaskMapper;
import com.homemate.taskmanagement.model.TaskEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor

public class TaskRequestService {

    private final TaskMapper taskMapper;
    private final TaskRequestDao taskRequestDao;
    private final EmailService emailService;

    public Optional<TaskDto> requestTask(TaskRequestDto requestDto){
        checkRequest(requestDto);

        TaskEntity newTask = taskMapper.getTaskEntity(requestDto);
        newTask.setChatID(handleChat(requestDto));

        Optional<Long> taskID = taskRequestDao.insertTask(newTask);

        if (taskID.isEmpty()) throw new IllegalArgumentException("the task not created correctly");
        Optional<TaskDto> taskDto = taskRequestDao.getTaskDetails(taskID.get());
        assert taskDto.isPresent();
        sendTaskRequestMail(taskDto.get());
        return taskDto;


    }

    private void sendTaskRequestMail(TaskDto taskDto) {
        try {
            EmailRequest emailRequest = EmailRequest.builder()
                    .task(taskDto)
                    .emailType(EmailType.TASK_REQUEST)
                    .recipientEmail(taskDto.getTaskerMail())
                    .build();

            emailService.sendEmail(emailRequest);

        } catch (Exception ignored) {
        }
    }

    public Long handleChat(TaskRequestDto requestDto) {
        return getORCreateChat(requestDto.getUserID(),requestDto.getTaskerID());
    }
    private Long getORCreateChat(Long userID,Long taskerID){
        Optional<Long> existingChat = taskRequestDao.getChat(userID, taskerID);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        Optional<Long> newChat = taskRequestDao.insertChat(userID, taskerID);
        return newChat.orElseGet(() -> taskRequestDao.getChat(userID, taskerID)
                .orElseThrow(BadTaskRequestException::new));

    }
    public void checkRequest(TaskRequestDto requestDto){
        if(taskRequestDao.checkIfTaskExist(requestDto.getUserID(),requestDto.getTaskerID(),requestDto.getAddressID())){
            throw new DuplicateRequestException();
        } else if (taskRequestDao.checkIfTaskLimit(requestDto.getUserID(),10)) {
            throw new RequestLimitExceededException();
        }
    }

    public List<TaskTimeDto> getAllBusyTime(Long taskerId, LocalDate day) {
        if(TaskerAvailability.UNAVAILABLE.equals(taskRequestDao.CheckAvailability(taskerId))){
            throw new BadEstimationException("UNAVAILABLE: This Tasker is UNAVAILABLE Now");
        }

        return taskRequestDao.getBusytime(taskerId,day);
    }

    public void addEstimation(Long taskId, int estimation, AppUserDetails userDetails) {
        if (estimation<=0) throw new BadEstimationException("estimation need to be > 0");

        if(checkValidEstimation(estimation,taskId)==false) throw new BadEstimationException("Invalid estimation: This time slot conflicts with another scheduled task");

        Optional<TaskDto> taskDto= taskRequestDao.getTaskDetails(taskId);
        if(taskDto.isPresent() &&( taskDto.get().getTaskerID() == userDetails.getId())) {
         taskRequestDao.addEstimation(taskId,estimation);
        }
        else
            throw new BadEstimationException("This Tasker has no Access to that task");
    }
    public Boolean checkValidEstimation(int estimation, Long taskId) {
        Optional<TaskDto> taskDto = taskRequestDao.getTaskDetails(taskId);
        if (taskDto.isEmpty()) return false;

        List<TaskTimeDto> busyTimes = taskRequestDao.getBusytime(
                taskDto.get().getTaskerID(),
                taskDto.get().getStartDate().toLocalDate()
        );

        LocalDateTime startDateTime = taskDto.get().getStartDate();
        LocalDateTime endDateTime = startDateTime.plusMinutes(estimation);

        for (TaskTimeDto busyTime : busyTimes) {
            LocalDateTime busyStart = busyTime.getStartDate();
            LocalDateTime busyEnd = busyStart.plusMinutes(busyTime.getEstimation());
            if (busyTime.getTaskID().equals(taskId)) {
                continue;
            }
            if (busyTime.getEstimation() <= 0) {
                continue;
            }
            if (startDateTime.isBefore(busyEnd) && endDateTime.isAfter(busyStart)) {
                return false;
            }
        }
        return true;
    }
    public int getTaskDetails(Long taskId) {
        return taskRequestDao.getEstimation(taskId);
    }
}
