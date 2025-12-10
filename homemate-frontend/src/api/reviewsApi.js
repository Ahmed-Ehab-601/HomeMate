import { baseUrl, apiRequest } from "../utils/apiClient";

const REVIEWS_ENDPOINT = `${baseUrl}/api/reviews`;

/**
 * Submit a new review
 * @param {Object} reviewData - The review data to submit
 * @returns {Promise<Object>} The created review
 */
export async function submitReview(reviewData) {
    return apiRequest(REVIEWS_ENDPOINT, {
        method: "POST",
        body: JSON.stringify(reviewData),
    });
}

/**
 * Get review by task ID
 * @param {number} taskId - The task ID
 * @returns {Promise<Object>} The review object or throws 404
 */
export async function getReviewByTask(taskId) {
    return apiRequest(`${REVIEWS_ENDPOINT}/task/${taskId}`, {
        method: "GET",
    });
}
/**
 * Update an existing review
 * @param {Object} reviewData - The review data to update
 * @returns {Promise<Object>} The updated review
 */
export async function updateReview(reviewData) {
    return apiRequest(REVIEWS_ENDPOINT, {
        method: "PUT",
        body: JSON.stringify(reviewData),
    });
}

/**
 * Delete a review
 * @param {number} reviewId - The review ID
 * @returns {Promise<boolean>} True if deleted
 */
export async function deleteReview(reviewId) {
    return apiRequest(`${REVIEWS_ENDPOINT}/${reviewId}`, {
        method: "DELETE",
    });
}

/**
 * Delete a review image
 * @param {number} imageId - The image ID
 * @returns {Promise<void>}
 */
export async function deleteReviewImage(imageId) {
    return apiRequest(`${REVIEWS_ENDPOINT}/image/${imageId}`, {
        method: "DELETE",
    });
}
