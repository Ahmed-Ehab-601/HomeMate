const DEFAULT_API_BASE_URL = "http://localhost:8080";
const baseUrl = (import.meta.env.VITE_API_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, "");
const TASKER_PROFILE_ENDPOINT = `${baseUrl}/api/tasker-profile`;

function buildError(status, body) {
  const error = new Error(body?.message ?? "Tasker profile service request failed.");
  error.status = status;
  error.body = body;
  return error;
}

async function safeJson(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (error) {
    console.error("[taskerProfileApi] Failed to parse JSON", error);
    return null;
  }
}

function getAuthHeaders() {
  const token = localStorage.getItem("homemate_token");
  const headers = {};
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
}

async function request(url, options = {}) {
  const headers = {
    ...getAuthHeaders(),
    ...(options.headers || {}),
  };
  const requestOptions = {
    ...options,
    headers,
  };

  let response;
  try {
    response = await fetch(url, requestOptions);
  } catch (error) {
    throw new Error(
      error?.message ?? "Unable to reach the HomeMate API. Please ensure the backend is running.",
    );
  }

  const data = await safeJson(response);
  if (!response.ok) {
    throw buildError(response.status, data);
  }
  return data;
}

function putJson(url, payload) {
  return request(url, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
}

export function getTaskerProfile() {
  return request(`${TASKER_PROFILE_ENDPOINT}/profile`);
}

export function getTaskerReviews({ page = 1, pageSize = 5 } = {}) {
  return request(`${TASKER_PROFILE_ENDPOINT}/reviews`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ page, pageSize }),
  });
}

export function getAvailableServices() {
  return request(`${TASKER_PROFILE_ENDPOINT}/services`);
}

export function updateTaskerName(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/name`, payload);
}

export function updateTaskerUsername(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/username`, payload);
}

export function updateTaskerEmail(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/email`, payload);
}

export function updateTaskerPhone(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/phone`, payload);
}

export function updateTaskerCity(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/city`, payload);
}

export function updateTaskerHourRate(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/hour-rate`, payload);
}

export function updateTaskerBio(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/bio`, payload);
}

export function updateTaskerAvailability(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/availability`, payload);
}

export function updateTaskerPassword(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/password`, payload);
}

export function updateTaskerImage(payload) {
  return putJson(`${TASKER_PROFILE_ENDPOINT}/image`, payload);
}

export function updateTaskerService(serviceId) {
  const url = `${TASKER_PROFILE_ENDPOINT}/service?serviceId=${encodeURIComponent(serviceId)}`;
  return request(url, { method: "PUT" });
}


