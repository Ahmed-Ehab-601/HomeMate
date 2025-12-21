package com.homemate.reviews.dao.imp;

import com.homemate.reviews.DTO.ReviewImagesDTO;
import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.Dao.ReviewsDao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("reviews")
public class ReviewsDaoIntegrationTest {

    @Autowired
    private ReviewsDao reviewsDao;

    @Test
    @DisplayName("GetReviewByTask - Should return review when it exists")
    public void testGetReviewByTask_Found() {
        ReviewsDTO review = reviewsDao.getReviewByTask(1); // Task 1 has Review 1
        assertThat(review).isNotNull();
        assertThat(review.getReviewId()).isEqualTo(1);
        assertThat(review.getText()).isEqualTo("Great job!");
        assertThat(review.getRate()).isEqualTo(5.0);
        assertThat(review.getReviewImages()).hasSize(1);
    }

    @Test
    @DisplayName("GetReviewByTask - Should return null when not found")
    public void testGetReviewByTask_NotFound() {
        ReviewsDTO review = reviewsDao.getReviewByTask(2); // Task 2 has no review in seed data (Wait, check data)
        // Task 2 was inserted but no Review inserted for Task 2 in reviewsData.sql.
        assertThat(review).isNull();
    }

    @Test
    @DisplayName("GetReviewById - Should return review")
    public void testGetReviewById() {
        ReviewsDTO review = reviewsDao.getReviewById(1);
        assertThat(review).isNotNull();
        assertThat(review.getTaskId()).isEqualTo(1);
    }

    @Test
    @DisplayName("AddReview - Should persist new review")
    public void testAddReview() {
        // Add review for Task 2
        ReviewsDTO newReview = new ReviewsDTO();
        newReview.setTaskId(2);
        newReview.setText("Another review");
        newReview.setRate(4.0);
        newReview.setReviewImages(Collections.emptyList());

        ReviewsDTO saved = reviewsDao.addReview(newReview);

        assertThat(saved).isNotNull();
        assertThat(saved.getReviewId()).isGreaterThan(0);
        assertThat(saved.getText()).isEqualTo("Another review");

        // Verify retrieval
        ReviewsDTO retrieved = reviewsDao.getReviewByTask(2);
        assertThat(retrieved).isNotNull();
        assertThat(retrieved.getText()).isEqualTo("Another review");
    }

    @Test
    @DisplayName("AddReview - With Images")
    public void testAddReviewWithImages() {
        // Add review for Task 3
        ReviewsDTO newReview = new ReviewsDTO();
        newReview.setTaskId(3);
        newReview.setText("With image");
        newReview.setRate(3.0);
        
        ReviewImagesDTO img = new ReviewImagesDTO();
        img.setImgName("test.jpg");
        img.setFormat("jpg");
        img.setImgFile("1234");
        
        newReview.setReviewImages(Collections.singletonList(img));

        ReviewsDTO saved = reviewsDao.addReview(newReview);

        assertThat(saved.getReviewImages()).hasSize(1);
        assertThat(saved.getReviewImages().get(0).getImgName()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("DeleteReview - Should remove review and images")
    public void testDeleteReview() {
        boolean deleted = reviewsDao.deleteReview(1); // Delete Review 1
        assertThat(deleted).isTrue();

        ReviewsDTO review = reviewsDao.getReviewById(1);
        assertThat(review).isNull();
        
        // Ensure images are deleted (Cascade)
        int count = reviewsDao.getReviewImageCount(1);
        assertThat(count).isEqualTo(0);
    }

    @Test
    @DisplayName("UpdateReview - Should update text and rate")
    public void testUpdateReview() {
        ReviewsDTO review = reviewsDao.getReviewById(1);
        review.setText("Updated text");
        review.setRate(2.5);
        review.setReviewImages(null); // Don't allow images update logic to interfere here

        reviewsDao.updateReview(review);

        ReviewsDTO updated = reviewsDao.getReviewById(1);
        assertThat(updated.getText()).isEqualTo("Updated text");
        assertThat(updated.getRate()).isEqualTo(2.5);
    }
    
    @Test
    @DisplayName("GetTaskerIdByTaskId - Should return correct ID")
    public void testGetTaskerIdByTaskId() {
        // Task 1 -> Tasker 1
        Integer taskerId = reviewsDao.getTaskerIdByTaskId(1);
        assertThat(taskerId).isEqualTo(1);
        
        // Task 2 -> Tasker 2
        Integer taskerId2 = reviewsDao.getTaskerIdByTaskId(2);
        assertThat(taskerId2).isEqualTo(2);
    }

    @Test
    @DisplayName("GetAverageRatingForTasker - Should calculate correctly")
    public void testGetAverageRatingForTasker() {
        // Tasker 1 has Task 1 with Review 1 (Rate 5.0).
        Double avg = reviewsDao.getAverageRatingForTasker(1);
        assertThat(avg).isEqualTo(5.0);
    }
    
    @Test
    @DisplayName("UpdateTaskerRating - Should update table")
    public void testUpdateTaskerRating() {
         reviewsDao.updateTaskerRating(1, 4.2);
    }
}
