package com.homemate.taskmanagement.service;

import com.homemate.TaskerProfile.DTO.ReviewDTO;
import com.homemate.TaskerProfile.DTO.ReviewImageDTO;
import com.homemate.TaskerProfile.Dao.ReviewDao;
import com.homemate.taskmanagement.dao.TaskRequestDao;
import com.homemate.taskmanagement.dao.TaskRescheduleDao;
import com.homemate.taskmanagement.dao.TaskReviewDao;
import com.homemate.taskmanagement.dao.TaskStatusDao;
import com.homemate.taskmanagement.dto.TaskDto;
import com.homemate.taskmanagement.exceptions.BadViewException;
import com.homemate.taskmanagement.exceptions.TaskNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class TaskViewService {

    private final TaskStatusDao taskStatusDao;
    private final TaskRescheduleDao taskRescheduleDao;
    private final TaskReviewDao taskReviewDao;
    private final ReviewDao reviewDao;
    private final TaskRequestDao taskRequestDao;

    public Optional<ReviewDTO> getReviewByTaskId(Long taskID, Long viewerID) {
        validateTaskFoundAndUserOwner(taskID, viewerID);

        Optional<ReviewDTO> review = taskReviewDao.getReviewByTaskId(taskID);
        if (review.isEmpty()) return review;

        ReviewDTO reviewDTO = review.get();
        List<ReviewImageDTO> images = reviewDao.getReviewImages(reviewDTO.getReviewId());
        reviewDTO.setReviewImages(images);

        return Optional.of(reviewDTO);
    }

    private void validateTaskFoundAndUserOwner(Long taskID, Long viewerID) throws BadViewException {
        Optional<Long> userID = taskRescheduleDao.getUserID(taskID);
        Optional<Long> taskerID = taskStatusDao.getTaskerID(taskID);

        if (userID.isEmpty() || taskerID.isEmpty()) {
            throw new TaskNotFoundException("Task with ID " + taskID + " not found");
        }
        if (!viewerID.equals(taskerID.get()) && !viewerID.equals(userID.get())) {
            throw new BadViewException("You are neither the user nor the tasker for this task");
        }

    }



    public Optional<TaskDto> getTaskDetails(Long taskID , Long viewerID){
        validateTaskFoundAndUserOwner(taskID,viewerID);
        return taskRequestDao.getTaskDetails(taskID);
    }

}
