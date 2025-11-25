import addresses from "../data/addresses";
import { baseUrl, apiRequest } from "../utils/apiClient";

export async function fetchUserAddresses(userId) {
  // Uncomment once backend is ready:
  // return apiRequest(`${baseUrl}/api/users/${userId}/addresses`, {
  //   method: "GET",
  // });

  // Mock implementation - keeping for backward compatibility
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(addresses.filter((address) => address.userId === userId));
    }, 300);
  });
}

