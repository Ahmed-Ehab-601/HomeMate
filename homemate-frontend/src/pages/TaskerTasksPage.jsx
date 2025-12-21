import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { fetchTaskerTasks } from "../api/tasksApi";
import TaskCard from "../components/TaskCard";
import { useAuth } from "../contexts/AuthContext";
import TaskCalendar from "../components/TaskCalendar";

const STATUS_OPTIONS = [
  { value: "All", label: "All" },
  { value: "InReview", label: "In Review" },
  { value: "Accepted", label: "Accepted" },
  { value: "InProgress", label: "In Progress" },
  { value: "Suspended", label: "Suspended" },
  { value: "Done", label: "Done" },
  { value: "Rejected", label: "Rejected" },
];

function TaskerTasksPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  // Get initial values from URL or use defaults
  const initialStatus = searchParams.get("status") || "All";
  const initialPage = parseInt(searchParams.get("page") || "0", 10);

  const [tasks, setTasks] = useState([]);
  const [totalCount, setTotalCount] = useState(0);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [selectedStatus, setSelectedStatus] = useState(initialStatus);
  const [loading, setLoading] = useState(true); // Start with loading true for initial fetch
  const [error, setError] = useState(null);
  const [viewMode, setViewMode] = useState('list'); // 'list' or 'calendar'

  // TODO: Replace with actual tasker ID from authentication
  const { user } = useAuth(); // ← ADD THIS
  const taskerId = user?.taskerId || user?.id;

  const isFirstLoad = tasks.length === 0 && !loading;

  useEffect(() => {
    loadTasks();
    // Scroll to top smoothly when page changes (but not on initial load)
    if (!isFirstLoad && (currentPage > 0 || selectedStatus !== "All")) {
      window.scrollTo({ top: 0, behavior: "smooth" });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentPage, selectedStatus, pageSize]);

  const loadTasks = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetchTaskerTasks(
        taskerId,
        selectedStatus,
        currentPage,
        pageSize
      );
      setTasks(response.tasks || []);
      setTotalCount(response.totalCount || 0);
      setTotalPages(response.totalPages || 0);
      // Don't set currentPage from response - it causes double fetch
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
      // TODO: Redirect to login page
      setTimeout(() => navigate("/login"), 2000);
    } else if (err.status === 400) {
      errorMessage = "Invalid request. Please refresh the page.";
    } else if (err.message) {
      errorMessage = err.message;
    }

    setError(errorMessage);

    // Auto-dismiss after 10 seconds
    setTimeout(() => {
      setError(null);
    }, 10000);
  };

  const handleStatusChange = (event) => {
    const newStatus = event.target.value;
    setSelectedStatus(newStatus);
    setCurrentPage(0); // Reset to first page
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

  // Calculate showing range
  const startItem = totalCount === 0 ? 0 : currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, totalCount);

  return (
    <main className="page">
      {viewMode === "calendar" ? (
        <TaskCalendar onBackToList={() => setViewMode("list")} />
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
              <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
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
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => setViewMode("calendar")}
                  style={{
                    padding: '8px 20px',
                    backgroundColor: '#c6ff4d',
                    color: '#111827',
                    border: '1px solid #a7df2d',
                    borderRadius: '8px',
                    fontWeight: '600',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease',
                  }}
                  onMouseOver={(e) => e.target.style.backgroundColor = '#a7df2d'}
                  onMouseOut={(e) => e.target.style.backgroundColor = '#c6ff4d'}
                >
                  📅 Calendar View
                </button>
              </div>
            </div>
            {loading && <div className="load-indicator">Loading tasks…</div>}

            {!loading && totalCount === 0 && (
              <div className="empty-state-tasks">
                <div className="empty-state-tasks__icon">📋</div>
                <h2 className="empty-state-tasks__title">
                  {selectedStatus !== "All"
                    ? "No tasks found with this status"
                    : "You haven't received any task requests yet"}
                </h2>
                {selectedStatus === "All" && (
                  <p className="empty-state-tasks__subtitle">
                    Task requests from customers will appear here
                  </p>
                )}
              </div>
            )}

            {!loading && totalCount > 0 && (
              <>
                <div className="tasks-container">
                  {tasks.map((task) => (
                    <TaskCard
                      key={task.taskId || task.taskID || Math.random()}
                      task={task}
                      viewType="tasker"
                      onTaskUpdated={loadTasks}
                    />
                  ))}
                </div>

                <div className="pagination-info">
                  Showing {startItem}-{endItem} of {totalCount} tasks
                </div>

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
              </>
            )}
          </section>
        </>
      )}
    </main>
  );
}

export default TaskerTasksPage;
