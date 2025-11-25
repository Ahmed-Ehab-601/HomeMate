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
  const imageFromBinary =
    service.imageData && service.imageType
      ? `data:${service.imageType};base64,${service.imageData}`
      : null;
  const image = service.image ?? imageFromBinary ?? null;

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
