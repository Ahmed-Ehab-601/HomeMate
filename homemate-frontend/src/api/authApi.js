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

