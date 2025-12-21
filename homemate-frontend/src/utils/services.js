export const getServiceSlug = (serviceName = "") =>
  serviceName
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "");

export const normalizeService = (service = {}) => {
  const serviceId =
    service.serviceId ??
    service.serviceid ??
    service.id ??
    service.serviceID ??
    null;
  const serviceName =
    service.serviceName ??
    service.servicename ??
    service.name ??
    "Service";
  const description = service.description ?? "";
  const totalTasks =
    service.numberOfRequestTasks ??
    service.totalTasks ??
    service.numberOfTasks ??
    0;
  const image = service.imageData ?? service.image ?? null;

  return {
    ...service,
    serviceId,
    serviceName,
    description,
    totalTasks: totalTasks ?? 0,
    image,
    slug: getServiceSlug(serviceName),
  };
};
