import { baseUrl, apiRequest } from "../utils/apiClient";

const USERS_ENDPOINT = `${baseUrl}/api/users`;

export function getUserProfile(userId) {
  return apiRequest(`${USERS_ENDPOINT}/profile`, {
    method: "GET",
  });
}

export function getUserAddresses(userId) {
  return apiRequest(`${USERS_ENDPOINT}/addresses`, {
    method: "GET",
  });
}

export function addUserAddress(userId, payload) {
  return apiRequest(`${USERS_ENDPOINT}/${userId}/addresses`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function updateUserAddress(userId, addressId, payload) {
  return apiRequest(`${USERS_ENDPOINT}/${userId}/addresses/${addressId}`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}

export function deleteUserAddress(userId, addressId) {
  return apiRequest(`${USERS_ENDPOINT}/${userId}/addresses/${addressId}`, {
    method: "DELETE",
  });
}

export function deleteUserAccount(userId) {
  return apiRequest(`${USERS_ENDPOINT}/${userId}/account`, {
    method: "DELETE",
  });
}

