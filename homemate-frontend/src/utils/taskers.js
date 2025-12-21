import { getServiceSlug } from "./services";

const availabilityTagMap = {
  weekdays: "weekday",
  weekday: "weekday",
  evenings: "evening",
  evening: "evening",
  weekends: "weekend",
  weekend: "weekend",
};

const buildName = (tasker) => {
  if (tasker.name) return tasker.name;
  const first = tasker.firstName ?? tasker.firstname ?? "";
  const last = tasker.lastName ?? tasker.lastname ?? "";
  const full = `${first} ${last}`.trim();
  return full || "Tasker";
};

const buildPhoto = (tasker) => {
  return tasker.imageBase64 ?? tasker.photoUrl ?? tasker.photo ?? tasker.photoData ?? "";
};

const mapAvailabilityTag = (availability = "") => {
  const key = availability.toLowerCase();
  return availabilityTagMap[key] ?? "weekday";
};

const fallbackId = () => {
  if (typeof crypto !== "undefined" && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  return `tasker-${Math.random().toString(36).slice(2, 11)}`;
};

export const normalizeTasker = (tasker = {}) => {
  const serviceId =
    tasker.serviceId ??
    tasker.serviceID ??
    tasker.serviceid ??
    tasker.service?.serviceId ??
    null;
  const serviceName =
    tasker.serviceName ?? tasker.servicename ?? tasker.service?.serviceName ?? "";

  const rating = tasker.rating ?? tasker.averageRating;
  const location = tasker.location || [tasker.addressCity, tasker.city, tasker.state, tasker.country].filter(Boolean).join(", ") || "";

  return {
    id: String(tasker.taskerId ?? tasker.id ?? fallbackId()),
    name: buildName(tasker),
    photo: buildPhoto(tasker),
    rating: rating != null && rating > 0 ? Number(rating) : 5,
    hourRate: Number(tasker.hourRate ?? tasker.price ?? 0),
    location,
    availability: tasker.availability ?? tasker.schedule ?? "",
    availabilityTag: mapAvailabilityTag(tasker.availability ?? ""),
    bio: tasker.bio ?? tasker.description ?? "",
    serviceId,
    serviceName,
    serviceSlug: serviceName ? getServiceSlug(serviceName) : null,
  };
};