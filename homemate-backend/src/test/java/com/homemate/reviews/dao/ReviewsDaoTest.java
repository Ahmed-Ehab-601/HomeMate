package com.homemate.reviews.dao;

import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.Dao.ReviewsDao;
import com.homemate.reviews.mappers.ReviewImagesDTORowMapper;
import com.homemate.reviews.mappers.ReviewsDTORowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReviewsDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private ReviewsDTORowMapper reviewsDTORowMapper;

    @Mock
    private ReviewImagesDTORowMapper reviewImagesDTORowMapper;

    @InjectMocks
    private ReviewsDao reviewsDao;

    private ReviewsDTO reviewsDTO;

    @BeforeEach
    void setUp() {
        reviewsDTO = new ReviewsDTO();
        reviewsDTO.setReviewId(1);
        reviewsDTO.setTaskId(100);
        reviewsDTO.setText("Great job");
        reviewsDTO.setRate(5.0);
    }

    @Test
    void addReview_ShouldReturnReview_WhenSuccessful() {
        // Mock KeyHolder update
        when(jdbcTemplate.update(any(PreparedStatementCreator.class), any(KeyHolder.class)))
                .thenAnswer(invocation -> {
                    KeyHolder keyHolder = invocation.getArgument(1);
                    java.util.Map<String, Object> keys = new java.util.HashMap<>();
                    keys.put("reviewID", 1);
                    keyHolder.getKeyList().add(keys);
                    return 1;
                });

        // Mock getReviewById called at the end of addReview
        String selectSql = "SELECT * FROM Reviews WHERE reviewID = ?";
        when(jdbcTemplate.query(eq(selectSql), eq(reviewsDTORowMapper), eq(1)))
                .thenReturn(Collections.singletonList(reviewsDTO));

        // Mock image fetch for getReviewById
        lenient().when(jdbcTemplate.query(contains("SELECT * FROM ReviewImage"), eq(reviewImagesDTORowMapper), eq(1)))
                .thenReturn(new ArrayList<>());

        ReviewsDTO result = reviewsDao.addReview(reviewsDTO);

        assertNotNull(result);
        assertEquals(1, result.getReviewId());
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }

    // Test 1: Get Review By Task - Found
    @Test
    void getReviewByTask_ShouldReturnReview_WhenFound() {
        String sql = "SELECT * FROM Reviews WHERE taskID = ?";
        List<ReviewsDTO> list = Collections.singletonList(reviewsDTO);

        when(jdbcTemplate.query(eq(sql), eq(reviewsDTORowMapper), eq(100))).thenReturn(list);
        
        lenient().when(jdbcTemplate.query(contains("SELECT * FROM ReviewImage"), eq(reviewImagesDTORowMapper), eq(1)))
                .thenReturn(new ArrayList<>()); // Mock image query, return empty list

        ReviewsDTO result = reviewsDao.getReviewByTask(100);

        assertNotNull(result);
        assertEquals(1, result.getReviewId());
        verify(jdbcTemplate).query(eq(sql), eq(reviewsDTORowMapper), eq(100));
    }

    // Test 2: Get Review By Task - Not Found
    @Test
    void getReviewByTask_ShouldReturnNull_WhenNotFound() {
        String sql = "SELECT * FROM Reviews WHERE taskID = ?";
        when(jdbcTemplate.query(eq(sql), eq(reviewsDTORowMapper), eq(999))).thenReturn(Collections.emptyList());
        
        ReviewsDTO result = reviewsDao.getReviewByTask(999);
        
        assertNull(result);
    }

    // Test 3: Delete Review - Success
    @Test
    void deleteReview_ShouldReturnTrue_WhenModified() {
        String sql = "DELETE FROM Reviews WHERE reviewID = ?";
        when(jdbcTemplate.update(sql, 1)).thenReturn(1);
        
        boolean result = reviewsDao.deleteReview(1);
        
        assertTrue(result);
        verify(jdbcTemplate).update(sql, 1);
    }

    // Test 4: Delete Review - Fail
    @Test
    void deleteReview_ShouldReturnFalse_WhenNotModified() {
        String sql = "DELETE FROM Reviews WHERE reviewID = ?";
        when(jdbcTemplate.update(sql, 1)).thenReturn(0);
        
        boolean result = reviewsDao.deleteReview(1);
        
        assertFalse(result);
    }
    
    // Test 5: Update Review
    @Test
    void updateReview_ShouldExecuteUpdate() {
        String sql = "UPDATE Reviews SET text = ?, rate = ? WHERE reviewID = ?";
        reviewsDTO.setReviewImages(null); // No images to update
        
        reviewsDao.updateReview(reviewsDTO);
        
        verify(jdbcTemplate).update(sql, reviewsDTO.getText(), reviewsDTO.getRate(), reviewsDTO.getReviewId());
    }

    // Test 6: Get Tasker ID By Task
    @Test
    void getTaskerIdByTaskId_ShouldReturnId() {
        String sql = "SELECT taskerID FROM Task WHERE taskID = ?";
        when(jdbcTemplate.queryForObject(sql, Integer.class, 100)).thenReturn(50);
        
        Integer taskerId = reviewsDao.getTaskerIdByTaskId(100);
        
        assertEquals(50, taskerId);
    }
}
