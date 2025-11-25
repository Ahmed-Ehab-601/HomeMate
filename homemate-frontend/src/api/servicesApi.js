// api/servicesApi.js
import { normalizeService } from "../utils/services";

const DEFAULT_API_BASE_URL = "http://localhost:8080";
const baseUrl = (import.meta.env.VITE_API_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, "");

export async function fetchServices() {
  const response = await fetch(`${baseUrl}/api/services`);
  if (!response.ok) {
    throw new Error("Failed to load services");
  }
  const services = await response.json();
  return Array.isArray(services) ? services.map(normalizeService) : [];
}
