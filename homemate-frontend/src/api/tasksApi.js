import { baseUrl, apiRequest } from "../utils/apiClient";

const TASK_REQUEST_ENDPOINT = `${baseUrl}/api/user/task/request`;
const USER_TASKS_ENDPOINT = `${baseUrl}/api/user/tasks`;
const TASKER_TASKS_ENDPOINT = `${baseUrl}/api/tasker/tasks`;

export async function requestTask(payload) {
  const body = {
    userID: payload.userId,
    taskerID: payload.taskerId,
    serviceID: payload.serviceId,
    addressID: payload.addressId,
    startDate: payload.startDate,
    description: payload.description ?? "",
  };
  console.log(TASK_REQUEST_ENDPOINT);

  return apiRequest(TASK_REQUEST_ENDPOINT, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

/**
 * Fetch user tasks with pagination and filtering
 * @param {number} userId - User ID
 * @param {string} statusDto - Status filter (All, InReview, Accepted, InProgress, Suspended, Done, Rejected)
 * @param {number} page - Page number (0-indexed)
 * @param {number} pageSize - Number of tasks per page
 * @returns {Promise<Object>} Response with tasks array and pagination metadata
 */
export async function fetchUserTasks(userId, statusDto = "All", page = 0, pageSize = 10) {
  // Page and pageSize are path variables, userID and status are query parameters
  const url = `${USER_TASKS_ENDPOINT}/${page}/${pageSize}?userID=${userId}&statusDto=${statusDto}`;

  console.log("🔵 [fetchUserTasks] Request:", {
    url,
    userId,
    statusDto,
    page,
    pageSize,
  });

  const data = await apiRequest(url, {
    method: "GET",
  });

  console.log("🟢 [fetchUserTasks] Response:", {
    data,
  });

  return data;
}

/**
 * Fetch tasker tasks with pagination and filtering
 * @param {number} taskerId - Tasker ID
 * @param {string} statusDto - Status filter (All, InReview, Accepted, InProgress, Suspended, Done, Rejected)
 * @param {number} page - Page number (0-indexed)
 * @param {number} pageSize - Number of tasks per page
 * @returns {Promise<Object>} Response with tasks array and pagination metadata
 */
export async function fetchTaskerTasks(taskerId, statusDto = "All", page = 0, pageSize = 10) {
  // Page and pageSize are path variables, taskerID and status are query parameters
  const url = `${TASKER_TASKS_ENDPOINT}/${page}/${pageSize}?taskerID=${taskerId}&statusDto=${statusDto}`;

  console.log("🔵 [fetchTaskerTasks] Request:", {
    url,
    taskerId,
    statusDto,
    page,
    pageSize,
  });

  const data = await apiRequest(url, {
    method: "GET",
  });

  console.log("🟢 [fetchTaskerTasks] Response:", {
    data,
  });

  return data;
}
