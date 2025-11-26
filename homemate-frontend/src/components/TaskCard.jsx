import { useNavigate } from "react-router-dom";

const STATUS_STYLES = {
  INREVIEW: {
    bg: "#fef3c7",
    text: "#92400e",
    label: "In Review",
  },
  ACCEPTED: {
    bg: "#dbeafe",
    text: "#1e40af",
    label: "Accepted",
  },
  INPROGRESS: {
    bg: "#fed7aa",
    text: "#c2410c",
    label: "In Progress",
  },
  SUSPENDED: {
    bg: "#f3f4f6",
    text: "#6b7280",
    label: "Suspended",
  },
  DONE: {
    bg: "#dcfce7",
    text: "#166534",
    label: "Done",
  },
  REJECTED: {
    bg: "#fee2e2",
    text: "#991b1b",
    label: "Rejected",
  },
};

function TaskCard({ task, viewType = "user" }) {
  const navigate = useNavigate();

  // Normalize status: remove spaces and convert to uppercase to match STATUS_STYLES keys
  const normalizedStatus =
    task.status?.toUpperCase().replace(/\s+/g, "") || "INREVIEW";
  const statusStyle = STATUS_STYLES[normalizedStatus] || STATUS_STYLES.INREVIEW;

  // Debug log only once per task
  if (!task._logged) {
    console.log("📋 [TaskCard] Task status:", {
      original: task.status,
      normalized: normalizedStatus,
      found: !!STATUS_STYLES[normalizedStatus],
    });
    task._logged = true;
  }

  // Format date as DD/MM/YYYY HH:MM
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const year = date.getFullYear();
    const hours = String(date.getHours()).padStart(2, "0");
    const minutes = String(date.getMinutes()).padStart(2, "0");
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  };

  const handleViewDetails = () => {
    // Navigate to task details page (to be implemented in another story)
    navigate(`/tasks/${task.taskId}`);
  };

  // const handleViewTasker = () => {
  //   // Mock tasker ID for now
  //   const taskerId = 1;
  //   navigate(`/taskers/${taskerId}`);
  // };

  return (
    <article className="task-card">
      <div className="task-card__header">
        <div className="task-card__title-section">
          <h3 className="task-card__service">{task.serviceName}</h3>
          <span
            className="task-card__status-badge"
            style={{
              backgroundColor: statusStyle.bg,
              color: statusStyle.text,
            }}
          >
            {statusStyle.label}
          </span>
        </div>
      </div>

      <div className="task-card__details">
        <div className="task-card__detail-row">
          <span className="task-card__label">
            {viewType === "user" ? "Tasker" : "Customer"}:
          </span>
          <span className="task-card__value">
            {viewType === "user"
              ? task.taskername || task.taskerName || "N/A"
              : task.username || task.userName || "N/A"}
          </span>
        </div>

        <div className="task-card__detail-row">
          <span className="task-card__label">Scheduled:</span>
          <span className="task-card__value">{formatDate(task.startDate)}</span>
        </div>

        <div className="task-card__detail-row">
          <span className="task-card__label">Location:</span>
          <span className="task-card__value">{task.addressCity}</span>
        </div>
      </div>

      <div className="task-card__actions">
        <button
          type="button"
          className="btn btn-secondary"
          onClick={handleViewDetails}
        >
          View Details
        </button>
        {/* {viewType === "user" && (
          <button
            type="button"
            className="btn btn-primary"
            onClick={handleViewTasker}
          >
            View Tasker
          </button>
        )} */}
      </div>
    </article>
  );
}

export default TaskCard;
