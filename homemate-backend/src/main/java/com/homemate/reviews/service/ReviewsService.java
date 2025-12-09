package com.homemate.reviews.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.DTO.ReviewImagesDTO;
import com.homemate.reviews.Dao.ReviewsDao;

@Service
public class ReviewsService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewsService.class);
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
        
        // Check for toxic content
        if (reviewsDTO.getText() != null && !reviewsDTO.getText().trim().isEmpty()) {
            try {
                boolean isToxic = checkToxicity(reviewsDTO.getText());
                if (isToxic) {
                    throw new IllegalArgumentException("Review text contains inappropriate or toxic content.");
                }
            } catch (IllegalArgumentException e) {
                throw e; // Re-throw validation errors
            } catch (Exception e) {
                logger.error("Error checking toxicity: {}", e.getMessage(), e);
                // Fail-safe: allow review if toxicity check fails
                logger.warn("Toxicity check failed, allowing review to proceed");
            }
        }
    }
    
    /**
     * Check if text is toxic using Python toxic-bert model.
     * Extracts Python script from JAR to temp file and executes it.
     * 
     * @param text The text to check
     * @return true if text is toxic, false otherwise
     * @throws Exception if Python execution fails
     */
    private boolean checkToxicity(String text) throws Exception {
        File tempFile = null;
        try {
            // Extract python script from JAR to temp file
            InputStream in = getClass().getResourceAsStream("/toxic_detector.py");
            if (in == null) {
                logger.warn("toxic_detector.py not found in resources, skipping toxicity check");
                return false;
            }
            
            tempFile = File.createTempFile("toxic_detector", ".py");
            tempFile.deleteOnExit();
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            in.close();

            // Run Python script
            ProcessBuilder pb = new ProcessBuilder(
                "python", tempFile.getAbsolutePath(), text
            );
            pb.redirectErrorStream(true);

            Process p = pb.start();
            
            // Read output
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String result = reader.readLine();
            reader.close();
            
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                logger.error("Python script exited with code: {}", exitCode);
                return false; // Fail-safe
            }
            
            return "true".equalsIgnoreCase(result);
        } finally {
            // Clean up temp file
            if (tempFile != null && tempFile.exists()) {
                try {
                    tempFile.delete();
                } catch (Exception e) {
                    logger.warn("Failed to delete temp file: {}", e.getMessage());
                }
            }
        }
    }


    private void calculateAndUpdateTaskerRating(int taskerID) {
        Double newRating = reviewsDao.getAverageRatingForTasker(taskerID);
        reviewsDao.updateTaskerRating(taskerID, newRating);
    }
}
