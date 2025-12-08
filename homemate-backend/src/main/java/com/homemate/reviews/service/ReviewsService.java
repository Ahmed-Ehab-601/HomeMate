package com.homemate.reviews.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homemate.reviews.DTO.ReviewDTO;
import com.homemate.reviews.DTO.ReviewImagesDTO;
import com.homemate.reviews.Dao.ReviewsDao;

@Service
public class ReviewsService {

    private final ReviewsDao reviewsDao;

    public ReviewsService(ReviewsDao reviewsDao) {
        this.reviewsDao = reviewsDao;
    }

    @Transactional
    public ReviewDTO addReview(ReviewDTO reviewDTO) {
        validateReview(reviewDTO);
        if (reviewsDao.getReviewByTask(reviewDTO.getTaskId()) != null) {
            throw new IllegalArgumentException("A review already exists for this task.");
        }
        
        List<ReviewImagesDTO> images = reviewDTO.getReviewImages();
        if (images != null && images.size() > 5) {
            throw new IllegalArgumentException("Cannot add more than 5 images to a review.");
        }

        return reviewsDao.addReview(reviewDTO);
    }

    public ReviewDTO getReviewByTask(int taskId) {
        return reviewsDao.getReviewByTask(taskId); // Returns null if not found, controller can handle 404
    }

    @Transactional
    public boolean deleteReview(int reviewId) {
        return reviewsDao.deleteReview(reviewId);
    }

    @Transactional
    public ReviewDTO updateReview(ReviewDTO reviewDTO) {
        validateReview(reviewDTO);

        ReviewDTO existing = reviewsDao.getReviewById(reviewDTO.getReviewId());
        if (existing == null) {
            throw new IllegalArgumentException("Review not found.");
        }

        // Count existing images + new images
        int currentImageCount = reviewsDao.getReviewImageCount(reviewDTO.getReviewId());
        int newImagesCount = 0;
        if (reviewDTO.getReviewImages() != null) {
            for (ReviewImagesDTO img : reviewDTO.getReviewImages()) {
                if (img.getImgId() == 0) newImagesCount++;
            }
        }
        
        if (currentImageCount + newImagesCount > 5) {
             throw new IllegalArgumentException("Cannot have more than 5 images in a review. Delete some images first.");
        }

        reviewsDao.updateReview(reviewDTO);
        return reviewsDao.getReviewById(reviewDTO.getReviewId());
    }

    @Transactional
    public void deleteImage(int imageId) {
        reviewsDao.deleteImage(imageId);
    }

    private void validateReview(ReviewDTO reviewDTO) {
        if (reviewDTO.getRate() < 0 || reviewDTO.getRate() > 5) {
            throw new IllegalArgumentException("Rate must be between 0 and 5.");
        }
        if (reviewDTO.getText() != null && reviewDTO.getText().length() > 50) {
            throw new IllegalArgumentException("Review text cannot exceed 50 characters.");
        }
    }
}
