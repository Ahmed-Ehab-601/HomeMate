// api/servicesApi.js
import { normalizeService } from "../utils/services";
import { baseUrl, apiRequest } from "../utils/apiClient";

export async function fetchServices() {
  // Services endpoint doesn't require authentication
  const services = await apiRequest(`${baseUrl}/api/services`, {
    method: "GET",
  }, false);
  return Array.isArray(services) ? services.map(normalizeService) : [];
}
