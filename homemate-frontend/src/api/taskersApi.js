// import taskers from "../data/taskers";
import { normalizeTasker } from "../utils/taskers";

const DEFAULT_API_BASE_URL = "http://localhost:8080";
const baseUrl = (import.meta.env.VITE_API_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, "");

const RATE_BUCKETS = {
  low: { minHourRate: 0, maxHourRate: 35 },
  medium: { minHourRate: 35, maxHourRate: 55 },
  premium: { minHourRate: 55, maxHourRate: null },
};

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
  page = 0,
  size = 12,
  searchTerm = "",
  filters = {},
  sortOption = "rating-desc",
}) {
  if (!serviceId) {
    throw new Error("serviceId is required to fetch taskers.");
  }

  const rateRange = filters.rate && RATE_BUCKETS[filters.rate] ? RATE_BUCKETS[filters.rate] : {};
  const sort = SORT_MAP[sortOption] ?? SORT_MAP["rating-desc"];

  const criteria = cleanPayload({
    serviceID: Number(serviceId),
    availability: filters.availability !== "any" ? filters.availability : null,
    search: searchTerm || null,
    minRating: filters.rating && filters.rating !== "any" ? Number(filters.rating) : null,
    minHourRate: rateRange.minHourRate ?? null,
    maxHourRate: rateRange.maxHourRate ?? null,
    city: filters.location || null,
    sortBy: sort.sortBy,
    sortOrder: sort.sortOrder,
  });

  const backendPage = Math.max(1, page + 1);
  const params = new URLSearchParams({
    page: String(backendPage),
    size: String(size),
  });

  const response = await fetch(`${baseUrl}/api/taskers/search?${params.toString()}`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(criteria),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => null);
    throw error ?? new Error("Failed to load taskers.");
  }

  const body = await response.json();
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

/**
 * Fetch paginated reviews for a tasker
 * @param {number} taskerId - Tasker ID
 * @param {number} page - Page number (0-indexed)
 * @param {number} pageSize - Number of reviews per page
 * @returns {Promise<Object>} Response with reviews array and pagination metadata
 */
export async function fetchTaskerReviews(taskerId, page = 0, pageSize = 5) {
  // TODO: Uncomment when backend is ready
  // const url = `${baseUrl}/api/taskers/${taskerId}/reviews/${page}/${pageSize}`;
  // const response = await fetch(url, {
  //   method: "GET",
  //   headers: {
  //     "Content-Type": "application/json",
  //   },
  // });
  //
  // if (!response.ok) {
  //   throw {
  //     status: response.status,
  //     error: "FETCH_ERROR",
  //     message: "Failed to load reviews",
  //   };
  // }
  //
  // return response.json();

  return new Promise((resolve) => {
    setTimeout(() => {
      // Mock implementation - will be replaced by actual API call
      resolve({
        reviews: [],
        totalCount: 0,
        currentPage: page,
        totalPages: 0,
        pageSize: pageSize,
      });
    }, 200);
  });
}
