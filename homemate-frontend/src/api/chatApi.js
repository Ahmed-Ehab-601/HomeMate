// api/chatApi.js
import { baseUrl, apiFetch } from "../utils/apiClient";

/**
 * Calculate and check if user has unread messages
 * @param {number} id - User ID or Tasker ID
 * @param {string} role - User role (ROLE_USER or ROLE_TASKER)
 * @returns {Promise<boolean>} True if has unread messages
 */
export async function calculateUnreadMessages(id, role) {
  const endpoint = role === "ROLE_TASKER"
    ? `${baseUrl}/api/message/tasker/${id}/calculate-unread`
    : `${baseUrl}/api/message/user/${id}/calculate-unread`;

  const response = await apiFetch(endpoint, {
    method: "PUT",
  });

  if (!response.ok) {
    throw new Error("Failed to calculate unread messages");
  }

  return response.json(); // Returns boolean
}

