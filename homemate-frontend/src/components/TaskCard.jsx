import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Modal from "./Modal";
import { acceptTask, rejectTask } from "../api/taskActionsApi";
import { addTaskEstimation } from "../api/taskManagementApi";
import "../styles/TaskCard.css";
import { useAuth } from "../contexts/AuthContext";

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
  const [showEstimationModal, setShowEstimationModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [successBanner, setSuccessBanner] = useState(null);
  const [errorBanner, setErrorBanner] = useState(null);
  const [localStatus, setLocalStatus] = useState(task.status);
  const [estimation, setEstimation] = useState("");
  const [estimationError, setEstimationError] = useState("");
  const { user, getUserRole } = useAuth(); 

  const taskerId = user?.taskerId || user?.id;
  const userRole = getUserRole();

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
    navigate(`/tasks/${task.taskID}`, { state: { estimation: task.timeEstimated } });
  };

  const handleAccept = () => {
    // Show estimation modal first
    setShowAcceptModal(false);
    setShowEstimationModal(true);
    setEstimation("");
    setEstimationError("");
  };

  const handleEstimationSubmit = async () => {
    // Validate estimation (in hours)
    const estValueHours = parseFloat(estimation);
    if (!estimation || isNaN(estValueHours) || estValueHours <= 0) {
      setEstimationError("Please enter a valid estimation (hours greater than 0)");
      return;
    }

    if (estValueHours > 24) {
      setEstimationError("Estimation cannot exceed 24 hours");
      return;
    }

    // Convert hours to minutes for storage
    const estValueMinutes = Math.round(estValueHours * 60);

    setIsSubmitting(true);
    setEstimationError("");
    setErrorBanner(null);

    try {
      // First add estimation (send as minutes)
      // Backend will validate against busy times and schedule conflicts
      await addTaskEstimation(task.taskID, estValueMinutes);
      
      // Then accept the task
      await acceptTask(task.taskID);

      // Instantly update local status
      setLocalStatus("Accepted");

      // Show success banner
      setSuccessBanner("✅ Task accepted successfully with estimation!");
      setShowEstimationModal(false);
      setEstimation("");

      // Auto-dismiss success after 3 seconds
      setTimeout(() => {
        setSuccessBanner(null);
      }, 3000);

      // Call onTaskUpdated if provided
      if (onTaskUpdated) {
        onTaskUpdated();
      }
    } catch (error) {
      // Display the specific error message from backend
      setEstimationError(`❌ ${error.message}`);
      setErrorBanner(`❌ ${error.message}`);
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
      setSuccessBanner("✅ Task rejected successfully.");
      setShowRejectModal(false);

      // Auto-dismiss success after 3 seconds
      setTimeout(() => {
        setSuccessBanner(null);
      }, 3000);
    } catch (error) {
      setShowRejectModal(false);
      setErrorBanner(`❌ Failed to reject task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

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
                Next: Add Estimation
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
          <p>You will need to provide an estimation before accepting.</p>
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
                {isSubmitting ? "Rejecting..." : "Yes, Reject"}
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

      {showEstimationModal && (
        <Modal
          title="Add Estimation & Accept Task"
          onClose={() => {
            if (!isSubmitting) {
              setShowEstimationModal(false);
              setEstimation("");
              setEstimationError("");
            }
          }}
          width={500}
          actions={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  if (!isSubmitting) {
                    setShowEstimationModal(false);
                    setEstimation("");
                    setEstimationError("");
                  }
                }}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                type="button"
                className="btn btn-accept"
                onClick={handleEstimationSubmit}
                disabled={isSubmitting || !estimation}
              >
                {isSubmitting ? "Accepting..." : "Add & Accept"}
              </button>
            </>
          }
        >
          <p>Please provide an estimation for this task before accepting:</p>
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
          <div style={{ marginTop: "16px" }}>
            <label
              htmlFor="estimation-input"
              style={{ display: "block", marginBottom: "8px", fontWeight: 600 }}
            >
              Estimation (hours)*:
            </label>
            <input
              id="estimation-input"
              type="number"
              min="0.5"
              max="24"
              step="0.5"
              value={estimation}
              onChange={(e) => {
                setEstimation(e.target.value);
                setEstimationError("");
              }}
              placeholder="e.g., 2.5"
              disabled={isSubmitting}
              style={{
                width: "100%",
                padding: "10px",
                borderRadius: "6px",
                border: estimationError ? "1px solid #dc2626" : "1px solid #e5e7eb",
                fontSize: "14px",
              }}
            />
            {estimationError && (
              <p style={{ marginTop: "8px", color: "#dc2626", fontSize: "14px" }}>
                {estimationError}
              </p>
            )}
            <p style={{ marginTop: "8px", color: "#6b7280", fontSize: "12px" }}>
              Enter the estimated number of hours needed to complete this task (0.5 - 24 hours)
            </p>
          </div>
        </Modal>
      )}
    </>
  );
}

export default TaskCard;