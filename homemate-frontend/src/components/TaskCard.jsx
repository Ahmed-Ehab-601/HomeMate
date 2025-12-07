import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Modal from "./Modal";
import { acceptTask, rejectTask } from "../api/taskActionsApi";
import "../styles/TaskCard.css";

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

function TaskCard({ task, viewType = "user", onTaskUpdated }) {
  const navigate = useNavigate();
  const [showAcceptModal, setShowAcceptModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successBanner, setSuccessBanner] = useState(null);
  const [errorBanner, setErrorBanner] = useState(null);
  const [localStatus, setLocalStatus] = useState(task.status);

  // Normalize status: remove spaces and convert to uppercase to match STATUS_STYLES keys
  const normalizedStatus =
    localStatus?.toUpperCase().replace(/\s+/g, "") || "INREVIEW";
  const statusStyle = STATUS_STYLES[normalizedStatus] || STATUS_STYLES.INREVIEW;

  // Show Accept/Reject buttons only for Tasker view and InReview status
  const showActionButtons =
    viewType === "tasker" && normalizedStatus === "INREVIEW";

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
    navigate(`/tasks/${task.taskID}`);
  };

  const handleAccept = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await acceptTask(task.taskID);

      // Instantly update local status
      setLocalStatus("Accepted");

      // Show success banner
      setSuccessBanner("✓ Task accepted successfully!");
      setShowAcceptModal(false);

      // Auto-dismiss success after 3 seconds
      setTimeout(() => {
        setSuccessBanner(null);
      }, 3000);
    } catch (error) {
      setShowAcceptModal(false);
      setErrorBanner(`✗ Failed to accept task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReject = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await rejectTask(task.taskID);

      // Instantly update local status
      setLocalStatus("Rejected");

      // Show success banner
      setSuccessBanner("✓ Task rejected successfully.");
      setShowRejectModal(false);

      // Auto-dismiss success after 3 seconds
      setTimeout(() => {
        setSuccessBanner(null);
      }, 3000);
    } catch (error) {
      setShowRejectModal(false);
      setErrorBanner(`✗ Failed to reject task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  // const handleViewTasker = () => {
  //   // Mock tasker ID for now
  //   const taskerId = 1;
  //   navigate(`/taskers/${taskerId}`);
  // };

  return (
    <>
      {successBanner && (
        <div className="banner banner-success">{successBanner}</div>
      )}

      {errorBanner && (
        <div className="banner banner-error">
          {errorBanner}
          <button
            type="button"
            className="banner-close"
            onClick={() => setErrorBanner(null)}
          >
            ×
          </button>
        </div>
      )}

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
            <span className="task-card__value">
              {formatDate(task.startDate)}
            </span>
          </div>

          <div className="task-card__detail-row">
            <span className="task-card__label">Location:</span>
            <span className="task-card__value">{task.addressCity}</span>
          </div>
        </div>

        <div className="task-card__actions">
          {showActionButtons && (
            <>
              <button
                type="button"
                className="btn btn-accept"
                onClick={() => setShowAcceptModal(true)}
                disabled={isSubmitting}
              >
                <span className="btn-icon">✓</span>
                Accept Task
              </button>
              <button
                type="button"
                className="btn btn-reject"
                onClick={() => setShowRejectModal(true)}
                disabled={isSubmitting}
              >
                <span className="btn-icon">✕</span>
                Reject Task
              </button>
            </>
          )}
          {viewType === "tasker" && (
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleViewDetails}
            >
              View Details
            </button>
          )}
          {viewType === "user" && (
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleViewDetails}
            >
              View Details
            </button>
          )}
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

      {showAcceptModal && (
        <Modal
          title="Accept Task Request?"
          onClose={() => !isSubmitting && setShowAcceptModal(false)}
          width={500}
          actions={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setShowAcceptModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-accept"
                onClick={handleAccept}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Accepting task..." : "Yes, Accept"}
              </button>
            </>
          }
        >
          <p>You are about to accept this task:</p>
          <ul className="modal-task-details">
            <li>
              <strong>Customer:</strong>{" "}
              {task.username || task.userName || "N/A"}
            </li>
            <li>
              <strong>Service:</strong> {task.serviceName}
            </li>
            <li>
              <strong>Date & Time:</strong> {formatDate(task.startDate)}
            </li>
            <li>
              <strong>Location:</strong> {task.addressCity}
            </li>
          </ul>
          <p>Are you sure you want to accept?</p>
        </Modal>
      )}

      {showRejectModal && (
        <Modal
          title="Reject Task Request?"
          onClose={() => !isSubmitting && setShowRejectModal(false)}
          width={500}
          actions={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setShowRejectModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-reject"
                onClick={handleReject}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Rejecting task..." : "Yes, Reject"}
              </button>
            </>
          }
        >
          <p>You are about to reject this task:</p>
          <ul className="modal-task-details">
            <li>
              <strong>Customer:</strong>{" "}
              {task.username || task.userName || "N/A"}
            </li>
            <li>
              <strong>Service:</strong> {task.serviceName}
            </li>
            <li>
              <strong>Date & Time:</strong> {formatDate(task.startDate)}
            </li>
          </ul>
          <p className="modal-warning">
            The customer will be notified. Are you sure you want to reject?
          </p>
        </Modal>
      )}
    </>
  );
}

export default TaskCard;
