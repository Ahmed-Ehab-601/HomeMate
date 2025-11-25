const DEFAULT_API_BASE_URL = "http://localhost:8080";
const baseUrl = (import.meta.env.VITE_API_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, "");
const TASK_REQUEST_ENDPOINT = `${baseUrl}/api/user/task/request`;
const USER_TASKS_ENDPOINT = `${baseUrl}/api/user/tasks`;
const TASKER_TASKS_ENDPOINT = `${baseUrl}/api/tasker/tasks`;

const parseJson = async (response) => {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (error) {
    console.error("Failed to parse task request response JSON", error);
    return null;
  }
};

export async function requestTask(payload) {
  const body = {
    userID: payload.userId,
    taskerID: payload.taskerId,
    serviceID: payload.serviceId,
    addressID: payload.addressId,
    startDate: payload.startDate,
    description: payload.description ?? "",
  };
  console.log(TASK_REQUEST_ENDPOINT)

  const response = await fetch(TASK_REQUEST_ENDPOINT, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  }).catch((error) => {
    throw {
      status: 0,
      error: "NETWORK_ERROR",
      message: error?.message ?? "Unable to reach the task service.",
    };
  });

  const data = await parseJson(response);

  if (!response.ok) {
    throw {
      status: response.status,
      ...(data ?? { error: "UNKNOWN_ERROR", message: "Failed to create task request." }),
    };
  }

  return data;
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

  const response = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  }).catch((error) => {
    throw {
      status: 0,
      error: "NETWORK_ERROR",
      message: error?.message ?? "Unable to reach the task service.",
    };
  });

  const data = await parseJson(response);

  console.log("🟢 [fetchUserTasks] Response:", {
    status: response.status,
    ok: response.ok,
    data,
  });

  if (!response.ok) {
    throw {
      status: response.status,
      ...(data ?? { error: "UNKNOWN_ERROR", message: "Failed to fetch tasks." }),
    };
  }

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

  const response = await fetch(url, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
    },
  }).catch((error) => {
    throw {
      status: 0,
      error: "NETWORK_ERROR",
      message: error?.message ?? "Unable to reach the task service.",
    };
  });

  const data = await parseJson(response);

  console.log("🟢 [fetchTaskerTasks] Response:", {
    status: response.status,
    ok: response.ok,
    data,
  });

  if (!response.ok) {
    throw {
      status: response.status,
      ...(data ?? { error: "UNKNOWN_ERROR", message: "Failed to fetch tasks." }),
    };
  }

  return data;
}
