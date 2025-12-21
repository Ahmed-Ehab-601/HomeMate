import { useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getUserAddresses } from "../api/userProfileApi";
import { requestTask } from "../api/tasksApi";
import { fetchServices } from "../api/servicesApi";
import { getTaskerBusyTime } from "../api/taskManagementApi";
import { useAuth } from "../contexts/AuthContext";
import "../styles/RequestTask.css";

import Modal from "../components/Modal";

const MOCK_USER_ID = 1;

const timeSlots = Array.from({ length: 25 }, (_, index) => {
  const minutes = index * 30;
  const hours = 8 + Math.floor(minutes / 60);
  const mins = minutes % 60;
  if (hours > 20 || (hours === 20 && mins > 0)) {
    return null;
  }
  return `${String(hours).padStart(2, "0")}:${String(mins).padStart(2, "0")}`;
}).filter(Boolean);

const formatDateForInput = (date) => {
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const formatDisplayDateTime = (date, time) => {
  if (!date || !time) return "--";
  const [year, month, day] = date.split("-");
  return `${day}/${month}/${year} ${time}`;
};

const formatAddress = (address) => {
  if (!address) return "";
  const apartment = address.apartment ? `${address.apartment}, ` : "";
  return `${apartment}${address.street}, ${address.city}, ${address.country}`;
};

const descriptionWarningThreshold = 450;

function RequestTaskPage() {
  const { taskerId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const stateTasker = location.state?.tasker;
  const stateService = location.state?.service;
  const fromPath = location.state?.from;

  const tasker = stateTasker;
  const service = stateService;

  const [addresses, setAddresses] = useState([]);
  const [addressStatus, setAddressStatus] = useState("loading");
  const [selectedAddressId, setSelectedAddressId] = useState("");
  const defaultAddressRef = useRef("");
  const [dateValue, setDateValue] = useState("");
  const [timeValue, setTimeValue] = useState("");
  const [description, setDescription] = useState("");
  const [showErrors, setShowErrors] = useState(false);
  const [isConfirmModalOpen, setConfirmModalOpen] = useState(false);
  const [isBackModalOpen, setBackModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submissionError, setSubmissionError] = useState(null);
  const [successBanner, setSuccessBanner] = useState(null);
  const [errorBanner, setErrorBanner] = useState(null);
  const [progressMessage, setProgressMessage] = useState("");
  const [duplicateModalOpen, setDuplicateModalOpen] = useState(false);
  const [duplicateMessage, setDuplicateMessage] = useState("");
  const [limitError, setLimitError] = useState("");
  const [busyTimes, setBusyTimes] = useState({});
  const [loadingBusyTime, setLoadingBusyTime] = useState(false);
  const [taskerUnavailable, setTaskerUnavailable] = useState(false);

  const hasUnsavedChanges =
    Boolean(description) ||
    Boolean(dateValue) ||
    Boolean(timeValue) ||
    (selectedAddressId && selectedAddressId !== defaultAddressRef.current);

  const today = useMemo(() => new Date(), []);
  const maxDate = useMemo(() => {
    const limit = new Date(today);
    limit.setMonth(limit.getMonth() + 3);
    return limit;
  }, [today]);

  useEffect(() => {
    // Only fetch addresses if user is authenticated
    if (!isAuthenticated) {
      setAddressStatus("error");
      setAddresses([]);
      return;
    }

    let cancelled = false;
    getUserAddresses()
      .then((data) => {
        if (cancelled) return;
        // Normalize address IDs
        const normalizedAddresses = (data ?? []).map((address) => ({
          ...address,
          id:
            address.addressId ??
            address.id ??
            address.addressID ??
            address.address_id,
        }));
        setAddresses(normalizedAddresses);
        if (normalizedAddresses.length > 0) {
          const primary =
            normalizedAddresses.find((address) => address.isPrimary) ??
            normalizedAddresses[0];
          defaultAddressRef.current = String(primary.id);
          setSelectedAddressId(String(primary.id));
        }
        setAddressStatus("success");
      })
      .catch((error) => {
        if (!cancelled) {
          // Handle 401 gracefully - user just needs to sign in
          if (error?.status === 401) {
            setAddressStatus("error");
            setAddresses([]);
          } else {
            setAddressStatus("error");
          }
        }
      });

    return () => {
      cancelled = true;
    };
  }, [isAuthenticated]);

  useEffect(() => {
    if (!successBanner) return undefined;
    const timeoutId = window.setTimeout(() => setSuccessBanner(null), 5000);
    return () => window.clearTimeout(timeoutId);
  }, [successBanner]);

  // Fetch busy time when date is selected
  useEffect(() => {
    if (!dateValue || !tasker?.id) {
      setBusyTimes({});
      setTimeValue(""); // Clear time when date changes
      setTaskerUnavailable(false);
      return;
    }

    // Reset time when date changes
    setTimeValue("");
    setBusyTimes({});
    setTaskerUnavailable(false);
    // Clear error banner if it was about unavailability
    if (errorBanner?.message?.includes("unavailable")) {
      setErrorBanner(null);
    }

    const fetchBusyTime = async () => {
      setLoadingBusyTime(true);
      setTaskerUnavailable(false);
      try {
        const busyTimeData = await getTaskerBusyTime(tasker.id, dateValue);
        setBusyTimes(busyTimeData || {});
        setTaskerUnavailable(false);
        // Clear error banner on successful fetch
        if (errorBanner?.message?.includes("unavailable")) {
          setErrorBanner(null);
        }
      } catch (error) {
        console.error("Failed to fetch busy time:", error);
        // Check if error is due to tasker being unavailable
        const errorMessage = error?.message || "";
        if (
          error?.error === "TASKER_UNAVAILABLE" ||
          error?.status === 503 ||
          errorMessage.includes("UNAVAILABLE") ||
          errorMessage.toLowerCase().includes("unavailable")
        ) {
          setTaskerUnavailable(true);
          setBusyTimes({});
          setTimeValue(""); // Clear selected time
          // Show notification banner
          setErrorBanner({
            message: `⚠️ ${tasker.name || "This tasker"} is currently unavailable. Please select another tasker or try again later.`,
          });
        } else {
          setTaskerUnavailable(false);
          setBusyTimes({});
        }
      } finally {
        setLoadingBusyTime(false);
      }
    };

    fetchBusyTime();
  }, [dateValue, tasker?.id]);

  if (!tasker || !service) {
    return <div className="page">We couldn't find that tasker.</div>;
  }

  const selectedAddress = addresses.find(
    (address) => String(address.id) === String(selectedAddressId)
  );

  const isAddressMissing = !selectedAddressId;
  const isScheduleMissing = !dateValue || !timeValue;

  const isDateWithinBounds =
    dateValue &&
    (() => {
      const selected = new Date(dateValue);
      const min = new Date(formatDateForInput(today));
      const max = new Date(formatDateForInput(maxDate));
      return selected >= min && selected <= max;
    })();

  // Check if a time slot is busy
  // Uses local date/time components to avoid timezone conversion issues
  const isTimeSlotBusy = (timeSlot) => {
    if (!dateValue || Object.keys(busyTimes).length === 0) return false;

    const [slotHours, slotMinutes] = timeSlot.split(":").map(Number);
    
    // Parse the selected date (YYYY-MM-DD format)
    const [year, month, day] = dateValue.split("-").map(Number);
    
    // Create slot time as a Date object in local timezone (no timezone conversion)
    const slotDateTime = new Date(year, month - 1, day, slotHours, slotMinutes, 0, 0);
    const slotDateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;

    // Check each busy time interval
    // estimation comes from backend as minutes
    for (const [busyStartStr, estimationMinutes] of Object.entries(busyTimes)) {
      // Parse the busy start date string from backend
      // Backend returns LocalDateTime which serializes to ISO format
      // We need to extract the local time components, not rely on timezone conversion
      let busyStart;
      try {
        busyStart = new Date(busyStartStr);
      } catch (e) {
        console.warn("Failed to parse busy start date:", busyStartStr);
        continue;
      }
      
      // Extract date components in local timezone
      const busyYear = busyStart.getFullYear();
      const busyMonth = busyStart.getMonth();
      const busyDay = busyStart.getDate();
      const busyHours = busyStart.getHours();
      const busyMins = busyStart.getMinutes();
      
      // Reconstruct busy start in local timezone (no timezone conversion)
      const busyStartLocal = new Date(busyYear, busyMonth, busyDay, busyHours, busyMins, 0, 0);
      const busyEndLocal = new Date(busyStartLocal.getTime() + estimationMinutes * 60 * 1000);
      
      // Compare dates first to ensure we're on the same day
      // Compare date components directly to avoid timezone issues
      const slotDate = `${slotDateTime.getFullYear()}-${String(slotDateTime.getMonth() + 1).padStart(2, '0')}-${String(slotDateTime.getDate()).padStart(2, '0')}`;
      const busyDate = `${busyYear}-${String(busyMonth + 1).padStart(2, '0')}-${String(busyDay).padStart(2, '0')}`;
      
      // Only check if dates match
      if (slotDate !== busyDate) {
        continue;
      }
      
      // Now compare times using minute-based calculations (more reliable)
      const slotStartMinutes = slotHours * 60 + slotMinutes;
      const slotEndMinutes = slotStartMinutes + 30; // 30 minutes slot duration
      const busyStartMinutes = busyHours * 60 + busyMins;
      const busyEndMinutes = busyStartMinutes + estimationMinutes;
      
      // Check if slot overlaps with busy period
      if (
        (slotStartMinutes >= busyStartMinutes && slotStartMinutes < busyEndMinutes) ||
        (slotEndMinutes > busyStartMinutes && slotEndMinutes <= busyEndMinutes) ||
        (slotStartMinutes <= busyStartMinutes && slotEndMinutes >= busyEndMinutes)
      ) {
        return true;
      }
    }

    return false;
  };

  // Check if date is in the past
  const isDatePast = (dateStr) => {
    if (!dateStr) return false;
    const selected = new Date(dateStr);
    const todayStart = new Date(formatDateForInput(today));
    todayStart.setHours(0, 0, 0, 0);
    return selected < todayStart;
  };

  // Filter available time slots
  const availableTimeSlots = timeSlots.filter((slot) => {
    // If tasker is unavailable, disable all slots
    if (taskerUnavailable) {
      return false;
    }
    
    if (!dateValue) return true;
    
    // Disable if date is today and time has passed
    const selectedDate = new Date(dateValue);
    const todayDate = new Date(formatDateForInput(today));
    const isToday = selectedDate.getTime() === todayDate.getTime();
    
    if (isToday) {
      const [hours, minutes] = slot.split(":").map(Number);
      const slotTime = new Date();
      slotTime.setHours(hours, minutes, 0, 0);
      const now = new Date();
      if (slotTime <= now) {
        return false;
      }
    }
    
    // Disable if slot is busy
    return !isTimeSlotBusy(slot);
  });

  const canSubmit =
    !isAddressMissing &&
    !isScheduleMissing &&
    isDateWithinBounds &&
    !isSubmitting;

  const describeMissingFields = () => {
    const missing = [];
    if (isAddressMissing) missing.push("service address");
    if (!dateValue) missing.push("preferred date");
    if (!isDateWithinBounds && dateValue) missing.push("valid date");
    if (!timeValue) missing.push("preferred time");
    if (!tasker) missing.push("tasker");
    if (!service) missing.push("service");
    if (!missing.length) return "";
    if (missing.length === 1) {
      return `Please provide the ${missing[0]} before submitting.`;
    }
    const last = missing.pop();
    return `Please provide the ${missing.join(
      ", "
    )} and ${last} before submitting.`;
  };

  const handleBack = () => {
    if (hasUnsavedChanges) {
      setBackModalOpen(true);
      return;
    }
    if (fromPath) {
      navigate(fromPath);
    } else {
      navigate(-1);
    }
  };

  const handleDescriptionChange = (event) => {
    const nextValue = event.target.value.slice(0, 500);
    setDescription(nextValue);
  };

  const openConfirmation = () => {
    setShowErrors(true);
    if (!canSubmit) {
      const message =
        describeMissingFields() ||
        "Please resolve the highlighted fields before submitting.";
      setErrorBanner({
        message,
      });
      return;
    }
    setSubmissionError(null);
    setConfirmModalOpen(true);
  };

  const submitRequest = () => {
    if (!canSubmit) return;
    if (!service?.serviceId) {
      setErrorBanner({
        message:
          "Service information is missing. Please return and select the service again.",
      });
      return;
    }
    setIsSubmitting(true);
    setProgressMessage("Submitting request...");
    const requestDto = {
      userId: MOCK_USER_ID,
      taskerId: tasker.id,
      serviceId: service.serviceId,
      addressId: Number(selectedAddressId),
      startDate: `${dateValue}T${timeValue}:00`,
      description: description.trim(),
    };

    console.info("[RequestTaskPage] Sending task request", requestDto);
    requestTask(requestDto)
      .then((response) => {
        console.info("[RequestTaskPage] Task request response", response);
        setConfirmModalOpen(false);
        setDescription("");
        setDateValue("");
        setTimeValue("");
        defaultAddressRef.current = selectedAddressId;
        setShowErrors(false);
        setSubmissionError(null);
        setErrorBanner(null);
        setLimitError("");
        setDuplicateModalOpen(false);
        setDuplicateMessage("");
        // Redirect to Task Details page with confirmation state
        if (response?.taskID) {
          navigate(`/tasks/${response.taskID}`, {
            state: { requestSubmitted: true },
          });
        }
      })
      .catch((error) => {
        // Close confirm modal to show error messages
        setConfirmModalOpen(false);

        if (error?.error === "DUPLICATE_REQUEST") {
          const message =
            error.message ??
            "You already have a pending request with this Tasker. Please wait for their response.";
          setDuplicateMessage(message);
          setDuplicateModalOpen(true);
          // Clear other error states
          setErrorBanner(null);
          setSubmissionError(null);
        } else if (error?.error === "REQUEST_LIMIT_EXCEEDED") {
          const message =
            error.message ?? "Maximum pending requests limit (10) reached";
          setLimitError(message);
          // Clear other error states
          setErrorBanner(null);
          setSubmissionError(null);
        } else {
          // Generic error - show only error banner
          const message =
            error?.message ??
            "Failed to submit task request. Please try again.";
          setErrorBanner({
            message: `✗ Failed to submit task request. ${message}`,
          });
          setSubmissionError(null);
        }
      })
      .finally(() => {
        setIsSubmitting(false);
        setProgressMessage("");
      });
  };

  const descriptionCounterClass =
    description.length >= descriptionWarningThreshold
      ? "char-counter warn"
      : "char-counter";

  return (
    <main className="page">
      <div className="request-shell">
        <button type="button" className="back-link" onClick={handleBack}>
          ← Back
        </button>
        <div className="request-header">
          <div>
            <p className="section-kicker">Request a task</p>
            <h1 className="section-heading" style={{ marginBottom: "4px" }}>
              {tasker.name}
            </h1>
            <p className="tasker-card__meta">
              {service.serviceName} • ${tasker.hourRate}/hr
            </p>
          </div>
        </div>

        {limitError && <div className="alert alert-error">{limitError}</div>}

        {successBanner && (
          <div className="alert-banner success">
            <div>{successBanner.message}</div>
            <button
              type="button"
              className="alert-link"
              onClick={() =>
                console.info(
                  "Navigate to task details once implemented",
                  successBanner.details
                )
              }
            >
              Go to Task
            </button>
          </div>
        )}

        {errorBanner && (
          <div className="alert-banner error">
            <div>{errorBanner.message}</div>
            <button
              type="button"
              className="alert-dismiss"
              onClick={() => setErrorBanner(null)}
            >
              ×
            </button>
          </div>
        )}

        <section className="request-form card">
          <div className="form-field">
            <label htmlFor="address-select">Service address*</label>
            {addressStatus === "loading" && (
              <p className="tasker-card__meta">Loading addresses…</p>
            )}
            {addressStatus === "error" && (
              <div className="error-text">
                {!isAuthenticated ? (
                  <>
                    Please <a href="/signin">sign in</a> to load your addresses and request a task.
                  </>
                ) : (
                  "We couldn't load your addresses. Please retry."
                )}
              </div>
            )}
            {addressStatus === "success" && addresses.length === 0 && (
              <div className="empty-state">
                No addresses found. Please add an address in your profile first.{" "}
                <a href="/profile">Go to profile</a>
              </div>
            )}
            {addresses.length > 0 && (
              <>
                <select
                  id="address-select"
                  className={`address-select${
                    showErrors && isAddressMissing ? " error" : ""
                  }`}
                  style={{ width: "400px" }}
                  value={selectedAddressId}
                  onChange={(event) => setSelectedAddressId(event.target.value)}
                >
                  <option value="">Select an address</option>
                  {addresses.map((address) => (
                    <option key={address.id} value={address.id}>
                      {formatAddress(address)}
                    </option>
                  ))}
                </select>
                {showErrors && isAddressMissing && (
                  <p className="error-text">Please select a service address.</p>
                )}
              </>
            )}
          </div>

          <div className="form-row">
            <div className="form-field">
              <label htmlFor="date-input">Preferred date*</label>
              <input
                id="date-input"
                type="date"
                className={`input ${
                  showErrors && !isDateWithinBounds ? "error" : ""
                }`}
                min={formatDateForInput(today)}
                max={formatDateForInput(maxDate)}
                value={dateValue}
                onChange={(event) => {
                  const selectedDate = event.target.value;
                  const minDate = formatDateForInput(today);
                  const maxDateStr = formatDateForInput(maxDate);
                  
                  // Only set if the date is within valid range (not in the past)
                  if (selectedDate >= minDate && selectedDate <= maxDateStr) {
                    setDateValue(selectedDate);
                  } else if (selectedDate < minDate) {
                    // If user tries to select a past date, don't update
                    // The browser's date picker should already prevent this, but this adds extra protection
                    setDateValue("");
                  } else {
                    setDateValue(selectedDate);
                  }
                }}
              />
              {showErrors && !isDateWithinBounds && (
                <p className="error-text">Please select a valid date.</p>
              )}
            </div>
            <div className="form-field">
              <label htmlFor="time-select">Preferred time*</label>
              {loadingBusyTime && dateValue && (
                <p className="tasker-card__meta" style={{ marginBottom: "8px" }}>
                  Loading available times...
                </p>
              )}
              {taskerUnavailable && dateValue && (
                <p className="error-text" style={{ marginBottom: "8px", fontWeight: 600 }}>
                  ⚠️ Tasker is currently unavailable. All time slots are disabled.
                </p>
              )}
              <select
                id="time-select"
                className={`input ${showErrors && !timeValue ? "error" : ""} ${
                  taskerUnavailable ? "disabled" : ""
                }`}
                value={timeValue}
                onChange={(event) => setTimeValue(event.target.value)}
                disabled={loadingBusyTime || !dateValue || taskerUnavailable}
                style={taskerUnavailable ? { backgroundColor: "#f3f4f6", cursor: "not-allowed" } : {}}
              >
                <option value="">
                  {!dateValue
                    ? "Select date first"
                    : loadingBusyTime
                    ? "Loading..."
                    : taskerUnavailable
                    ? "Tasker unavailable - no times available"
                    : availableTimeSlots.length === 0
                    ? "No available times"
                    : "Select time"}
                </option>
                {availableTimeSlots.map((slot) => (
                  <option key={slot} value={slot}>
                    {slot}
                  </option>
                ))}
              </select>
              {showErrors && !timeValue && !taskerUnavailable && (
                <p className="error-text">Please select a valid time.</p>
              )}
              {dateValue && !loadingBusyTime && !taskerUnavailable && availableTimeSlots.length === 0 && (
                <p className="error-text">
                  No available time slots on this day. Please select another date.
                </p>
              )}
            </div>
          </div>

          <div className="form-field">
            <label htmlFor="description">Task description (optional)</label>
            <textarea
              id="description"
              className="task-textarea"
              placeholder="Describe your task requirements (e.g., broken pipe in kitchen, need urgent repair...)"
              value={description}
              onChange={handleDescriptionChange}
            />
            <div className={descriptionCounterClass}>
              {description.length}/500 characters
            </div>
          </div>

          <div className="summary-box">
            <h3>Task summary</h3>
            <dl>
              <div>
                <dt>Tasker</dt>
                <dd>{tasker.name}</dd>
              </div>
              <div>
                <dt>Service</dt>
                <dd>{service.serviceName}</dd>
              </div>
              <div>
                <dt>Hourly rate</dt>
                <dd>
                  ${tasker.hourRate}/hr{" "}
                  <span className="tasker-card__meta">
                    (Final cost calculated after task completion based on worked
                    hours)
                  </span>
                </dd>
              </div>
              <div>
                <dt>Address</dt>
                <dd>
                  {selectedAddress ? formatAddress(selectedAddress) : "--"}
                </dd>
              </div>
              <div>
                <dt>Date & time</dt>
                <dd>{formatDisplayDateTime(dateValue, timeValue)}</dd>
              </div>
            </dl>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn btn-ghost"
              onClick={handleBack}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary"
              disabled={!canSubmit}
              onClick={openConfirmation}
            >
              Confirm & Chat
            </button>
          </div>
        </section>
      </div>

      {isBackModalOpen && (
        <Modal
          title="Discard Task Request?"
          onClose={() => setBackModalOpen(false)}
          actions={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setBackModalOpen(false)}
              >
                Stay
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => {
                  setBackModalOpen(false);
                  if (fromPath) {
                    navigate(fromPath);
                  } else {
                    navigate(-1);
                  }
                }}
              >
                Leave
              </button>
            </>
          }
        >
          <p>
            You have unsaved changes. Are you sure you want to leave? All
            entered information will be lost.
          </p>
        </Modal>
      )}

      {duplicateModalOpen && (
        <Modal
          title="⚠️ Existing Request Found"
          onClose={() => setDuplicateModalOpen(false)}
          actions={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  console.info(
                    "Navigate to existing request details once implemented"
                  );
                  setDuplicateModalOpen(false);
                }}
              >
                View Existing Request
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setDuplicateModalOpen(false)}
              >
                Close
              </button>
            </>
          }
        >
          <p>
            {duplicateMessage ||
              `You already have a pending task request with ${tasker.name}. Please wait for their response or cancel your previous request before creating a new one.`}
          </p>
        </Modal>
      )}

      {isConfirmModalOpen && (
        <Modal
          title="Confirm Task Request"
          onClose={() => {
            if (isSubmitting) return;
            setConfirmModalOpen(false);
          }}
          actions={
            <>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  if (!isSubmitting) {
                    setConfirmModalOpen(false);
                  }
                }}
              >
                Edit
              </button>
              <button
                type="button"
                className="btn btn-primary"
                disabled={isSubmitting}
                onClick={submitRequest}
              >
                {isSubmitting ? "Submitting…" : "Confirm"}
              </button>
            </>
          }
        >
          {submissionError && <p className="error-text">{submissionError}</p>}
          <p>You are requesting a task from {tasker.name}</p>
          <ul className="confirm-list">
            <li>Service: {service.serviceName}</li>
            <li>
              Address: {selectedAddress ? formatAddress(selectedAddress) : "--"}
            </li>
            <li>Date & Time: {formatDisplayDateTime(dateValue, timeValue)}</li>
            <li>
              Description: {description ? `${description.slice(0, 100)}…` : "—"}
            </li>
          </ul>
          {progressMessage && <p>{progressMessage}</p>}
        </Modal>
      )}
    </main>
  );
}

export default RequestTaskPage;
