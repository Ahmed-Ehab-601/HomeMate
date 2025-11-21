import client from '../api/axiosClient';

const ADMIN_PREFIX = '/admin';

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

export const suspendUser = async (userId, suspended = true) => {
  const { data } = await client.patch(`${ADMIN_PREFIX}/users/${userId}/suspend`, { suspended });
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

export const suspendTasker = async (taskerId, suspended = true) => {
  const { data } = await client.patch(`${ADMIN_PREFIX}/taskers/${taskerId}/suspend`, { suspended });
  return data;
};

export default {
  getUsers,
  getTaskers,
  createUser,
  updateUser,
  suspendUser,
  createTasker,
  updateTasker,
  suspendTasker,
};

