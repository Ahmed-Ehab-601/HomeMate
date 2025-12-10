package com.homemate.reviews.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.Dao.ReviewsDao;
import com.homemate.security.model.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;

/**
 * Unit tests for ReviewsService toxic content detection
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewsServiceToxicityTest {

    @Mock
    private ReviewsDao reviewsDao;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private final Long USER_ID = 1L;

    private ReviewsService reviewsService;

    @BeforeEach
    void setUp() {
        reviewsService = new ReviewsService(reviewsDao);
        
        // Mock Security Context
        AppUserDetails userDetails = mock(AppUserDetails.class);
        when(userDetails.getId()).thenReturn(USER_ID);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testAddReview_WithCleanText_ShouldSucceed() {
        // Arrange
        ReviewsDTO reviewDTO = new ReviewsDTO();
        reviewDTO.setReviewId(1);
        reviewDTO.setTaskId(100);
        reviewDTO.setRate(5.0);
        reviewDTO.setText("Great service, very professional!");
        
        
        when(reviewsDao.getReviewByTask(100)).thenReturn(null);
        when(reviewsDao.addReview(any())).thenReturn(reviewDTO);
        when(reviewsDao.getTaskStatus(reviewDTO.getTaskId())).thenReturn("Done");
        when(reviewsDao.getTaskerIdByTaskId(100)).thenReturn(1);
        when(reviewsDao.getAverageRatingForTasker(1)).thenReturn(4.5);

        // Authorization mock
        when(reviewsDao.getUserIdByTaskId(100)).thenReturn(USER_ID);

        assertDoesNotThrow(() -> reviewsService.addReview(reviewDTO));
    }

    @Test
    void testAddReview_WithEmptyText_ShouldSucceed() {
        // Arrange
        ReviewsDTO reviewDTO = new ReviewsDTO();
        reviewDTO.setReviewId(1);
        reviewDTO.setTaskId(100);
        reviewDTO.setRate(4.0);
        reviewDTO.setText("");
        
        when(reviewsDao.getReviewByTask(100)).thenReturn(null);
        when(reviewsDao.addReview(any())).thenReturn(reviewDTO);
        when(reviewsDao.getTaskStatus(reviewDTO.getTaskId())).thenReturn("Done");
        when(reviewsDao.getTaskerIdByTaskId(100)).thenReturn(1);
        when(reviewsDao.getAverageRatingForTasker(1)).thenReturn(4.5);

        // Authorization mock
        when(reviewsDao.getUserIdByTaskId(100)).thenReturn(USER_ID);

        assertDoesNotThrow(() -> reviewsService.addReview(reviewDTO));
    }

    @Test
    void testAddReview_WithNullText_ShouldSucceed() {
        // Arrange
        ReviewsDTO reviewDTO = new ReviewsDTO();
        reviewDTO.setReviewId(1);
        reviewDTO.setTaskId(100);
        reviewDTO.setRate(4.0);
        reviewDTO.setText(null);
        
        when(reviewsDao.getReviewByTask(100)).thenReturn(null);
        when(reviewsDao.addReview(any())).thenReturn(reviewDTO);
        when(reviewsDao.getTaskStatus(reviewDTO.getTaskId())).thenReturn("Done");
        when(reviewsDao.getTaskerIdByTaskId(100)).thenReturn(1);
        when(reviewsDao.getAverageRatingForTasker(1)).thenReturn(4.5);

        // Authorization mock
        when(reviewsDao.getUserIdByTaskId(100)).thenReturn(USER_ID);

        // Act & Assert - Null text should skip toxicity check
        assertDoesNotThrow(() -> reviewsService.addReview(reviewDTO));
    }

    @Test
    void testValidateReview_WithTooLongText_ShouldThrow() {
        // Arrange
        ReviewsDTO reviewDTO = new ReviewsDTO();
        reviewDTO.setTaskId(100);
        reviewDTO.setRate(5.0);
        reviewDTO.setText("a".repeat(51)); // Exceeds 50 character limit
        
        when(reviewsDao.getReviewByTask(100)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewsService.addReview(reviewDTO)
        );
        assertEquals("Review text cannot exceed 50 characters.", exception.getMessage());
    }

    @Test
    void testValidateReview_WithInvalidRating_ShouldThrow() {
        // Arrange
        ReviewsDTO reviewDTO = new ReviewsDTO();
        reviewDTO.setTaskId(100);
        reviewDTO.setRate(6.0); // Exceeds 5.0 max
        reviewDTO.setText("Good");
        
        when(reviewsDao.getReviewByTask(100)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> reviewsService.addReview(reviewDTO)
        );
        assertEquals("Rate must be between 0 and 5.", exception.getMessage());
    }
}
