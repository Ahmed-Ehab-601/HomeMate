const DEFAULT_API_BASE_URL = "http://localhost:8080";
const baseUrl = (import.meta.env.VITE_API_URL ?? DEFAULT_API_BASE_URL).replace(/\/$/, "");
const TASK_REQUEST_ENDPOINT = `${baseUrl}/api/user/task/request`;

const parseJson = async (response) => {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch (error) {
    console.error("Failed to parse task request response JSON", error);
    return null;
  }
};

export async function requestTask(payload) {
  const body = {
    userID: payload.userId,
    taskerID: payload.taskerId,
    serviceID: payload.serviceId,
    addressID: payload.addressId,
    startDate: payload.startDate,
    description: payload.description ?? "",
  };
  console.log(TASK_REQUEST_ENDPOINT)

  const response = await fetch(TASK_REQUEST_ENDPOINT, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(body),
  }).catch((error) => {
    throw {
      status: 0,
      error: "NETWORK_ERROR",
      message: error?.message ?? "Unable to reach the task service.",
    };
  });

  const data = await parseJson(response);

  if (!response.ok) {
    throw {
      status: response.status,
      ...(data ?? { error: "UNKNOWN_ERROR", message: "Failed to create task request." }),
    };
  }

  return data;
}


