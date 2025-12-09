package com.homemate.reviews.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.DTO.ReviewImagesDTO;
import com.homemate.reviews.Dao.ReviewsDao;

@Service
public class ReviewsService {

    private final ReviewsDao reviewsDao;

    public ReviewsService(ReviewsDao reviewsDao) {
        this.reviewsDao = reviewsDao;
    }

    @Transactional
    public ReviewsDTO addReview(ReviewsDTO reviewsDTO) {
        validateReview(reviewsDTO);
        if (reviewsDao.getReviewByTask(reviewsDTO.getTaskId()) != null) {
            throw new IllegalArgumentException("A review already exists for this task.");
        }
        
        List<ReviewImagesDTO> images = reviewsDTO.getReviewImages();
        if (images != null && images.size() > 5) {
            throw new IllegalArgumentException("Cannot add more than 5 images to a review.");
        }

        ReviewsDTO savedReview = reviewsDao.addReview(reviewsDTO);

        Integer taskerId = reviewsDao.getTaskerIdByTaskId(reviewsDTO.getTaskId());
        if (taskerId != null) {
            calculateAndUpdateTaskerRating(taskerId);
        }
        return savedReview;
    }

    public ReviewsDTO getReviewByTask(int taskId) {
        return reviewsDao.getReviewByTask(taskId); // Returns null if not found, controller can handle 404
    }

    @Transactional
    public boolean deleteReview(int reviewId) {
        ReviewsDTO review = reviewsDao.getReviewById(reviewId);
        if (review == null) return false;

        boolean deleted = reviewsDao.deleteReview(reviewId);
        
        if (deleted) {
            Integer taskerId = reviewsDao.getTaskerIdByTaskId(review.getTaskId());
            if (taskerId != null) {
                calculateAndUpdateTaskerRating(taskerId);
            }
        }
        return deleted;
    }

    @Transactional
    public ReviewsDTO updateReview(ReviewsDTO reviewsDTO) {
        validateReview(reviewsDTO);

        ReviewsDTO existing = reviewsDao.getReviewById(reviewsDTO.getReviewId());
        if (existing == null) {
            throw new IllegalArgumentException("Review not found.");
        }

        // Count existing images + new images
        int currentImageCount = reviewsDao.getReviewImageCount(reviewsDTO.getReviewId());
        int newImagesCount = 0;
        if (reviewsDTO.getReviewImages() != null) {
            for (ReviewImagesDTO img : reviewsDTO.getReviewImages()) {
                if (img.getImgId() == 0) newImagesCount++;
            }
        }
        
        if (currentImageCount + newImagesCount > 5) {
             throw new IllegalArgumentException("Cannot have more than 5 images in a review. Delete some images first.");
        }

        reviewsDao.updateReview(reviewsDTO);
        
        Integer taskerId = reviewsDao.getTaskerIdByTaskId(existing.getTaskId());
        if (taskerId != null) {
            calculateAndUpdateTaskerRating(taskerId);
        }

        return reviewsDao.getReviewById(reviewsDTO.getReviewId());
    }

    @Transactional
    public void deleteImage(int imageId) {
        reviewsDao.deleteImage(imageId);
    }

    private void validateReview(ReviewsDTO reviewsDTO) {
        if (reviewsDTO.getRate() < 0 || reviewsDTO.getRate() > 5) {
            throw new IllegalArgumentException("Rate must be between 0 and 5.");
        }
        if (reviewsDTO.getText() != null && reviewsDTO.getText().length() > 50) {
            throw new IllegalArgumentException("Review text cannot exceed 50 characters.");
        }
    }


    private void calculateAndUpdateTaskerRating(int taskerID) {
        Double newRating = reviewsDao.getAverageRatingForTasker(taskerID);
        reviewsDao.updateTaskerRating(taskerID, newRating);
    }
}
