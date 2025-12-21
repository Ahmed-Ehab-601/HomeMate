/**
 * Centralized API client that automatically adds JWT token to all requests
 */

// Use backend URL directly or env variable for production
export const baseUrl = (import.meta.env.VITE_API_URL).replace(/\/$/, "");
//export const baseUrl = "https://homemate-homemate.up.railway.app"
/**
 * Get authentication headers with JWT token
 * @returns {Object} Headers object with Authorization header if token exists
 */
function getAuthHeaders() {
  const token = localStorage.getItem("homemate_token");
  const headers = {};

  if (token) {
    headers["Authorization"] = `Bearer ${token}`;
  }

  return headers;
}

/**
 * Wrapper around fetch that automatically adds JWT token to headers
 * @param {string} url - The URL to fetch
 * @param {Object} options - Fetch options (method, headers, body, etc.)
 * @param {boolean} requireAuth - Whether to require authentication (default: true)
 * @returns {Promise<Response>} Fetch response
 */
export async function apiFetch(url, options = {}, requireAuth = true) {
  // Get auth token and add to headers only if auth is required
  const authHeaders = requireAuth ? getAuthHeaders() : {};
  const headers = {
    "Content-Type": "application/json",
    ...authHeaders,
    ...(options.headers || {}),
  };

  const requestOptions = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, requestOptions);

    // Handle 401 Unauthorized - redirect to signin
    if (response.status === 401 && requireAuth) {
      // Clear auth data
      localStorage.removeItem("homemate_user");
      localStorage.removeItem("homemate_token");
      // Redirect to signin
      if (window.location.pathname !== "/signin" && window.location.pathname !== "/signup") {
        window.location.href = "/signin";
      }
      // Still return response so caller can handle it
    }

    return response;
  } catch (error) {
    throw {
      status: 0,
      error: "NETWORK_ERROR",
      message: error?.message ?? "Unable to reach the API. Please ensure the backend is running.",
    };
  }
}

/**
 * Parse JSON response safely
 * @param {Response} response - Fetch response object
 * @returns {Promise<Object|null>} Parsed JSON or null
 */
export async function parseJson(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (error) {
    console.error("Failed to parse response JSON", error);
    return null;
  }
}

/**
 * Make an API request with automatic token injection and JSON parsing
 * @param {string} url - The URL to fetch
 * @param {Object} options - Fetch options
 * @param {boolean} requireAuth - Whether to require authentication (default: true)
 * @returns {Promise<Object>} Parsed JSON response
 */
export async function apiRequest(url, options = {}, requireAuth = true) {
  const response = await apiFetch(url, options, requireAuth);
  const data = await parseJson(response);

  if (!response.ok) {
    throw {
      status: response.status,
      ...(data ?? { error: "UNKNOWN_ERROR", message: "Request failed." }),
    };
  }

  return data;
}

