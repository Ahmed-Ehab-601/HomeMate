import client from '../api/axiosClient';

// backend API uses /api/admin prefix
const ADMIN_PREFIX = '/api/admin';

const buildPaginationParams = (page = 0, size = 10) => ({
  page,
  size,
});

export const getUsers = async ({ page = 0, size = 10, filters = {} } = {}) => {
  const params = {
    ...buildPaginationParams(page, size),
    ...filters,
  };
  const { data } = await client.get(`${ADMIN_PREFIX}/users`, { params });
  return data;
};

export const getTaskers = async ({ page = 0, size = 10, filters = {} } = {}) => {
  const params = {
    ...buildPaginationParams(page, size),
    ...filters,
  };
  const { data } = await client.get(`${ADMIN_PREFIX}/taskers`, { params });
  return data;
};

export const createUser = async (payload) => {
  const { data } = await client.post(`${ADMIN_PREFIX}/users`, payload);
  return data;
};

export const updateUser = async (userId, payload) => {
  const { data } = await client.put(`${ADMIN_PREFIX}/users/${userId}`, payload);
  return data;
};

export const suspendUser = async (userId, suspended = true, reason = '') => {
  // backend expects a SuspendDto: { userId, userType, reason }
  const payload = { userId, userType: 'USER', reason };
  const { data } = await client.patch(`${ADMIN_PREFIX}/user/suspend`, payload);
  return data;
};

export const createTasker = async (payload) => {
  const { data } = await client.post(`${ADMIN_PREFIX}/taskers`, payload);
  return data;
};

export const updateTasker = async (taskerId, payload) => {
  const { data } = await client.put(`${ADMIN_PREFIX}/taskers/${taskerId}`, payload);
  return data;
};

export const suspendTasker = async (taskerId, suspended = true, reason = '') => {
  const payload = { userId: taskerId, userType: 'TASKER', reason };
  const { data } = await client.patch(`${ADMIN_PREFIX}/tasker/suspend`, payload);
  return data;
};

export const promoteUser = async (userId) => {
  const { data } = await client.patch(`${ADMIN_PREFIX}/user/promote/${userId}`);
  return data;
};

export const demoteUser = async (userId) => {
  const { data } = await client.patch(`${ADMIN_PREFIX}/user/demote/${userId}`);
  return data;
};

export const promoteUsers = async (userIds = []) => {
  // promote users in parallel; backend exposes single promote endpoint per user
  const promises = userIds.map((id) => client.patch(`${ADMIN_PREFIX}/user/promote/${id}`));
  const results = await Promise.all(promises);
  return results.map((r) => r.data);
};

export const demoteUsers = async (userIds = []) => {
  const promises = userIds.map((id) => client.patch(`${ADMIN_PREFIX}/user/demote/${id}`));
  const results = await Promise.all(promises);
  return results.map((r) => r.data);
};

export const suspendUsers = async (userIds = [], reason = '') => {
  const promises = userIds.map((id) => client.patch(`${ADMIN_PREFIX}/user/suspend`, { userId: id, userType: 'USER', reason }));
  const results = await Promise.all(promises);
  return results.map((r) => r.data);
};

export const reactiveUsers = async (userIds = []) => {
  const promises = userIds.map((id) => client.patch(`${ADMIN_PREFIX}/user/reactive/${id}`));
  const results = await Promise.all(promises);
  return results.map((r) => r.data);
};

export const reactiveUser = async (userId) => {
  const { data } = await client.patch(`${ADMIN_PREFIX}/user/reactive/${userId}`);
  return data;
};

export const reactiveTasker = async (taskerId) => {
  const { data } = await client.patch(`${ADMIN_PREFIX}/tasker/reactive/${taskerId}`);
  return data;
};

export const suspendTaskers = async (taskerIds = [], reason = '') => {
  const promises = taskerIds.map((id) => client.patch(`${ADMIN_PREFIX}/tasker/suspend`, { userId: id, userType: 'TASKER', reason }));
  const results = await Promise.all(promises);
  return results.map((r) => r.data);
};

export const reactiveTaskers = async (taskerIds = []) => {
  const promises = taskerIds.map((id) => client.patch(`${ADMIN_PREFIX}/tasker/reactive/${id}`));
  const results = await Promise.all(promises);
  return results.map((r) => r.data);
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
};

