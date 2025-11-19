// Authentication API

const DEFAULT_API_BASE_URL = "http://localhost:8080";
const baseUrl = (import.meta.env.VITE_API_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, "");

export async function login(email, password) {
  const response = await fetch(`${baseUrl}/api/login`, {
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

export async function loginWithGoogle() {
  // Uncomment once backend is ready:
  // const response = await fetch(`${import.meta.env.VITE_API_URL}/auth/google`, {
  //   method: "POST",
  //   headers: {
  //     "Content-Type": "application/json",
  //   },
  // });
  // if (!response.ok) {
  //   const error = await response.json();
  //   throw new Error(error.message || "Google login failed");
  // }
  // return response.json();

  // Dummy implementation for now
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        token: "dummy_google_token_" + Date.now(),
        user: {
          username: "google_user",
          id: "google_user_123",
        },
      });
    }, 500);
  });
}

