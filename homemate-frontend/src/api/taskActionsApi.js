import { baseUrl, apiFetch } from "../utils/apiClient";

/**
 * Accept a task request
 * @param {number} taskId - ID of the task to accept
 * @returns {Promise<void>}
 * @throws {Object} Error object with status, error, and message properties
 */
export async function acceptTask(taskId) {
    const response = await apiFetch(`${baseUrl}/api/tasker/task/${taskId}/accept`, {
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

        if (response.status === 401) {
            throw {
                status: 401,
                error: data.error || "UNAUTHORIZED",
                message: data.message || "Please log in to continue",
            };
        }

        if (response.status === 403) {
            throw {
                status: 403,
                error: data.error || "FORBIDDEN",
                message: data.message || "You are not authorized to accept this task",
            };
        }

        if (response.status === 400) {
            throw {
                status: 400,
                error: data.error || "INVALID_STATUS",
                message: data.message || "Task cannot be accepted. Please refresh the page.",
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

    return;
}

/**
 * Reject a task request
 * @param {number} taskId - ID of the task to reject
 * @returns {Promise<void>}
 * @throws {Object} Error object with status, error, and message properties
 */
export async function rejectTask(taskId) {
    const response = await apiFetch(`${baseUrl}/api/tasker/task/${taskId}/reject`, {
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

        if (response.status === 401) {
            throw {
                status: 401,
                error: data.error || "UNAUTHORIZED",
                message: data.message || "Please log in to continue",
            };
        }

        if (response.status === 403) {
            throw {
                status: 403,
                error: data.error || "FORBIDDEN",
                message: data.message || "You are not authorized to reject this task",
            };
        }

        if (response.status === 400) {
            throw {
                status: 400,
                error: data.error || "INVALID_STATUS",
                message: data.message || "Task cannot be rejected. Please refresh the page.",
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

    return;
}
