import { baseUrl, apiFetch } from "../utils/apiClient";

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

export default {
  createStripeCustomer,
  createStripeConnectedAccount,
};
