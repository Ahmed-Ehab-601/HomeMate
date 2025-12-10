import { useEffect, useState } from "react";
import { useParams, useNavigate, Link, useLocation } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import {
  getTaskDetails,
  rescheduleTask,
  startTask,
  suspendTask,
  completeTask,
} from "../api/taskManagementApi";
import { acceptTask, rejectTask } from "../api/taskActionsApi";
import { getTaskReview } from "../api/taskManagementApi";
import { deleteReview } from "../api/reviewsApi";
import { getTaskerById } from "../api/taskerProfileApi";
import TaskerCard from "../components/TaskerCard";
import Modal from "../components/Modal";
import { websocketService } from "../services/websocketService";
import "../styles/TaskDetails.css";

const normalizeImage = (imageValue) => {
  if (!imageValue) return null;
  if (typeof imageValue === "string") {
    if (imageValue.startsWith("data:")) return imageValue;
    return `data:image/jpeg;base64,${imageValue}`;
  }
  if (Array.isArray(imageValue)) {
    if (typeof window === "undefined" || typeof window.btoa !== "function") {
      return null;
    }
    let binary = "";
    for (let i = 0; i < imageValue.length; i += 1) {
      binary += String.fromCharCode(imageValue[i] & 0xff);
    }
    return `data:image/jpeg;base64,${window.btoa(binary)}`;
  }
  return null;
};

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

function TaskDetailsPage() {
  const { taskId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { getUserRole, getToken } = useAuth();

  const [task, setTask] = useState(null);
  const [review, setReview] = useState(null);
  const [tasker, setTasker] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [elapsedTime, setElapsedTime] = useState(0);
  const [successBanner, setSuccessBanner] = useState(null);
  const [errorBanner, setErrorBanner] = useState(null);

  // Modal states
  const [showAcceptModal, setShowAcceptModal] = useState(false);
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [showRescheduleModal, setShowRescheduleModal] = useState(false);
  const [showStartModal, setShowStartModal] = useState(false);
  const [showSuspendModal, setShowSuspendModal] = useState(false);
  const [showResumeModal, setShowResumeModal] = useState(false);
  const [showCompleteModal, setShowCompleteModal] = useState(false);
  const [showDeleteReviewModal, setShowDeleteReviewModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const [newStartDate, setNewStartDate] = useState("");

  const userRole = getUserRole();
  const isTasker = userRole === "ROLE_TASKER";

  // Check for review submitted or request submitted success message
  useEffect(() => {
    if (location.state?.reviewSubmitted) {
      setSuccessBanner("✅ Review submitted successfully!");
      const timer = setTimeout(() => {
        setSuccessBanner(null);
      }, 3000);
      window.history.replaceState({}, document.title);
      return () => clearTimeout(timer);
    }
    if (location.state?.requestSubmitted) {
      setSuccessBanner(
        "✅ Task request sent successfully! The Tasker will review your request shortly."
      );
      const timer = setTimeout(() => {
        setSuccessBanner(null);
      }, 4000);
      window.history.replaceState({}, document.title);
      return () => clearTimeout(timer);
    }
  }, [location.state]);

  useEffect(() => {
    loadTaskDetails();
  }, [taskId]);

  // Timer effect for in-progress tasks
  useEffect(() => {
    if (task && task.status === "InProgress" && task.startInProgress) {
      const interval = setInterval(() => {
        const now = Date.now();
        const start = new Date(task.startInProgress).getTime();
        const currentSessionSeconds = Math.floor((now - start) / 1000);
        const previousWorkedSeconds = Math.floor(
          (task.workedHours || 0) * 3600
        );
        setElapsedTime(previousWorkedSeconds + currentSessionSeconds);
      }, 1000);

      return () => clearInterval(interval);
    }
  }, [task?.status, task?.startInProgress, task?.workedHours]);

  // WebSocket connection and subscription
  useEffect(() => {
    const token = getToken();
    if (!token || !taskId) return;

    let isSubscribed = false;

    const connectAndSubscribe = async () => {
      try {
        await websocketService.connect(token);

        websocketService.subscribeToTask(taskId, (updatedTask) => {
          console.log("📬 Task update received:", updatedTask);

          // Check if status changed
          if (task && task.status !== updatedTask.status) {
            const statusLabel =
              STATUS_STYLES[
                updatedTask.status?.toUpperCase().replace(/\s+/g, "")
              ]?.label || updatedTask.status;
            showSuccessBanner(`✅ Task status changed to: ${statusLabel}`);
          }

          // Check if rescheduled (startDate changed)
          if (task && task.startDate !== updatedTask.startDate) {
            const newDate = formatDate(updatedTask.startDate);
            showSuccessBanner(`📅 Task rescheduled to: ${newDate}`);
          }

          // Update task state
          setTask(updatedTask);
        });

        isSubscribed = true;
      } catch (error) {
        console.error("Failed to connect WebSocket:", error);
      }
    };

    connectAndSubscribe();

    return () => {
      if (isSubscribed) {
        websocketService.unsubscribeFromTask(taskId);
      }
    };
  }, [taskId, getToken, task?.status, task?.startDate]);

  const loadTaskDetails = async () => {
    setLoading(true);
    setError(null);

    try {
      const taskData = await getTaskDetails(taskId);
      console.log("Task data loaded:", taskData);
      console.log(
        "Task hourRate:",
        taskData.hourRate,
        "Task rate:",
        taskData.rate
      );
      setTask(taskData);

      // Load review if task is done
      if (taskData.status === "Done") {
        try {
          const reviewData = await getTaskReview(taskId);
          setReview(reviewData);
        } catch (err) {
          console.log("No review found:", err);
        }
      }

      // Load tasker profile if user is viewing
      if (!isTasker && taskData.taskerID) {
        try {
          console.log("Loading tasker with ID:", taskData.taskerID);
          const taskerData = await getTaskerById(taskData.taskerID);
          console.log("Tasker data received:", taskerData);

          // If task doesn't have hourRate, add it from tasker data
          if (!taskData.hourRate && !taskData.rate && taskerData.hourRate) {
            taskData.hourRate = taskerData.hourRate;
            setTask({ ...taskData });
          }

          // Transform backend DTO to match TaskerCard props
          setTasker({
            id: taskerData.taskerId,
            name: `${taskerData.firstName} ${taskerData.lastName}`,
            photo: normalizeImage(taskerData.imageBase64),
            rating: taskerData.rating,
            location: taskerData.addressCity,
            availability: taskerData.availability,
            hourRate: taskerData.hourRate,
          });
        } catch (err) {
          console.error("Failed to load tasker:", err);
        }
      }

      // Load tasker profile if tasker is viewing their own task
      if (isTasker && taskData.taskerID) {
        try {
          const taskerData = await getTaskerById(taskData.taskerID);
          console.log("Tasker viewing own task, data:", taskerData);

          // Add hourRate to task from tasker data
          if (!taskData.hourRate && !taskData.rate && taskerData.hourRate) {
            taskData.hourRate = taskerData.hourRate;
            setTask({ ...taskData });
          }
        } catch (err) {
          console.error("Failed to load tasker data for hourRate:", err);
        }
      }
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    const options = {
      weekday: "long",
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    };
    return date.toLocaleDateString("en-US", options);
  };

  const formatTime = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;
    return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(
      2,
      "0"
    )}:${String(secs).padStart(2, "0")}`;
  };

  const formatWorkedHours = (hours) => {
    if (!hours) return "0h 0m";
    const h = Math.floor(hours);
    const m = Math.round((hours - h) * 60);
    return `${h}h ${m}m`;
  };

  const showSuccessBanner = (message) => {
    setSuccessBanner(message);
    setTimeout(() => setSuccessBanner(null), 3000);
  };

  const showErrorBanner = (message) => {
    setErrorBanner(message);
  };

  const normalizedStatus =
    task?.status?.toUpperCase().replace(/\s+/g, "") || "INREVIEW";
  const statusStyle = STATUS_STYLES[normalizedStatus] || STATUS_STYLES.INREVIEW;

  // Accept/Reject handlers
  const handleAccept = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await acceptTask(task.taskID);
      showSuccessBanner("✓ Task accepted successfully!");
      setShowAcceptModal(false);
      await loadTaskDetails();
    } catch (error) {
      setShowAcceptModal(false);
      showErrorBanner(`✗ Failed to accept task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReject = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await rejectTask(task.taskID);
      showSuccessBanner("✓ Task rejected successfully.");
      setShowRejectModal(false);
      await loadTaskDetails();
    } catch (error) {
      setShowRejectModal(false);
      showErrorBanner(`✗ Failed to reject task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Reschedule handler
  const handleReschedule = async () => {
    if (!newStartDate) {
      showErrorBanner("Please select a new date and time");
      return;
    }

    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await rescheduleTask(task.taskID, newStartDate);
      showSuccessBanner("✓ Task rescheduled successfully!");
      setShowRescheduleModal(false);
      setNewStartDate("");
      await loadTaskDetails();
    } catch (error) {
      setShowRescheduleModal(false);
      showErrorBanner(`✗ Failed to reschedule task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Status update handlers
  const handleStart = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await startTask(task.taskID);
      showSuccessBanner("Task started successfully! Timer is now running.");
      setShowStartModal(false);
      await loadTaskDetails();
    } catch (error) {
      setShowStartModal(false);
      showErrorBanner(`✗ Failed to start task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleSuspend = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      const result = await suspendTask(task.taskID);
      showSuccessBanner(
        `Task suspended. Worked time saved: ${formatWorkedHours(
          result.workedHours
        )}`
      );
      setShowSuspendModal(false);
      await loadTaskDetails();
    } catch (error) {
      setShowSuspendModal(false);
      showErrorBanner(`✗ Failed to suspend task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResume = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await startTask(task.taskID); // Resume uses same endpoint as start
      showSuccessBanner("Task resumed. Timer is now running.");
      setShowResumeModal(false);
      await loadTaskDetails();
    } catch (error) {
      setShowResumeModal(false);
      showErrorBanner(`✗ Failed to resume task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleComplete = async () => {
    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      const result = await completeTask(task.taskID);
      showSuccessBanner(
        `Task completed successfully! Total Worked: ${formatWorkedHours(
          result.workedHours
        )} | Bill Amount: $${result.bill.toFixed(2)}`
      );
      setShowCompleteModal(false);
      await loadTaskDetails();
    } catch (error) {
      setShowCompleteModal(false);
      showErrorBanner(`✗ Failed to complete task. ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteReview = async () => {
    if (!review) return;

    setIsSubmitting(true);
    setErrorBanner(null);

    try {
      await deleteReview(review.reviewId);
      showSuccessBanner("✓ Review deleted successfully.");
      setShowDeleteReviewModal(false);
      setReview(null);
    } catch (error) {
      setShowDeleteReviewModal(false);
      showErrorBanner(`✗ Failed to delete review. ${error.message || "Please try again."}`);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p className="loading-text">Loading task details...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <div className="error-emoji">{error.status === 404 ? "😞" : "🔒"}</div>
        <h1 className="error-title">
          {error.status === 404 ? "Task Not Found" : "Access Denied"}
        </h1>
        <p className="error-message">{error.message}</p>
        <button className="btn btn-primary" onClick={() => navigate(-1)}>
          ← Go Back
        </button>
      </div>
    );
  }

  if (!task) return null;

  const canReschedule =
    normalizedStatus === "INREVIEW" || normalizedStatus === "ACCEPTED";
  const showAcceptRejectButtons = isTasker && normalizedStatus === "INREVIEW";
  const showStartButton = isTasker && normalizedStatus === "ACCEPTED";
  const showSuspendButton = isTasker && normalizedStatus === "INPROGRESS";
  const showResumeButton = isTasker && normalizedStatus === "SUSPENDED";
  const showCompleteButton =
    isTasker &&
    (normalizedStatus === "INPROGRESS" || normalizedStatus === "SUSPENDED");
  const showBill =
    normalizedStatus === "DONE" &&
    task.bill !== undefined &&
    task.bill !== null;
  const showTimer = normalizedStatus === "INPROGRESS";
  const showWorkedHours =
    normalizedStatus === "SUSPENDED" || normalizedStatus === "DONE";

  return (
    <div className="task-details-page">
      {successBanner && (
        <div className="banner banner-success">{successBanner}</div>
      )}

      {errorBanner && (
        <div className="banner banner-error">
          {errorBanner}
          <button className="banner-close" onClick={() => setErrorBanner(null)}>
            ×
          </button>
        </div>
      )}

      <div className="task-details-container">
        {/* Main Content */}
        <div className="task-details-content">
          {/* Header */}
          <div className="task-details-header">
            <div className="task-header-top">
              <div>
                <h1 className="task-title">
                  {task.serviceName || "Task Details"}
                </h1>
              </div>
              <div
                className="task-card__status-badge"
                style={{
                  backgroundColor: statusStyle.bg,
                  color: statusStyle.text,
                }}
              >
                {statusStyle.label}
              </div>
            </div>
          </div>

          {/* Work Timer (for In Progress tasks) */}
          {showTimer && (
            <div className="work-timer-section">
              <div className="work-timer-label">Working Time</div>
              <div className="work-timer-value">{formatTime(elapsedTime)}</div>
            </div>
          )}

          {/* Worked Hours (for Suspended/Done tasks) */}
          {showWorkedHours && (
            <div className="work-timer-section suspended">
              <div className="work-timer-label">Total Worked</div>
              <div className="work-timer-value">
                {formatWorkedHours(task.workedHours)}
              </div>
            </div>
          )}

          {/* Bill Summary (for Done tasks) */}
          {showBill && (
            <div className="bill-summary-section">
              <h3 className="bill-summary-title">Task Completion Summary</h3>
              <div className="bill-row">
                <span className="bill-label">Total Worked Time:</span>
                <span className="bill-value">
                  {formatWorkedHours(task.workedHours)}
                </span>
              </div>
              <div className="bill-row">
                <span className="bill-label">Hourly Rate:</span>
                <span className="bill-value">
                  ${task.hourRate || task.rate || "N/A"}/hour
                </span>
              </div>
              <div className="bill-row bill-total">
                <span className="bill-label">Final Bill:</span>
                <span className="bill-value">${task.bill.toFixed(2)}</span>
              </div>
            </div>
          )}

          {/* Description */}
          {task.description && (
            <div className="task-description-section">
              <div className="task-section-label">TASK DESCRIPTION</div>
              <div className="task-section-divider"></div>
              <p className="task-description-text">{task.description}</p>
            </div>
          )}

          {/* Task Details Grid */}
          <div className="task-details-grid">
            <div className="task-section-label">TASK DETAILS</div>
            <div className="task-section-divider"></div>

            <div className="task-detail-row">
              <div className="task-detail-icon">📅</div>
              <div className="task-detail-content">
                <div className="task-detail-label">Date & Time</div>
                <div className="task-detail-value">
                  {formatDate(task.startDate)}
                </div>
              </div>
            </div>

            {task.endDate && normalizedStatus === "DONE" && (
              <div className="task-detail-row">
                <div className="task-detail-icon">🏁</div>
                <div className="task-detail-content">
                  <div className="task-detail-label">Completed On</div>
                  <div className="task-detail-value">
                    {formatDate(task.endDate)}
                  </div>
                </div>
              </div>
            )}

            <div className="task-detail-row">
              <div className="task-detail-icon">📍</div>
              <div className="task-detail-content">
                <div className="task-detail-label">Location</div>
                <div className="task-detail-value">
                  {task.addressDetails || "N/A"}
                </div>
              </div>
            </div>

            <div className="task-detail-row">
              <div className="task-detail-icon">👤</div>
              <div className="task-detail-content">
                <div className="task-detail-label">
                  {isTasker ? "Customer" : "Tasker"}
                </div>
                <div className="task-detail-value">
                  {isTasker ? task.userName : task.taskerName}
                </div>
              </div>
            </div>

            <div className="task-detail-row">
              <div className="task-detail-icon">✉️</div>
              <div className="task-detail-content">
                <div className="task-detail-label">Email</div>
                <div className="task-detail-value">
                  {isTasker ? task.userMail : task.taskerMail}
                </div>
              </div>
            </div>
          </div>

          {/* Review Section */}
          {normalizedStatus === "DONE" && (
            <div className="review-section">
              <div className="task-section-label">REVIEW</div>
              <div className="task-section-divider"></div>

              {review ? (
                <>
                  <div
                    className="review-item"
                    style={{
                      listStyle: "none",
                      padding: "16px 0",
                      display: "flex",
                      flexDirection: "column",
                    }}
                  >
                    <div
                      className="review-item__header"
                      style={{
                        display: "flex",
                        alignItems: "center",
                        marginBottom: "22px",
                        gap: "12px",
                        flexWrap: "wrap",
                      }}
                    >
                      <span
                        style={{
                          fontSize: "0.95rem",
                          color: "var(--text-primary)",
                          fontWeight: "bold",
                        }}
                      >
                        {"@" + (review.reviewerUsername || "Client")}
                      </span>
                      <span style={{ color: "var(--text-secondary)" }}>•</span>
                      <span
                        style={{
                          fontSize: "0.85rem",
                          color: "var(--text-secondary)",
                        }}
                      >
                        {review.time
                          ? new Date(review.time).toLocaleDateString("en-GB")
                          : ""}
                      </span>
                      <span style={{ color: "var(--text-secondary)" }}>•</span>
                      <span
                        style={{
                          color: "#fbbf24",
                          fontSize: "0.95rem",
                          fontWeight: "bold",
                        }}
                      >
                        {review.rate?.toFixed
                          ? review.rate.toFixed(1)
                          : review.rate}
                        ★
                      </span>
                    </div>
                    <p
                      style={{
                        margin: "0",
                        fontSize: "0.95rem",
                        color: "var(--text-primary)",
                        lineHeight: "1.5",
                      }}
                    >
                      {review.text ?? "No review text provided."}
                    </p>
                    {review.reviewImages && review.reviewImages.length > 0 && (
                      <div
                        className="review-images"
                        style={{
                          display: "flex",
                          gap: "12px",
                          marginTop: "16px",
                          flexWrap: "wrap",
                        }}
                      >
                        {review.reviewImages.map((img, index) => {
                          console.log("Review image:", img);
                          let imgSrc;

                          // Handle different image data formats
                          if (img.imgFile) {
                            if (img.imgFile.startsWith("data:")) {
                              imgSrc = img.imgFile;
                            } else if (Array.isArray(img.imgFile)) {
                              // Convert byte array to base64
                              imgSrc = normalizeImage(img.imgFile);
                            } else {
                              // Assume it's base64 string
                              imgSrc = `data:image/${img.format || "jpeg"
                                };base64,${img.imgFile}`;
                            }
                          }

                          if (!imgSrc) {
                            console.warn(
                              "Could not create image source for:",
                              img
                            );
                            return null;
                          }

                          return (
                            <img
                              key={img.imgId ?? index}
                              src={imgSrc}
                              alt={img.imgName || "Review attachment"}
                              className="review-thumbnail"
                              style={{
                                width: "100px",
                                height: "100px",
                                objectFit: "cover",
                                borderRadius: "8px",
                                cursor: "pointer",
                              }}
                              onError={(e) => {
                                console.error("Image failed to load:", img);
                                e.target.style.display = "none";
                              }}
                            />
                          );
                        })}
                      </div>
                    )}
                  </div>

                  {!isTasker && (
                    <div
                      className="review-actions"
                      style={{
                        display: "flex",
                        gap: "12px",
                        marginTop: "16px",
                      }}
                    >
                      <Link
                        to={`/submit-review/${task.taskID}`}
                        className="btn btn-primary"
                        style={{ textDecoration: "none" }}
                      >
                        Edit Review
                      </Link>
                      <button
                        className="btn btn-danger"
                        onClick={() => setShowDeleteReviewModal(true)}
                        disabled={isSubmitting}
                      >
                        Delete Review
                      </button>
                    </div>
                  )}
                </>
              ) : (
                <>
                  <p className="no-review-message">
                    No review has been submitted yet.
                  </p>
                </>
              )}
            </div>
          )}
        </div>

        {/* Right Sidebar */}
        <div className="task-details-sidebar">
          {/* Tasker Profile Card (for users) */}
          {!isTasker && tasker && (
            <div className="tasker-profile-section">
              <h2 className="section-heading">Your Tasker</h2>
              <TaskerCard
                tasker={tasker}
                service={task.serviceName}
                hideRequestButton={true}
              />
            </div>
          )}

          {/* Action Buttons */}
          <div className="action-buttons-section">
            <h3 className="action-buttons-title">Actions</h3>
            <div className="action-buttons-list">
              {/* Accept/Reject for taskers on In Review tasks */}
              {showAcceptRejectButtons && (
                <>
                  <button
                    className="btn btn-primary"
                    onClick={() => setShowAcceptModal(true)}
                    disabled={isSubmitting}
                  >
                    ✓ Accept Task
                  </button>
                  <button
                    className="btn btn-danger"
                    onClick={() => setShowRejectModal(true)}
                    disabled={isSubmitting}
                  >
                    ✕ Reject Task
                  </button>
                </>
              )}

              {/* Start Task */}
              {showStartButton && (
                <button
                  className="btn btn-primary"
                  onClick={() => setShowStartModal(true)}
                  disabled={isSubmitting}
                >
                  Start Task
                </button>
              )}

              {/* Suspend Task */}
              {showSuspendButton && (
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowSuspendModal(true)}
                  disabled={isSubmitting}
                >
                  Suspend Task
                </button>
              )}

              {/* Resume Task */}
              {showResumeButton && (
                <button
                  className="btn btn-primary"
                  onClick={() => setShowResumeModal(true)}
                  disabled={isSubmitting}
                >
                  Resume Task
                </button>
              )}

              {/* Complete Task */}
              {showCompleteButton && (
                <button
                  className="btn btn-primary"
                  onClick={() => setShowCompleteModal(true)}
                  disabled={isSubmitting}
                >
                  Mark as Done
                </button>
              )}

              {/* Reschedule */}
              {canReschedule && (
                <button
                  className="btn btn-reschedule"
                  onClick={() => setShowRescheduleModal(true)}
                  disabled={isSubmitting}
                >
                  <span className="btn-icon">📅</span> Reschedule
                </button>
              )}

              {/* Chat, Report, Review buttons */}
              {task.chatID && (
                <Link to={`/chat/${task.chatID}`} className="btn btn-message">
                  <span className="btn-icon">✉️</span> Message
                </Link>
              )}

              <Link
                to={`/report/submit/${task.taskID}`}
                className="btn btn-report"
              >
                <span className="btn-icon">🛡️</span> Report Issue
              </Link>

              {normalizedStatus === "DONE" && !isTasker && !review && (
                <Link
                  to={`/submit-review/${task.taskID}`}
                  className="btn btn-review"
                >
                  <span className="btn-icon">⭐</span> Leave Review
                </Link>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Accept Modal */}
      {showAcceptModal && (
        <Modal
          title="Accept Task Request?"
          onClose={() => !isSubmitting && setShowAcceptModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowAcceptModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-primary"
                onClick={handleAccept}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Accepting..." : "Yes, Accept"}
              </button>
            </>
          }
        >
          <p>You are about to accept this task:</p>
          <ul className="modal-task-details">
            <li>
              <strong>Customer:</strong> {task.userName}
            </li>
            <li>
              <strong>Service:</strong> {task.serviceName}
            </li>
            <li>
              <strong>Date & Time:</strong> {formatDate(task.startDate)}
            </li>
            <li>
              <strong>Location:</strong> {task.addressDetails}
            </li>
          </ul>
          <p>Are you sure you want to accept?</p>
        </Modal>
      )}

      {/* Reject Modal */}
      {showRejectModal && (
        <Modal
          title="Reject Task Request?"
          onClose={() => !isSubmitting && setShowRejectModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowRejectModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-danger"
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
              <strong>Customer:</strong> {task.userName}
            </li>
            <li>
              <strong>Service:</strong> {task.serviceName}
            </li>
            <li>
              <strong>Date & Time:</strong> {formatDate(task.startDate)}
            </li>
          </ul>
          <p className="modal-warning">The customer will be notified.</p>
          <p>Are you sure you want to reject?</p>
        </Modal>
      )}

      {/* Reschedule Modal */}
      {showRescheduleModal && (
        <Modal
          title="Reschedule Task"
          onClose={() => !isSubmitting && setShowRescheduleModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowRescheduleModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-primary"
                onClick={handleReschedule}
                disabled={isSubmitting || !newStartDate}
              >
                {isSubmitting ? "Rescheduling..." : "Confirm"}
              </button>
            </>
          }
        >
          <div style={{ marginBottom: "16px" }}>
            <label
              style={{ display: "block", marginBottom: "8px", fontWeight: 600 }}
            >
              Current Date:
            </label>
            <div style={{ color: "#6b7280" }}>{formatDate(task.startDate)}</div>
          </div>

          <div>
            <label
              style={{ display: "block", marginBottom: "8px", fontWeight: 600 }}
            >
              New Date & Time:
            </label>
            <input
              type="datetime-local"
              value={newStartDate}
              onChange={(e) => setNewStartDate(e.target.value)}
              onClick={(e) => e.stopPropagation()}
              min={new Date().toISOString().slice(0, 16)}
              style={{
                width: "100%",
                padding: "10px",
                borderRadius: "6px",
                border: "1px solid #e5e7eb",
                fontSize: "14px",
              }}
            />
          </div>
        </Modal>
      )}

      {/* Start Task Modal */}
      {showStartModal && (
        <Modal
          title="Start Working on Task?"
          onClose={() => !isSubmitting && setShowStartModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowStartModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-primary"
                onClick={handleStart}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Starting..." : "Yes, Start Now"}
              </button>
            </>
          }
        >
          <p>You are about to start working on:</p>
          <ul className="modal-task-details">
            <li>
              <strong>Customer:</strong> {task.userName}
            </li>
            <li>
              <strong>Service:</strong> {task.serviceName}
            </li>
            <li>
              <strong>Location:</strong> {task.addressDetails}
            </li>
          </ul>
          <p>The timer will start tracking your work hours.</p>
          <p>Are you sure you want to start?</p>
        </Modal>
      )}

      {/* Suspend Task Modal */}
      {showSuspendModal && (
        <Modal
          title="Suspend Task?"
          onClose={() => !isSubmitting && setShowSuspendModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowSuspendModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-primary"
                onClick={handleSuspend}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Suspending..." : "Yes, Suspend"}
              </button>
            </>
          }
        >
          <p>You are about to pause work on this task:</p>
          <ul className="modal-task-details">
            <li>
              <strong>Customer:</strong> {task.userName}
            </li>
            <li>
              <strong>Current Working Time:</strong> {formatTime(elapsedTime)}
            </li>
          </ul>
          <p>Your work hours will be saved. You can resume later.</p>
        </Modal>
      )}

      {/* Resume Task Modal */}
      {showResumeModal && (
        <Modal
          title="Resume Task?"
          onClose={() => !isSubmitting && setShowResumeModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowResumeModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-primary"
                onClick={handleResume}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Resuming..." : "Yes, Resume"}
              </button>
            </>
          }
        >
          <p>Resume working on:</p>
          <ul className="modal-task-details">
            <li>
              <strong>Customer:</strong> {task.userName}
            </li>
            <li>
              <strong>Service:</strong> {task.serviceName}
            </li>
            <li>
              <strong>Previous Worked Time:</strong>{" "}
              {formatWorkedHours(task.workedHours)}
            </li>
          </ul>
          <p>The timer will continue from where you left off.</p>
          <p>Ready to resume?</p>
        </Modal>
      )}

      {/* Complete Task Modal */}
      {showCompleteModal && (
        <Modal
          title="Complete Task?"
          onClose={() => !isSubmitting && setShowCompleteModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowCompleteModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-primary"
                onClick={handleComplete}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Completing..." : "Yes, Complete Task"}
              </button>
            </>
          }
        >
          <p>You are about to mark this task as completed:</p>
          <ul className="modal-task-details">
            <li>
              <strong>Customer:</strong> {task.userName}
            </li>
            <li>
              <strong>Service:</strong> {task.serviceName}
            </li>
            {normalizedStatus === "INPROGRESS" && (
              <li>
                <strong>Current Working Time:</strong> {formatTime(elapsedTime)}
              </li>
            )}
            {normalizedStatus === "SUSPENDED" && (
              <li>
                <strong>Total Worked Time:</strong>{" "}
                {formatWorkedHours(task.workedHours)}
              </li>
            )}
          </ul>
          <p className="modal-warning">Once completed:</p>
          <ul style={{ marginLeft: "20px" }}>
            <li>Final bill will be calculated</li>
            <li>Customer will be notified</li>
            <li>You cannot restart this task</li>
          </ul>
          <p>Are you ready to complete?</p>
        </Modal>
      )}

      {/* Delete Review Modal */}
      {showDeleteReviewModal && (
        <Modal
          title="Delete Review"
          onClose={() => !isSubmitting && setShowDeleteReviewModal(false)}
          width={500}
          actions={
            <>
              <button
                className="btn btn-secondary"
                onClick={() => setShowDeleteReviewModal(false)}
                disabled={isSubmitting}
              >
                Cancel
              </button>
              <button
                className="btn btn-danger"
                onClick={handleDeleteReview}
                disabled={isSubmitting}
              >
                {isSubmitting ? "Deleting..." : "Yes, Delete Review"}
              </button>
            </>
          }
        >
          <p>Are you sure you want to delete your review?</p>
          <p className="modal-warning" style={{ color: "#dc2626" }}>
            This action cannot be undone.
          </p>
        </Modal>
      )}
    </div>
  );
}

export default TaskDetailsPage;
