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
