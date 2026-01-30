import { baseUrl, apiFetch, apiRequest } from "../utils/apiClient";

const PAYMENT_ENDPOINT = `${baseUrl}/api/stripe/signup`;

async function _postAndParse(url) {
  const response = await apiFetch(url, { method: "POST" });
  if (!response.ok) {
    const text = await response.text();
    let parsed = null;
    try {
      parsed = JSON.parse(text);
    } catch (e) {
      // not JSON
    }
    const err = new Error((parsed && parsed.message) || text || "Request failed");
    err.status = response.status;
    throw err;
  }

  const body = await response.text();
  if (!body) return null;
  try {
    return JSON.parse(body);
  } catch (e) {
    // return raw text (useful when backend returns plain URL)
    return body;
  }
}

export function createStripeCustomer(userId) {
  if (!userId) return Promise.reject(new Error("Missing userId"));
  return _postAndParse(`${PAYMENT_ENDPOINT}/user/${userId}`);
}

export function createStripeConnectedAccount(taskerId) {
  if (!taskerId) return Promise.reject(new Error("Missing taskerId"));
  return _postAndParse(`${PAYMENT_ENDPOINT}/tasker/${taskerId}`);
}

export async function checkTaskerStripeStatus() {
  // Endpoint returns boolean indicating whether the tasker's Stripe account is enabled
  const url = `${PAYMENT_ENDPOINT}/tasker/status`;
  // apiRequest will parse JSON and throw on non-OK responses
  const data = await apiRequest(url, { method: "GET" });
  // Expecting a boolean in the response body (true/false)
  if (typeof data === "boolean") return data;
  // If backend returned { enabled: true } or similar, attempt to find a boolean
  if (data && typeof data.enabled === "boolean") return data.enabled;
  if (data && typeof data.status === "boolean") return data.status;
  // Fallback: treat truthy responses as enabled
  return !!data;
}

export default {
  createStripeCustomer,
  createStripeConnectedAccount,
};
