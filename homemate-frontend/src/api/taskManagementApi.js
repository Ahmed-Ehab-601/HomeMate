
import { baseUrl, apiFetch } from "../utils/apiClient";

/**
 * Get task details by ID
 * @param {number} taskId - ID of the task
 * @returns {Promise<Object>} Task details
 */
export async function getTaskDetails(taskId) {
    const response = await apiFetch(`${baseUrl}/api/task/${taskId}/taskDetails`, {
        method: "GET",
    }).catch((error) => {
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));

        if (response.status === 404) {
            throw {
                status: 404,
                error: data.error || "TASK_NOT_FOUND",
                message: data.message || "Task not found or you don't have permission to access it",
            };
        }

        if (response.status === 403) {
            throw {
                status: 403,
                error: data.error || "FORBIDDEN",
                message: data.message || "You don't have permission to view this task",
            };
        }

        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: data.message || "Server error. Please try again in a few moments.",
        };
    }

    return response.json();
}

/**
 * Get task review by task ID
 * @param {number} taskId - ID of the task
 * @returns {Promise<Object>} Review details
 */
export async function getTaskReview(taskId) {
    const response = await apiFetch(`${baseUrl}/api/task/${taskId}/taskReview`, {
        method: "GET",
    }).catch((error) => {
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));

        if (response.status === 404) {
            // No review found is not an error
            return null;
        }

        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: data.message || "Failed to load review",
        };
    }

    return response.json();
}

/**
 * Reschedule a task
 * @param {number} taskId - ID of the task
 * @param {string} newStartDate - New start date in ISO format
 * @returns {Promise<Object>} Reschedule response
 */
export async function rescheduleTask(taskId, newStartDate) {
    const response = await apiFetch(`${baseUrl}/api/task/${taskId}/reschedule`, {
        method: "PATCH",
        body: JSON.stringify({ newStartDate }),
    }).catch((error) => {
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));

        if (response.status === 400) {
            throw {
                status: 400,
                error: data.error || "INVALID_DATE",
                message: data.message || "Invalid date selection",
            };
        }

        if (response.status === 403) {
            throw {
                status: 403,
                error: data.error || "INVALID_STATUS",
                message: data.message || "Task cannot be rescheduled in current status",
            };
        }

        if (response.status === 404) {
            throw {
                status: 404,
                error: data.error || "TASK_NOT_FOUND",
                message: data.message || "Task not found",
            };
        }

        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: data.message || "Server error. Please try again in a few moments.",
        };
    }

    return response.json();
}

/**
 * Start a task (Accepted/Suspended → In Progress)
 * @param {number} taskId - ID of the task
 * @returns {Promise<Object>} Updated task
 */
export async function startTask(taskId) {
    const url = `${baseUrl}/api/tasker/task/${taskId}/start`;
    console.log("Starting task with URL:", url);
    const response = await apiFetch(url, {
        method: "PATCH",
    }).catch((error) => {
        console.error("Start task network error:", error);
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));

        if (response.status === 403) {
            throw {
                status: 403,
                error: data.error || "FORBIDDEN",
                message: data.message || "You are not authorized to modify this task",
            };
        }

        if (response.status === 400) {
            throw {
                status: 400,
                error: data.error || "INVALID_STATUS",
                message: data.message || "Task cannot be started from current status",
            };
        }

        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: data.message || "Server error. Please try again in a few moments.",
        };
    }

    return response.json();
}

/**
 * Suspend a task (In Progress → Suspended)
 * @param {number} taskId - ID of the task
 * @returns {Promise<Object>} Updated task
 */
export async function suspendTask(taskId) {
    const response = await apiFetch(`${baseUrl}/api/tasker/task/${taskId}/suspend`, {
        method: "PATCH",
    }).catch((error) => {
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));

        if (response.status === 403) {
            throw {
                status: 403,
                error: data.error || "FORBIDDEN",
                message: data.message || "You are not authorized to modify this task",
            };
        }

        if (response.status === 400) {
            throw {
                status: 400,
                error: data.error || "INVALID_STATUS",
                message: data.message || "Only tasks in 'InProgress' status can be suspended",
            };
        }

        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: data.message || "Server error. Please try again in a few moments.",
        };
    }

    return response.json();
}

/**
 * Complete a task (In Progress/Suspended → Done)
 * @param {number} taskId - ID of the task
 * @returns {Promise<Object>} Updated task with bill
 */
export async function completeTask(taskId) {
    const response = await apiFetch(`${baseUrl}/api/tasker/task/${taskId}/complete`, {
        method: "PATCH",
    }).catch((error) => {
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));

        if (response.status === 403) {
            throw {
                status: 403,
                error: data.error || "FORBIDDEN",
                message: data.message || "You are not authorized to modify this task",
            };
        }

        if (response.status === 400) {
            throw {
                status: 400,
                error: data.error || "INVALID_STATUS",
                message: data.message || "Task cannot be completed from current status",
            };
        }

        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: data.message || "Server error. Please try again in a few moments.",
        };
    }

    return response.json();
}

/**
 * Get tasker busy time for a specific day
 * @param {number} taskerId - ID of the tasker
 * @param {string} day - Date in YYYY-MM-DD format
 * @param {string} userRole - User role ("ROLE_TASKER" or "ROLE_USER")
 * @returns {Promise<Array>} Array of TaskTimeDto objects: [{taskID, startDate, estimation}, ...]
 */
export async function getTaskerBusyTime(taskerId, day, userRole) {
    // Choose endpoint based on user role
    const endpoint = userRole === "ROLE_TASKER" 
        ? `${baseUrl}/api/task/get-tasker-tasks-tasker-only/${taskerId}?day=${day}`
        : `${baseUrl}/api/task/get-tasker-tasks/${taskerId}?day=${day}`;
    
    const response = await apiFetch(endpoint, {
        method: "GET",
    }).catch((error) => {
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        
        // Check if error indicates tasker is unavailable
        const errorMessage = data.message || data.error || "";
        if (response.status === 503 || 
            data.error === "TASKER_UNAVAILABLE" || 
            errorMessage.toLowerCase().includes("unavailable")) {
            throw {
                status: response.status === 503 ? 503 : response.status,
                error: "TASKER_UNAVAILABLE",
                message: errorMessage || "This Tasker is UNAVAILABLE Now",
            };
        }
        
        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: errorMessage || "Failed to load tasker busy time",
        };
    }

    const data = await response.json();
    
    // Backend now returns List<TaskTimeDto>
    // If empty response, return empty array
    if (!data || !Array.isArray(data) || data.length === 0) {
        return [];
    }
    
    return data;
}

/**
 * Add estimation time to a task
 * @param {number} taskId - ID of the task
 * @param {number} estimation - Estimation in MINUTES (not hours)
 * @returns {Promise<void>}
 */
export async function addTaskEstimation(taskId, estimation) {
    const response = await apiFetch(`${baseUrl}/api/task/add-estimation/${taskId}?estimation=${estimation}`, {
        method: "POST",
    }).catch((error) => {
        throw {
            status: 0,
            error: "NETWORK_ERROR",
            message: "Network error. Please check your connection.",
        };
    });

    if (!response.ok) {
        const data = await response.json().catch(() => ({}));
        throw {
            status: response.status,
            error: data.error || "SERVER_ERROR",
            message: data.message || "Failed to add estimation",
        };
    }

    // No content expected, just return success
    return;
}


