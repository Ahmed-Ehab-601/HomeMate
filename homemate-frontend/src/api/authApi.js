// Authentication API
// TODO: Replace with actual backend API calls when ready

const DUMMY_USERS = [
  { username: "demo", password: "password123" },
  { username: "test", password: "test123" },
];

export async function login(username, password) {
  // Uncomment once backend is ready:
  // const response = await fetch(`${import.meta.env.VITE_API_URL}/auth/login`, {
  //   method: "POST",
  //   headers: {
  //     "Content-Type": "application/json",
  //   },
  //   body: JSON.stringify({ username, password }),
  // });
  // if (!response.ok) {
  //   const error = await response.json();
  //   throw new Error(error.message || "Login failed");
  // }
  // return response.json();

  // Dummy implementation for now
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const user = DUMMY_USERS.find(
        (u) => u.username === username && u.password === password
      );
      if (user) {
        resolve({
          token: "dummy_token_" + Date.now(),
          user: {
            username: user.username,
            id: "user_" + user.username,
          },
        });
      } else {
        reject(new Error("Invalid username or password"));
      }
    }, 500);
  });
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

