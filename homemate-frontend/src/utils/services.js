export const getServiceSlug = (serviceName = "") =>
  serviceName
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");

export const normalizeService = (service) => ({
  ...service,
  slug: getServiceSlug(service.serviceName),
});

