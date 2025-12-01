// Signup API
import { baseUrl } from "../utils/apiClient";

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
  // Convert image file to base64 if provided
  let profileImage = null;
  if (taskerData.profileImage) {
    if (typeof taskerData.profileImage === "string") {
      // Already base64 or URL
      profileImage = taskerData.profileImage;
    } else if (taskerData.profileImage instanceof File) {
      // Convert File to base64
      profileImage = await new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onloadend = () => {
          const base64String = reader.result.split(",")[1]; // Remove data:image/...;base64, prefix
          resolve(base64String);
        };
        reader.onerror = reject;
        reader.readAsDataURL(taskerData.profileImage);
      });
    }
  }

  const payload = {
    username: taskerData.username,
    email: taskerData.email,
    password: taskerData.password,
    phoneNumber: taskerData.phoneNumber,
    dateOfBirth: taskerData.dateOfBirth, // String format: "YYYY-MM-DD"
    bio: taskerData.bio || "",
    serviceID: taskerData.serviceID ? Number(taskerData.serviceID) : null,
    hourRate: taskerData.hourRate ? Number(taskerData.hourRate) : null,
    firstName: taskerData.firstName,
    lastName: taskerData.lastName,
    city: taskerData.city || "",
  };

  // Add profile image if available (backend expects byte array)
  if (profileImage && typeof profileImage === "string") {
    // Convert base64 to Uint8Array for byte array
    try {
      const binaryString = atob(profileImage);
      const bytes = new Uint8Array(binaryString.length);
      for (let i = 0; i < binaryString.length; i++) {
        bytes[i] = binaryString.charCodeAt(i);
      }
      payload.profileImage = Array.from(bytes);
    } catch (error) {
      console.error("Failed to convert image to byte array:", error);
      // Continue without image if conversion fails
    }
  }

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

  // Backend returns just the JWT token as a string (not JSON)
  const token = await response.text();
  return token.trim(); // Remove any whitespace
}

