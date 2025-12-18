// Authentication API
// Note: Login requests should NOT include the Authorization header since we're getting the token

import { baseUrl } from "../utils/apiClient";

export async function login(email, password) {
  const response = await fetch(`${baseUrl}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "Login failed" }));
    throw new Error(error.message || "Invalid email or password");
  }

  return response.json();
}

export async function loginWithGoogle(idToken) {
  const response = await fetch(`${baseUrl}/api/auth/google`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      idToken,
    }),
  });
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message || "Google login failed");
  }
  return response.json();
}

export const setUserOffline = async (userId) => {
  const token = localStorage.getItem("homemate_token");
  const response = await fetch(`${baseUrl}/api/chat/user/${userId}/offline`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (!response.ok) {
    throw new Error("Failed to set user offline");
  }
  // Backend returns plain text "changed", not JSON
  return response.text();
};

export const setTaskerOffline = async (taskerId) => {
  const token = localStorage.getItem("homemate_token");
  const response = await fetch(`${baseUrl}/api/chat/tasker/${taskerId}/offline`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (!response.ok) {
    throw new Error("Failed to set tasker offline");
  }
  // Backend returns plain text "changed", not JSON
  return response.text();
};

// Forgot Password API
export async function sendOtpForPasswordReset(email) {
  const response = await fetch(`${baseUrl}/api/auth/otp/send`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      recipientEmail: email,
      emailType: "FORGOT_PASSWORD",
    }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "Failed to send OTP" }));
    throw new Error(error.message || "Failed to send OTP");
  }

  return response.json();
}

export async function verifyOtpForPasswordReset(email, code) {
  const response = await fetch(`${baseUrl}/api/auth/otp/verify`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      recipientEmail: email,
      code: code,
      emailType: "FORGOT_PASSWORD",
    }),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: "Invalid OTP" }));
    throw new Error(error.message || "Invalid OTP");
  }

  return response.json();
}

export async function resetPassword(verifyToken, newPassword) {
  const response = await fetch(`${baseUrl}/api/auth/reset-password`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      verifyToken: verifyToken,
      newPassword: newPassword,
    }),
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(errorText || "Failed to reset password");
  }

  return response.text();
}