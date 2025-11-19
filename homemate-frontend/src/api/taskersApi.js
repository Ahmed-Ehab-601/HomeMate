import taskers from "../data/taskers";

const PAGE_SIZE = 20;

const rateBuckets = {
  any: () => true,
  low: (value) => value < 35,
  medium: (value) => value >= 35 && value <= 55,
  premium: (value) => value > 55,
};

export async function fetchTaskers({
  serviceSlug,
  page = 1,
  searchTerm = "",
  filters = {},
  sortOption = "rating-desc",
}) {
  // Uncomment when backend endpoint is available.
  // const searchParams = new URLSearchParams({
  //   page: String(page),
  //   pageSize: String(PAGE_SIZE),
  //   search: searchTerm,
  //   rate: filters.rate ?? "",
  //   availability: filters.availability ?? "",
  //   rating: filters.rating ?? "",
  //   location: filters.location ?? "",
  //   sort: sortOption,
  // });
  // const response = await fetch(
  //   `${import.meta.env.VITE_API_URL}/services/${serviceSlug}/taskers?${searchParams}`,
  // );
  // if (!response.ok) throw new Error("Failed to load taskers");
  // return response.json();

  return new Promise((resolve) => {
    setTimeout(() => {
      let pool = taskers.filter((t) => t.serviceSlug === serviceSlug);

      if (searchTerm) {
        const query = searchTerm.toLowerCase();
        pool = pool.filter((t) => t.name.toLowerCase().includes(query));
      }

      if (filters.rate && rateBuckets[filters.rate]) {
        pool = pool.filter((t) => rateBuckets[filters.rate](t.hourRate));
      }

      if (filters.availability && filters.availability !== "any") {
        pool = pool.filter((t) => t.availabilityTag === filters.availability);
      }

      if (filters.rating && filters.rating !== "any") {
        pool = pool.filter((t) => t.rating >= Number(filters.rating));
      }

      if (filters.location) {
        const locationQuery = filters.location.toLowerCase();
        pool = pool.filter((t) => t.location.toLowerCase().includes(locationQuery));
      }

      switch (sortOption) {
        case "price-asc":
          pool = [...pool].sort((a, b) => a.hourRate - b.hourRate);
          break;
        case "price-desc":
          pool = [...pool].sort((a, b) => b.hourRate - a.hourRate);
          break;
        case "rating-asc":
          pool = [...pool].sort((a, b) => a.rating - b.rating);
          break;
        default:
          pool = [...pool].sort((a, b) => b.rating - a.rating);
      }

      const start = (page - 1) * PAGE_SIZE;
      const end = start + PAGE_SIZE;
      const data = pool.slice(start, end);
      const hasMore = end < pool.length;

      resolve({
        data,
        page,
        hasMore,
        total: pool.length,
      });
    }, 400);
  });
}

export async function fetchTaskerById(taskerId) {
  // const response = await fetch(`${import.meta.env.VITE_API_URL}/taskers/${taskerId}`);
  // if (!response.ok) throw new Error("Failed to load tasker profile");
  // return response.json();

  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const tasker = taskers.find((t) => t.id === taskerId);
      if (!tasker) {
        reject(new Error("Tasker not found"));
        return;
      }
      resolve(tasker);
    }, 250);
  });
}

