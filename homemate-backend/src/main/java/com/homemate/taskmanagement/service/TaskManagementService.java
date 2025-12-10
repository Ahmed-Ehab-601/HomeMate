package com.homemate.taskmanagement.service;

//import com.homemate.TaskManagement.Dao.impl.TaskDaoImpl;
import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.TaskerProfile.DTO.ReviewImageDTO;
import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.notification.domains.dto.EmailRequest;
import com.homemate.notification.service.EmailService;
import com.homemate.taskmanagement.dao.TaskDao;
import com.homemate.taskmanagement.dao.impl.TaskDaoImpl;
import com.homemate.taskmanagement.dto.*;
import com.homemate.taskmanagement.exceptions.*;
import com.homemate.taskmanagement.mappers.TaskMapper;
import com.homemate.taskmanagement.model.Status;
import com.homemate.taskmanagement.model.TaskEntity;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.config.Task;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TaskManagementService {
    private final TaskMapper taskMapper;
    private final TaskDao taskDao;
    private final StatusFactory statusFactory;
    private final ReviewDao reviewDao;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final EmailService emailService;


    public Optional<TaskDto> requestTask(TaskRequestDto requestDto){
        checkRequest(requestDto);
        TaskEntity newTask = taskMapper.getTaskEntity(requestDto);
        newTask.setChatID(handleChat(requestDto));
        Optional<Long> taskID = taskDao.insertTask(newTask);
        if(taskID.isEmpty()) throw new IllegalArgumentException("the task not created correctly");
//        //send email
        try {
            TaskDto taskDto = taskDao.getTaskDetails(taskID.get())
                    .orElseThrow(() -> new IllegalArgumentException("Task details missing"));

            EmailRequest emailRequest = EmailRequest.builder()
                    .task(taskDto)
                    .emailType(EmailRequest.EmailType.TASK_REQUEST)
                    .recipientEmail(taskDto.getTaskerMail())
                    .build();

            emailService.sendTaskerEmail(emailRequest);

        } catch (Exception e) {
            System.out.println("Failed to send email: " + e.getMessage());
        }

        return getTaskDetails(taskID.get(),requestDto.getUserID());

    }
    public Long handleChat(TaskRequestDto requestDto) {
        return getORCreateChat(requestDto.getUserID(),requestDto.getTaskerID());
    }
    private Long getORCreateChat(Long userID,Long taskerID){
        Optional<Long> existingChat = taskDao.getChat(userID, taskerID);
        if (existingChat.isPresent()) {
            return existingChat.get();
        }
        Optional<Long> newChat = taskDao.insertChat(userID, taskerID);
        return newChat.orElseGet(() -> taskDao.getChat(userID, taskerID)
                .orElseThrow(BadTaskRequestException::new));

    }
    public void checkRequest(TaskRequestDto requestDto){
        if(taskDao.checkIfTaskExist(requestDto.getUserID(),requestDto.getTaskerID(),requestDto.getAddressID())){
            throw new DuplicateRequestException();
        } else if (taskDao.checkIfTaskLimit(requestDto.getUserID(),10)) {
            throw new RequestLimitExceededException();
        }
    }

    public Optional<PaginatedResponse> getUserTasks(Long userID, StatusDto statusDto, int page, int pageSize){

        if(statusDto == StatusDto.All){
            Optional<Long> totalCount =  taskDao.countTasksByUserID(userID);
            if (totalCount.isEmpty() || totalCount.get() == 0){
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListUserTasksByIDSortedByDate(userID,page,pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(),pageSize))
                    .build();
            return Optional.ofNullable(response);

        }else{
            Optional<Long> totalCount =  taskDao.countTasksByUserIDAndStatus(userID,statusDto);
            if (totalCount.isEmpty()){
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListUserTasksByIDAndStatusSortedByDate(userID,statusDto,page,pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(),pageSize))
                    .build();
            return Optional.ofNullable(response);

        }
    }

    public Optional<PaginatedResponse> getTaskerTasks(Long taskerID, StatusDto statusDto, int page, int pageSize) {

        if (statusDto == StatusDto.All) {
            Optional<Long> totalCount = taskDao.countTasksByTaskerID(taskerID);
            if (totalCount.isEmpty() || totalCount.get() == 0) {
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListTaskerTasksByIDSortedByDate(taskerID, page, pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(), pageSize))
                    .build();
            return Optional.ofNullable(response);

        } else {
            Optional<Long> totalCount = taskDao.countTasksByTaskerIDAndStatus(taskerID, statusDto);
            if (totalCount.isEmpty()) {
                return Optional.empty();
            }
            List<TaskCardDto> tasks = taskDao.getListTaskerTasksByIDAndStatusSortedByDate(taskerID, statusDto, page, pageSize);
            PaginatedResponse response = PaginatedResponse.builder().
                    tasks(tasks).
                    page(page).
                    pageSize(pageSize).
                    totalCount(totalCount.get()).
                    totalPages(Math.ceilDiv(totalCount.get(), pageSize))
                    .build();
            return Optional.ofNullable(response);

        }
    }

    public TaskRequestResponseDto acceptOrReject(Long taskID,Long taskerID,Status newStatus){
        Optional<Status> status = taskDao.getStatus(taskID);
        if(status.isEmpty()) {
            throw new TaskNotFoundException("wrong task id");
        }
        if(!status.get().equals(Status.InReview)){
            throw new BadAcceptRejectException("bad request the task must be inReview");
        }
        Optional<Long> id = taskDao.getTaskerID(taskID);
        if(id.isEmpty() || ! id.get().equals(taskerID) ){
            throw new BadAcceptRejectException("the task id does not belong to this tasker");
        }
        taskDao.updateStatus(taskID,newStatus);
        TaskDto taskDto = taskDao.getTaskDetails(taskID).get();
        simpMessagingTemplate.convertAndSend("/send/task/"+taskID,taskDto);

        // TO DO SEND EMAIL
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(taskDto.getUserMail())
                .task(taskDto)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .build();

        emailService.sendUserEmail(emailRequest);

        return new TaskRequestResponseDto(taskID,newStatus);


    }

    @Transactional
    public TaskDto updateTaskStatus(Long taskID,Long taskerID,Status newStatus){
        Optional<Status> status = taskDao.getStatus(taskID);
        if(status.isEmpty()) {
            throw new TaskNotFoundException("wrong task id");
        }
        Optional<Long> id = taskDao.getTaskerID(taskID);
        if(id.isEmpty() || ! id.get().equals(taskerID) ){
            throw new BadStateUpdateException("the task id does not belong to this tasker");
        }
        TaskState taskState = statusFactory.createTaskState(status.get(),taskID,taskerID);
        TaskContext taskContext = new TaskContext(taskState);
        TaskState newTaskState = statusFactory.createTaskState(newStatus,taskID,taskerID);
        taskContext.contextChange(newTaskState);
        taskContext.updateWorkedHours();
        taskContext.updateStatus();
        TaskDto taskDto = taskDao.getTaskDetails(taskID).get();
        simpMessagingTemplate.convertAndSend("/send/task/"+taskID,taskDto);
        // TO DO SEND EMAIL
        EmailRequest emailRequest = EmailRequest.builder()
                .recipientEmail(taskDto.getUserMail())
                .task(taskDto)
                .emailType(EmailRequest.EmailType.TASK_STATUS)
                .build();

        emailService.sendUserEmail(emailRequest);

        return taskDto;
    }

    public RescheduleResponseDto rescheduleTask(Long taskID, RescheduleRequestDto rescheduleRequestDto ,long requestID) {

        Optional<Long> taskerID = taskDao.getTaskerID(taskID);
        Optional<Long> userID = taskDao.getUserID(taskID);


        TaskDto taskDto = taskDao.getTaskDetails(taskID)
                .orElseThrow(() -> new TaskNotFoundException("Task with ID " + taskID + " not found"));


        if (requestID != taskerID.get() && requestID!= userID.get()) {
            throw new BadRescheduleException("You are neither the user nor the tasker for this task");
        }

        Optional<Status> status = taskDao.getStatus(taskID);
        if (status.isEmpty() ||
                (status.get() != Status.InReview && status.get() != Status.Accepted)) {
            throw new BadRescheduleException("Task must be InReview or Accepted");
        }
        LocalDateTime newStart =rescheduleRequestDto.getNewStartDate();

        if (newStart.isBefore(LocalDateTime.now())) {
            throw new BadRescheduleException("New date cannot be in the past");
        }

        boolean updated = taskDao.updateTaskStartDate(taskID, newStart);
        if (!updated) {
            throw new IllegalStateException("Task could not be rescheduled");
        }
        simpMessagingTemplate.convertAndSend("/send/task/"+taskID,taskDao.getTaskDetails(taskID));
        // TO DO SEND EMAIL

        EmailRequest userMail = EmailRequest.builder()
                .recipientEmail((taskDto.getUserMail()))
                .task(taskDto)
                .emailType(EmailRequest.EmailType.TASK_RESCHEDULE)
                .build();
        emailService.sendUserEmail(userMail);

        EmailRequest taskerMail = EmailRequest.builder()
                .recipientEmail(taskDto.getTaskerMail())
                .task(taskDto)
                .emailType(EmailRequest.EmailType.TASK_RESCHEDULE)
                .build();
        emailService.sendTaskerEmail(taskerMail);

        return RescheduleResponseDto.builder()
                .taskID(taskID)
                .newStartDate(newStart)
                .rescheduleStatus(StatusDto.Accepted)
                .build();
    }



    public Optional<TaskDto> getTaskDetails(Long taskID ,Long viewerID){
        Optional<Long> taskerID = taskDao.getTaskerID(taskID);
        Optional<Long> userID = taskDao.getUserID(taskID);

        if (taskerID.isEmpty() || userID.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + taskID + " not found");
        }

        if (!viewerID.equals(taskerID.get()) && !viewerID.equals(userID.get())) {
            throw new BadViewExecption("You are neither the user nor the tasker for this task");
        }

        return taskDao.getTaskDetails(taskID);
    }


    public Optional<ReviewDTO> getReviewByTaskId(long taskID, long viewerID) {
        Optional<Long> userID = taskDao.getUserID(taskID);
        Optional<Long> taskerID = taskDao.getTaskerID(taskID);

        if (userID.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + taskID + " not found");
        }

        if (!viewerIDEquals(userID.get(), viewerID) &&
                (taskerID.isEmpty() || !viewerIDEquals(taskerID.get(), viewerID))) {
            throw new BadViewExecption("You are neither the user nor the tasker for this task");
        }

        Optional<ReviewDTO> review = taskDao.getReviewByTaskId(taskID);
        if (review.isEmpty()) return review;

        ReviewDTO reviewDTO = review.get();
        List<ReviewImageDTO> images = reviewDao.getReviewImages(reviewDTO.getReviewId());
        reviewDTO.setReviewImages(images);

        return Optional.of(reviewDTO);
    }

    public boolean viewerIDEquals(Long id, long viewerID) {
        return id != null && id.equals(viewerID);
    }




}
