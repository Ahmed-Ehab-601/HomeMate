package com.homemate.reviews.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.homemate.reviews.DTO.ReviewsDTO;
import com.homemate.reviews.service.ReviewsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
@Validated
public class ReviewsController {

	private final ReviewsService reviewsService;

	@Autowired
	public ReviewsController(ReviewsService reviewsService) {
		this.reviewsService = reviewsService;
	}

	// Add a new review. Returns the created ReviewsDTO or 400 on validation errors.
	@PostMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	public ReviewsDTO addReview(@Valid @RequestBody ReviewsDTO dto) {
		try {
			return reviewsService.addReview(dto);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		}
	}

	// Get review by task id. Returns 404 if not found.
	@GetMapping("/task/{taskId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ReviewsDTO getReviewByTask(@PathVariable("taskId") int taskId) {
		ReviewsDTO review = reviewsService.getReviewByTask(taskId);
		if (review == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found for task: " + taskId);
		}
		return review;
	}

	// Delete a review. Returns true if deleted, false otherwise.
	@DeleteMapping("/{reviewId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public boolean deteleReview(@PathVariable("reviewId") int reviewId) {
		try {
			return reviewsService.deleteReview(reviewId);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete review", e);
		}
	}

	// Update a review. Returns the updated ReviewsDTO or 400 on validation errors.
	@PutMapping
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<ReviewsDTO> updateReview(@Valid @RequestBody ReviewsDTO reviewsDTO) {
		try {
			ReviewsDTO updated = reviewsService.updateReview(reviewsDTO);
			return ResponseEntity.ok(updated);
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update review", e);
		}
	}

	// Delete an image belonging to a review.
	@DeleteMapping("/image/{imageId}")
	@PreAuthorize("hasRole('ROLE_USER')")
	public ResponseEntity<Void> deleteImage(@PathVariable("imageId") int imageId) {
		try {
			reviewsService.deleteImage(imageId);
			return ResponseEntity.noContent().build();
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image", e);
		}
	}
}

