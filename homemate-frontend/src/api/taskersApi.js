import taskers from "../data/taskers";
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
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const tasker = taskers.find((t) => String(t.id) === String(taskerId));
      if (!tasker) {
        reject(new Error("Tasker not found"));
        return;
      }
      resolve(tasker);
    }, 250);
  });
}

