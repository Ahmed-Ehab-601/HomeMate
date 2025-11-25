const DEFAULT_API_BASE_URL = "http://localhost:8080";
const baseUrl = (import.meta.env.VITE_API_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, "");
const USERS_ENDPOINT = `${baseUrl}/api/users`;

const buildError = (status, body) => {
  const error = new Error(body?.message ?? "HomeMate profile service request failed.");
  error.status = status;
  error.body = body;
  return error;
};

const safeJson = async (response) => {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (error) {
    console.error("[userProfileApi] Failed to parse JSON", error);
    return null;
  }
};

function getAuthHeaders() {
  const token = localStorage.getItem("homemate_token");
  const headers = {};
  
  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }
  
  return headers;
}

async function request(url, options = {}) {
  // Get auth token and add to headers
  const authHeaders = getAuthHeaders();
  const headers = {
    ...authHeaders,
    ...(options.headers || {}),
  };

  const requestOptions = {
    ...options,
    headers,
  };
  console.log(requestOptions);
  console.log(url);
  let response;
  try {
    response = await fetch(url, requestOptions);
  } catch (error) {
    throw new Error(
      error?.message ?? "Unable to reach the HomeMate API. Please ensure the backend is running.",
    );
  }

  console.log(response);
  const data = await safeJson(response);

  if (!response.ok) {
    throw buildError(response.status, data);
  }

  return data;
}

export function getUserProfile() {
  return request(`${USERS_ENDPOINT}/profile`);
}

export function getUserAddresses() {
  return request(`${USERS_ENDPOINT}/addresses`);
}

export function addUserAddress(payload) {
  return request(`${USERS_ENDPOINT}/addresses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export function updateUserAddress(addressId, payload) {
  return request(`${USERS_ENDPOINT}/addresses/${addressId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export function deleteUserAddress(addressId) {
  return request(`${USERS_ENDPOINT}/addresses/${addressId}`, {
    method: "DELETE",
  });
}

export function deleteUserAccount() {
  return request(`${USERS_ENDPOINT}/account`, {
    method: "DELETE",
  });
}

