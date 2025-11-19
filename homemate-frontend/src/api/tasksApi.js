const pendingRequests = new Map(); // key: `${userId}-${taskerId}`

function getUserPendingCount(userId) {
  let count = 0;
  pendingRequests.forEach((value) => {
    if (value.userId === userId) {
      count += 1;
    }
  });
  return count;
}

export function hasPendingRequest(userId, taskerId) {
  return pendingRequests.has(`${userId}-${taskerId}`);
}

export function getPendingRequestsCount(userId) {
  return getUserPendingCount(userId);
}

export async function requestTask(payload) {
  // Uncomment to call backend once ready:
  // const response = await fetch(`${import.meta.env.VITE_API_URL}/tasks/request`, {
  //   method: "POST",
  //   headers: {
  //     "Content-Type": "application/json",
  //     Authorization: `Bearer ${payload.token}`,
  //   },
  //   body: JSON.stringify(payload),
  // });
  // if (!response.ok) {
  //   const error = await response.json();
  //   throw error;
  // }
  // return response.json();

  return new Promise((resolve, reject) => {
    setTimeout(() => {
      if (hasPendingRequest(payload.userId, payload.taskerId)) {
        reject({
          status: 400,
          error: "DUPLICATE_REQUEST",
          message: "You already have a pending request with this Tasker",
        });
        return;
      }

      if (getUserPendingCount(payload.userId) >= 10) {
        reject({
          status: 429,
          error: "REQUEST_LIMIT_EXCEEDED",
          message: "Maximum pending requests limit (10) reached",
        });
        return;
      }

      const key = `${payload.userId}-${payload.taskerId}`;
      const response = {
        taskID: Math.floor(Math.random() * 1_000_000),
        startDate: payload.startDate,
        endDate: null,
        status: "INREVIEW",
        description: payload.description ?? "",
        workedHours: 0,
        startInprogress: null,
        bill: null,
        username: payload.username ?? "john_doe",
        taskername: payload.taskerName ?? "tasker_name",
        servicename: payload.serviceName ?? "General",
        chatID: Math.floor(Math.random() * 1_000_000),
        addressDetails: payload.addressDetails ?? "",
        rate: payload.hourRate ?? null,
        reviewText: null,
        reviewImages: null,
        userMail: payload.userMail ?? "customer@example.com",
        taskerMail: payload.taskerMail ?? "tasker@example.com",
      };

      pendingRequests.set(key, { userId: payload.userId, taskerId: payload.taskerId, taskId: response.taskID });
      resolve(response);
    }, 1200);
  });
}

