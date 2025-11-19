import services from "../data/services";
import { normalizeService } from "../utils/services";

const normalizedServices = services.map(normalizeService);

export async function fetchServices() {
  // Uncomment once backend is ready:
  // const response = await fetch(`${import.meta.env.VITE_API_URL}/services`);
  // if (!response.ok) throw new Error("Failed to load services");
  // return response.json();

  return new Promise((resolve) => {
    setTimeout(() => resolve(normalizedServices), 300);
  });
}

