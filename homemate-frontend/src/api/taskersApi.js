// import taskers from "../data/taskers";
import { normalizeTasker } from "../utils/taskers";
import { baseUrl, apiRequest } from "../utils/apiClient";

const SORT_MAP = {
  "price-asc": { sortBy: "hourRate", sortOrder: "ASC" },
  "price-desc": { sortBy: "hourRate", sortOrder: "DESC" },
  "rating-asc": { sortBy: "rating", sortOrder: "ASC" },
  "rating-desc": { sortBy: "rating", sortOrder: "DESC" },
};

const cleanPayload = (payload) =>
  Object.fromEntries(
    Object.entries(payload).filter(
      ([, value]) =>
        value !== undefined &&
        value !== null &&
        value !== "" &&
        !(typeof value === "number" && Number.isNaN(value)),
    ),
  );

export async function fetchTaskers({
  serviceId,
  page = 1,
  size = 12,
  searchTerm = "",
  filters = {},
  sortOption = "rating-desc",
}) {
  if (!serviceId) {
    throw new Error("serviceId is required to fetch taskers.");
  }

  const sort = SORT_MAP[sortOption] ?? SORT_MAP["rating-desc"];

  const criteria = cleanPayload({
    serviceID: Number(serviceId),
    availability: filters.availability !== "any" ? filters.availability : null,
    search: searchTerm || null,
    minRating: filters.minRating > 0 ? Number(filters.minRating) : null,
    minHourlyRate: filters.minHourlyRate > 0 ? Number(filters.minHourlyRate) : null,
    maxHourlyRate: filters.maxHourlyRate < 500 ? Number(filters.maxHourlyRate) : null,
    city: filters.location || null,
    sortBy: sort.sortBy,
    sortOrder: sort.sortOrder,
  });

  const params = new URLSearchParams({
    // backend expects 1-based page index while frontend uses 0-based
    page: String(Math.max(1, page + 1)),
    size: String(size),
  });

  const body = await apiRequest(`${baseUrl}/api/taskers/search?${params.toString()}`, {
    method: "POST",
    body: JSON.stringify(criteria),
  });
  const list = Array.isArray(body) ? body : [];

  return {
    data: list.map(normalizeTasker),
    page,
    hasMore: list.length === size,
  };
}

export async function fetchTaskerById(taskerId) {
  // TODO: Uncomment when backend is ready
  // const response = await fetch(`${baseUrl}/api/taskers/${taskerId}`);
  // if (!response.ok) throw new Error("Failed to load tasker profile");
  // return response.json();

  // Mock implementation - keeping for backward compatibility
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // This would normally fetch from API
      reject(new Error("Tasker not found - API not implemented"));
    }, 250);
  });
}

/**
 * Fetch tasker profile details
 * @param {number} taskerId - Tasker ID
 * @returns {Promise<Object>} TaskerProfileDTO
 */
export async function fetchTaskerProfile(taskerId) {
  // TODO: Uncomment when backend is ready
  // const response = await fetch(`${baseUrl}/api/taskers/${taskerId}/profile`);
  // if (!response.ok) {
  //   throw {
  //     status: response.status,
  //     error: "FETCH_ERROR",
  //     message: "Failed to load tasker profile",
  //   };
  // }
  // return response.json();

  // Mock implementation - keeping for backward compatibility
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      reject(new Error("Tasker profile not found - API not implemented"));
    }, 250);
  });
}

export async function getTaskerReviews(taskerId, page = 1, pageSize = 5) {
  const params = new URLSearchParams({
    taskerID: Number(taskerId),
    page: page,
    pageSize: pageSize
  });

  const response = await fetch(`${baseUrl}/api/tasker-profile/reviews?${params.toString()}`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      // Add Authorization if available, though endpoint is now public-capable
      ...(localStorage.getItem("homemate_token") ? { Authorization: `Bearer ${localStorage.getItem("homemate_token")}` } : {})
    },
  });

  if (!response.ok) {
    // Handle error or return empty
    console.error("Failed to fetch reviews");
    return { reviews: [], totalReviews: 0, totalPages: 0 };
  }

  return response.json();
}
