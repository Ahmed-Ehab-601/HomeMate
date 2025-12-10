package com.homemate.reviews.service;

import com.homemate.reviews.DTO.ReviewImagesDTO;
import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.Dao.ReviewsDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.homemate.security.model.AppUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewsServiceTest {

    @Mock
    private ReviewsDao reviewsDao;

    @InjectMocks
    private ReviewsService reviewsService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private ReviewsDTO reviewsDTO;
    private final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        reviewsDTO = new ReviewsDTO();
        reviewsDTO.setReviewId(1);
        reviewsDTO.setTaskId(100);
        reviewsDTO.setText("Great service!");
        reviewsDTO.setRate(5.0);
        reviewsDTO.setReviewImages(new ArrayList<>());
        
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
    void addReview_ShouldSucceed_WhenValid() {
        when(reviewsDao.getReviewByTask(reviewsDTO.getTaskId())).thenReturn(null);
        when(reviewsDao.addReview(reviewsDTO)).thenReturn(reviewsDTO);
        when(reviewsDao.getTaskerIdByTaskId(reviewsDTO.getTaskId())).thenReturn(10);
        when(reviewsDao.getAverageRatingForTasker(10)).thenReturn(4.5);
        
        // Authorization mock
        when(reviewsDao.getUserIdByTaskId(reviewsDTO.getTaskId())).thenReturn(USER_ID);
        // Task Status mock
        when(reviewsDao.getTaskStatus(reviewsDTO.getTaskId())).thenReturn("Done");

        ReviewsDTO result = reviewsService.addReview(reviewsDTO);

        assertNotNull(result);
        assertEquals(reviewsDTO.getText(), result.getText());
        verify(reviewsDao).addReview(reviewsDTO);
        verify(reviewsDao).updateTaskerRating(10, 4.5);
    }

    @Test
    void addReview_ShouldThrow_WhenReviewExists() {
        when(reviewsDao.getUserIdByTaskId(reviewsDTO.getTaskId())).thenReturn(USER_ID);
        when(reviewsDao.getTaskStatus(reviewsDTO.getTaskId())).thenReturn("Done");
        when(reviewsDao.getReviewByTask(reviewsDTO.getTaskId())).thenReturn(new ReviewsDTO());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewsService.addReview(reviewsDTO);
        });

        assertEquals("A review already exists for this task.", exception.getMessage());
        verify(reviewsDao, never()).addReview(any());
    }

    @Test
    void addReview_ShouldThrow_WhenTooManyImages() {
        List<ReviewImagesDTO> images = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            images.add(new ReviewImagesDTO());
        }
        reviewsDTO.setReviewImages(images);

        reviewsDTO.setReviewImages(images);

        when(reviewsDao.getUserIdByTaskId(reviewsDTO.getTaskId())).thenReturn(USER_ID);
        when(reviewsDao.getTaskStatus(reviewsDTO.getTaskId())).thenReturn("Done");
        when(reviewsDao.getReviewByTask(reviewsDTO.getTaskId())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewsService.addReview(reviewsDTO);
        });

        assertEquals("Cannot add more than 5 images to a review.", exception.getMessage());
    }

    @Test
    void addReview_ShouldThrow_WhenRateInvalid() {
        reviewsDTO.setRate(6.0); // Invalid rate

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewsService.addReview(reviewsDTO);
        });

        assertEquals("Rate must be between 0 and 5.", exception.getMessage());
    }

    @Test
    void addReview_ShouldThrow_WhenTextTooLong() {
        String longText = "a".repeat(51);
        reviewsDTO.setText(longText);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewsService.addReview(reviewsDTO);
        });

        assertEquals("Review text cannot exceed 50 characters.", exception.getMessage());
    }

    @Test
    void getReviewByTask_ShouldReturnReview_WhenFound() {
        when(reviewsDao.getReviewByTask(100)).thenReturn(reviewsDTO);

        ReviewsDTO result = reviewsService.getReviewByTask(100);

        assertNotNull(result);
        assertEquals(100, result.getTaskId());
    }

    @Test
    void getReviewByTask_ShouldReturnNull_WhenNotFound() {
        when(reviewsDao.getReviewByTask(999)).thenReturn(null);

        ReviewsDTO result = reviewsService.getReviewByTask(999);

        assertNull(result);
    }

    @Test
    void deleteReview_ShouldReturnTrue_WhenReviewDeleted() {
        when(reviewsDao.getReviewById(1)).thenReturn(reviewsDTO);
        when(reviewsDao.deleteReview(1)).thenReturn(true);
        when(reviewsDao.getTaskerIdByTaskId(reviewsDTO.getTaskId())).thenReturn(10);
        when(reviewsDao.getAverageRatingForTasker(10)).thenReturn(4.2);
        
        // Authorization mock
        when(reviewsDao.getUserIdByTaskId(reviewsDTO.getTaskId())).thenReturn(USER_ID);
        when(reviewsDao.getTaskStatus(reviewsDTO.getTaskId())).thenReturn("Done");

        boolean result = reviewsService.deleteReview(1);

        assertTrue(result);
        verify(reviewsDao).updateTaskerRating(10, 4.2);
    }

    @Test
    void deleteReview_ShouldReturnFalse_WhenReviewNotFound() {
        when(reviewsDao.getReviewById(1)).thenReturn(null);

        boolean result = reviewsService.deleteReview(1);

        assertFalse(result);
        verify(reviewsDao, never()).deleteReview(anyInt());
    }

    @Test
    void deleteReview_ShouldReturnFalse_WhenDeletionFails() {
        when(reviewsDao.getReviewById(1)).thenReturn(reviewsDTO);
        when(reviewsDao.deleteReview(1)).thenReturn(false);
        
        // Authorization mock
        when(reviewsDao.getUserIdByTaskId(reviewsDTO.getTaskId())).thenReturn(USER_ID);
        when(reviewsDao.getTaskStatus(reviewsDTO.getTaskId())).thenReturn("Done");

        boolean result = reviewsService.deleteReview(1);

        assertFalse(result);
        verify(reviewsDao, never()).updateTaskerRating(anyInt(), anyDouble());
    }

    @Test
    void updateReview_ShouldSucceed_WhenValid() {
        when(reviewsDao.getReviewById(reviewsDTO.getReviewId())).thenReturn(reviewsDTO);
        when(reviewsDao.getReviewImageCount(reviewsDTO.getReviewId())).thenReturn(2);
        // Add 2 valid existing images
        List<ReviewImagesDTO> newImages = new ArrayList<>();
        // Assuming images with imgId 0 are new
        ReviewImagesDTO newImg = new ReviewImagesDTO();
        newImg.setImgId(0);
        newImages.add(newImg);
        reviewsDTO.setReviewImages(newImages); // 2 existing + 1 new = 3 < 5. OK.

        // Authorization mock
        when(reviewsDao.getUserIdByTaskId(reviewsDTO.getTaskId())).thenReturn(USER_ID);
        when(reviewsDao.getTaskStatus(reviewsDTO.getTaskId())).thenReturn("Done");

        when(reviewsDao.getTaskerIdByTaskId(reviewsDTO.getTaskId())).thenReturn(10);
        when(reviewsDao.getAverageRatingForTasker(10)).thenReturn(4.8);
        when(reviewsDao.getReviewById(reviewsDTO.getReviewId())).thenReturn(reviewsDTO); // Return updated for the second call

        ReviewsDTO result = reviewsService.updateReview(reviewsDTO);

        assertNotNull(result);
        verify(reviewsDao).updateReview(reviewsDTO);
        verify(reviewsDao).updateTaskerRating(10, 4.8);
    }

    @Test
    void updateReview_ShouldThrow_WhenReviewNotFound() {
        when(reviewsDao.getReviewById(reviewsDTO.getReviewId())).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewsService.updateReview(reviewsDTO);
        });

        assertEquals("Review not found.", exception.getMessage());
    }

    @Test
    void updateReview_ShouldThrow_WhenImageLimitExceeded() {
        when(reviewsDao.getReviewById(reviewsDTO.getReviewId())).thenReturn(reviewsDTO);
        when(reviewsDao.getUserIdByTaskId(reviewsDTO.getTaskId())).thenReturn(USER_ID);
        when(reviewsDao.getTaskStatus(reviewsDTO.getTaskId())).thenReturn("Done");
        when(reviewsDao.getReviewImageCount(reviewsDTO.getReviewId())).thenReturn(4); // 4 existing

        // Try to add 2 new images (Total 6 > 5)
        List<ReviewImagesDTO> newImages = new ArrayList<>();
        ReviewImagesDTO img1 = new ReviewImagesDTO(); img1.setImgId(0);
        ReviewImagesDTO img2 = new ReviewImagesDTO(); img2.setImgId(0);
        newImages.add(img1);
        newImages.add(img2);
        reviewsDTO.setReviewImages(newImages);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewsService.updateReview(reviewsDTO);
        });

        assertEquals("Cannot have more than 5 images in a review. Delete some images first.", exception.getMessage());
    }

    @Test
    void updateReview_ShouldThrow_WhenRateInvalid() {
        reviewsDTO.setRate(-1.0); // Invalid

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            reviewsService.updateReview(reviewsDTO);
        });

        assertEquals("Rate must be between 0 and 5.", exception.getMessage());
    }

    @Test
    void deleteImage_ShouldCallDao() {
        reviewsService.deleteImage(101);
        verify(reviewsDao, times(1)).deleteImage(101);
    }
}
