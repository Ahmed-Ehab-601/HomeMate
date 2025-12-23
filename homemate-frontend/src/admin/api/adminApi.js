import { apiFetch, parseJson } from '../../utils/apiClient';

// backend API uses /api/admin prefix
const ADMIN_PREFIX = '/api/admin';

/**
 * Enhanced apiFetch with admin-specific logging
 */
async function adminApiFetch(url, options = {}) {
    const fullUrl = url.startsWith('http') ? url : `${ADMIN_PREFIX}${url}`;
    const token = localStorage.getItem('homemate_token');
    const user = JSON.parse(localStorage.getItem('homemate_user') || '{}');

    console.log('🔵 [ADMIN] API Request:', {
        url: fullUrl,
        method: options.method || 'GET',
        auth: {
            hasToken: !!token,
            token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
            userRole: user?.role || 'UNKNOWN',
            userId: user?.id || 'UNKNOWN',
        },
        headers: options.headers,
        body: options.body ? JSON.parse(options.body) : undefined,
    });

    try {
        const response = await apiFetch(fullUrl, options);
        const data = await parseJson(response);

        console.log('✅ [ADMIN] API Response:', {
            url: fullUrl,
            status: response.status,
            statusText: response.statusText,
            data,
        });

        if (!response.ok) {
            console.error('❌ [ADMIN] API Error:', {
                url: fullUrl,
                method: options.method || 'GET',
                status: response.status,
                statusText: response.statusText,
                data,
            });

            // Handle 403 Forbidden
            if (response.status === 403) {
                console.error('🚫 [ADMIN] 403 Forbidden - Check:', {
                    tokenExists: !!token,
                    userRole: user?.role,
                    userId: user?.id,
                    endpoint: fullUrl,
                });
            }

            throw {
                status: response.status,
                message: data?.message || data?.error || 'Request failed',
                data,
            };
        }

        return data;
    } catch (error) {
        console.error('❌ [ADMIN] API Exception:', {
            url: fullUrl,
            error: error.message,
            stack: error.stack,
        });
        throw error;
    }
}

const buildQueryString = (params) => {
    const searchParams = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
            searchParams.append(key, value);
        }
    });
    return searchParams.toString();
};

export const getUsers = async ({ page = 0, size = 10, filters = {} } = {}) => {
    const params = { page, size, ...filters };
    const queryString = buildQueryString(params);
    return adminApiFetch(`/users?${queryString}`);
};

export const getTaskers = async ({ page = 0, size = 10, filters = {} } = {}) => {
    const params = { page, size, ...filters };
    const queryString = buildQueryString(params);
    return adminApiFetch(`/taskers?${queryString}`);
};

export const createUser = async (payload) => {
    return adminApiFetch('/users', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
};

export const updateUser = async (userId, payload) => {
    return adminApiFetch(`/users/${userId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    });
};

export const suspendUser = async (userId, suspended = true, reason = '') => {
    const payload = { userId, userType: 'USER', reason };
    return adminApiFetch('/user/suspend', {
        method: 'PATCH',
        body: JSON.stringify(payload),
    });
};

export const createTasker = async (payload) => {
    return adminApiFetch('/taskers', {
        method: 'POST',
        body: JSON.stringify(payload),
    });
};

export const updateTasker = async (taskerId, payload) => {
    return adminApiFetch(`/taskers/${taskerId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
    });
};

export const suspendTasker = async (taskerId, suspended = true, reason = '') => {
    const payload = { userId: taskerId, userType: 'TASKER', reason };
    return adminApiFetch('/tasker/suspend', {
        method: 'PATCH',
        body: JSON.stringify(payload),
    });
};

export const promoteUser = async (userId) => {
    return adminApiFetch(`/user/promote/${userId}`, {
        method: 'PATCH',
    });
};

export const demoteUser = async (userId) => {
    return adminApiFetch(`/user/demote/${userId}`, {
        method: 'PATCH',
    });
};

export const promoteUsers = async (userIds = []) => {
    const promises = userIds.map((id) => promoteUser(id));
    return Promise.all(promises);
};

export const demoteUsers = async (userIds = []) => {
    const promises = userIds.map((id) => demoteUser(id));
    return Promise.all(promises);
};

export const suspendUsers = async (userIds = [], reason = '') => {
    const promises = userIds.map((id) => suspendUser(id, true, reason));
    return Promise.all(promises);
};

export const reactiveUsers = async (userIds = []) => {
    const promises = userIds.map((id) => reactiveUser(id));
    return Promise.all(promises);
};

export const reactiveUser = async (userId) => {
    return adminApiFetch(`/user/reactive/${userId}`, {
        method: 'PATCH',
    });
};

export const reactiveTasker = async (taskerId) => {
    return adminApiFetch(`/tasker/reactive/${taskerId}`, {
        method: 'PATCH',
    });
};

export const suspendTaskers = async (taskerIds = [], reason = '') => {
    const promises = taskerIds.map((id) => suspendTasker(id, true, reason));
    return Promise.all(promises);
};

export const reactiveTaskers = async (taskerIds = []) => {
    const promises = taskerIds.map((id) => reactiveTasker(id));
    return Promise.all(promises);
};

export const getReports = async ({
    pageNumber = 0,
    pageSize = 20,
    header,
    body,
    taskID,
    reporter,
    adminStatus
} = {}) => {
    const params = { pageNumber, pageSize };

    // Only include filter params if they have values
    if (header) params.header = header;
    if (body) params.body = body;
    if (taskID) params.taskID = taskID;
    if (reporter !== undefined && reporter !== null) params.reporter = reporter;
    if (adminStatus) params.adminStatus = adminStatus;

    const queryString = buildQueryString(params);
    const response = await apiFetch(`/api/reports/short-reports?${queryString}`);
    const data = await parseJson(response);

    if (!response.ok) {
        throw {
            status: response.status,
            message: data?.message || data?.error || 'Failed to fetch reports',
            data,
        };
    }

    return data;
};

export const respondToReport = async (reportId, message) => {
    return apiFetch(`/api/reports/${reportId}/response`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message }),
    });
};

export default {
    getUsers,
    getTaskers,
    createUser,
    updateUser,
    suspendUser,
    promoteUser,
    demoteUser,
    reactiveUser,
    createTasker,
    updateTasker,
    suspendTasker,
    reactiveTasker,
    suspendTaskers,
    reactiveTaskers,
    demoteUsers,
    promoteUsers,
    suspendUsers,
    reactiveUsers,
    getReports,
    respondToReport,
};
