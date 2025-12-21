// Signup API
import { baseUrl } from "../utils/apiClient";

/**
 * Initialize Google signup - validates Google token and returns user details with verify token
 * Unified endpoint for both user and tasker signup
 * @param {string} idToken - Google ID token
 * @param {string} userType - 'user' or 'tasker' (informational, not used in endpoint)
 * @returns {Promise<{email: string, firstName: string, lastName: string, username: string, verifyToken: string}>}
 */
export async function initGoogleSignup(idToken, userType) {
  const response = await fetch(`${baseUrl}/api/auth/signup/google/init`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      idToken: idToken,
    }),
  });

  if (!response.ok) {
    const error = await response.text().catch(() => "Failed to validate Google token");
    throw new Error(error || "Failed to validate Google token");
  }

  return response.json();
}

/**
 * Sign up a regular user
 * @param {Object} userData - User signup data
 * @returns {Promise<string>} JWT token
 */
export async function signupUser(userData) {
  console.log(userData)
  const response = await fetch(`${baseUrl}/api/user/signup`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      username: userData.username,
      firstName: userData.firstName,
      lastName: userData.lastName,
      email: userData.email,
      password: userData.password,
      birthDate: userData.birthDate, // Should be in format: "YYYY-MM-DDTHH:mm:ss.sssZ" or Timestamp
      gender: userData.gender, // Character: 'M', 'F', etc.
      phone: userData.phone,
      verifyToken: userData.verifyToken || null, // Include verification token if available
    }),
  });

  if (!response.ok) {
    const errorText = await response.text().catch(() => "Signup failed");
    throw new Error(errorText || "Invalid credentials");
  }

  // Backend returns just the JWT token as a string (not JSON)
  const token = await response.text();
  return token.trim(); // Remove any whitespace
}

/**
 * Sign up a tasker
 * @param {Object} taskerData - Tasker signup data
 * @returns {Promise<string>} JWT token
 */
export async function signupTasker(taskerData) {
  const payload = {
    username: taskerData.username,
    email: taskerData.email,
    password: taskerData.password,
    phoneNumber: taskerData.phoneNumber,
    dateOfBirth: taskerData.dateOfBirth,
    bio: taskerData.bio || "",
    serviceID: taskerData.serviceID ? Number(taskerData.serviceID) : null,
    hourRate: taskerData.hourRate ? Number(taskerData.hourRate) : null,
    firstName: taskerData.firstName,
    lastName: taskerData.lastName,
    city: taskerData.city || "",
    profileImage: taskerData.profileImage || null,
    verifyToken: taskerData.verifyToken || null,
  };

  const response = await fetch(`${baseUrl}/api/tasker/signup`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorText = await response.text().catch(() => "Signup failed");
    throw new Error(errorText || "Invalid credentials");
  }

  const token = await response.text();
  return token.trim();
}

