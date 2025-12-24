// pages/UserTasksPage.jsx
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { fetchUserTasks } from "../api/tasksApi";
import TaskCard from "../components/TaskCard";
import TaskCalendar from "../components/TaskCalendar";
import { useAuth } from "../contexts/AuthContext";
import { useUnread } from "../contexts/UnreadContext";

const STATUS_OPTIONS = [
  { value: "All", label: "All" },
  { value: "InReview", label: "In Review" },
  { value: "Accepted", label: "Accepted" },
  { value: "InProgress", label: "In Progress" },
  { value: "Suspended", label: "Suspended" },
  { value: "Done", label: "Done" },
  { value: "Rejected", label: "Rejected" },
  { value: "Unread", label: "Unread Messages" }, // NEW
];

function UserTasksPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { user } = useAuth();
  const { refreshUnread } = useUnread();

  // Get initial values from URL or use defaults
  const initialStatus = searchParams.get("status") || "All";
  const initialPage = parseInt(searchParams.get("page") || "0", 10);

  const [tasks, setTasks] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [selectedStatus, setSelectedStatus] = useState(initialStatus);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [viewMode, setViewMode] = useState("list");

  const userId = user?.userId || user?.id;

  useEffect(() => {
    loadTasks();
    // Scroll to top smoothly when page changes
    if (currentPage > 0 || selectedStatus !== "All") {
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
  }, [currentPage, selectedStatus, pageSize]);

  // Refresh unread count when tasks are loaded
  useEffect(() => {
    if (!loading && userId) {
      refreshUnread();
    }
  }, [loading, userId, refreshUnread]);

  const loadTasks = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetchUserTasks(
        userId,
        selectedStatus === "Unread" ? "All" : selectedStatus, // Backend doesn't support "Unread" filter
        currentPage,
        pageSize
      );
      setTasks(response.tasks || []);
      setTotalCount(response.totalCount || 0);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      handleError(err);
    } finally {
      setLoading(false);
    }
  };

  const handleError = (err) => {
    let errorMessage = "Failed to load tasks. Please try again.";

    if (err.status === 0 || err.error === "NETWORK_ERROR") {
      errorMessage = "No internet connection. Please check your network.";
    } else if (err.status === 500) {
      errorMessage = "Server error. Please try again in a few moments.";
    } else if (err.status === 401) {
      errorMessage = "Session expired. Please log in again.";
      setTimeout(() => navigate("/signin"), 2000);
    } else if (err.status === 400) {
      errorMessage = "Invalid request. Please refresh the page.";
    } else if (err.message) {
      errorMessage = err.message;
    }

    setError(errorMessage);

    setTimeout(() => {
      setError(null);
    }, 10000);
  };

  const handleStatusChange = (event) => {
    const newStatus = event.target.value;
    setSelectedStatus(newStatus);
    setCurrentPage(0);
    setSearchParams({ status: newStatus, page: "0" });
  };

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      const newPage = currentPage - 1;
      setCurrentPage(newPage);
      setSearchParams({ status: selectedStatus, page: String(newPage) });
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      const newPage = currentPage + 1;
      setCurrentPage(newPage);
      setSearchParams({ status: selectedStatus, page: String(newPage) });
    }
  };

  const handleRetry = () => {
    setError(null);
    loadTasks();
  };

  const handleDismissError = () => {
    setError(null);
  };

  const handleFindTasker = () => {
    navigate("/services");
  };

  // Filter tasks by unread if "Unread" status selected
  const displayTasks = selectedStatus === "Unread"
    ? tasks.filter(task => {
        if (user?.role === "ROLE_USER") {
          return task.userHasUnreadMessages === true;
        } else if (user?.role === "ROLE_TASKER") {
          return task.taskerHasUnreadMessages === true;
        }
        return false;
      })
    : tasks;

  const displayCount = selectedStatus === "Unread" ? displayTasks.length : totalCount;

  // Calculate showing range
  const startItem = displayCount === 0 ? 0 : currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, displayCount);

  return (
    <main className="page">
      <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '12px', marginBottom: '16px' }}>
        <button
          onClick={() => setViewMode("list")}
          style={{
            padding: viewMode === 'list' ? '12px 24px' : '8px 16px',
            backgroundColor: viewMode === 'list' ? '#c6ff4d' : '#f3f4f6',
            color: '#111827',
            border: '2px solid #a7df2d',
            borderRadius: '10px',
            cursor: viewMode === 'list' ? 'default' : 'pointer',
            fontWeight: viewMode === 'list' ? '700' : '600',
            fontSize: viewMode === 'list' ? '16px' : '14px',
            opacity: viewMode === 'list' ? 1 : 0.7,
            boxShadow: viewMode === 'list' ? '0 2px 8px rgba(198,255,77,0.15)' : 'none',
            transition: 'all 0.2s',
          }}
          disabled={viewMode === 'list'}
        >
          List View
        </button>
        <button
          onClick={() => setViewMode("calendar")}
          style={{
            padding: viewMode === 'calendar' ? '12px 24px' : '8px 16px',
            backgroundColor: viewMode === 'calendar' ? '#c6ff4d' : '#f3f4f6',
            color: '#111827',
            border: '2px solid #a7df2d',
            borderRadius: '10px',
            cursor: viewMode === 'calendar' ? 'default' : 'pointer',
            fontWeight: viewMode === 'calendar' ? '700' : '600',
            fontSize: viewMode === 'calendar' ? '16px' : '14px',
            opacity: viewMode === 'calendar' ? 1 : 0.7,
            boxShadow: viewMode === 'calendar' ? '0 2px 8px rgba(198,255,77,0.15)' : 'none',
            transition: 'all 0.2s',
          }}
          disabled={viewMode === 'calendar'}
        >
          Calendar View
        </button>
      </div>
      {viewMode === 'calendar' ? (
        <TaskCalendar userRole="ROLE_USER" />
      ) : (
        <>
        {error && (
          <div className="error-banner-fixed">
            <span className="error-banner__icon">⚠️</span>
            <div className="error-banner__content">
              <p className="error-banner__message">{error}</p>
              <div className="error-banner__actions">
                <button
                  type="button"
                  className="error-banner__retry"
                  onClick={handleRetry}
                >
                  Retry
                </button>
              </div>
            </div>
            <button
              type="button"
              className="error-banner__close"
              onClick={handleDismissError}
              aria-label="Dismiss error"
            >
              ×
            </button>
          </div>
        )}
        <section>
          <div className="filter-section">
            <div>
              <p className="section-kicker">Task Management</p>
              <h1 className="section-heading">My Tasks</h1>
            </div>
            <div className="filter-dropdown">
              <label htmlFor="status-filter">Filter by Status:</label>
              <select
                id="status-filter"
                value={selectedStatus}
                onChange={handleStatusChange}
                disabled={loading}
              >
                {STATUS_OPTIONS.map((option) => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
          {loading && <div className="load-indicator">Loading tasks…</div>}
          {!loading && displayCount === 0 && (
            <div className="empty-state-tasks">
              <div className="empty-state-tasks__icon">📋</div>
              <h2 className="empty-state-tasks__title">
                {selectedStatus === "Unread"
                  ? "No tasks with unread messages"
                  : selectedStatus !== "All"
                  ? "No tasks found with this status"
                  : "You haven't requested any tasks yet"}
              </h2>
              {selectedStatus === "All" && (
                <>
                  <p className="empty-state-tasks__subtitle">
                    Start by finding a Tasker to help with your home services
                  </p>
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={handleFindTasker}
                  >
                    Find a Tasker
                  </button>
                </>
              )}
            </div>
          )}
          {!loading && displayCount > 0 && (
            <>
              <div className="tasks-container">
                {displayTasks.map((task) => (
                  <TaskCard 
                    key={task.taskID} 
                    task={task} 
                    viewType="user"
                    onTaskUpdated={loadTasks}
                  />
                ))}
              </div>
              <div className="pagination-info">
                Showing {startItem}-{endItem} of {displayCount} tasks
              </div>
              {selectedStatus !== "Unread" && (
                <div className="pagination-controls">
                  <button
                    type="button"
                    className="pagination-btn"
                    onClick={handlePreviousPage}
                    disabled={currentPage === 0}
                  >
                    Previous
                  </button>
                  <span className="pagination-page-info">
                    Page {currentPage + 1} of {totalPages || 1}
                  </span>
                  <button
                    type="button"
                    className="pagination-btn"
                    onClick={handleNextPage}
                    disabled={currentPage >= totalPages - 1 || totalPages === 0}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </section>
        </>
      )}
    </main>
  );
}

export default UserTasksPage;