import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import {
  getAvailableServices,
  getTaskerProfile,
  getTaskerReviews,
  updateTaskerAvailability,
  updateTaskerBio,
  updateTaskerCity,
  updateTaskerEmail,
  updateTaskerHourRate,
  updateTaskerImage,
  updateTaskerName,
  updateTaskerPassword,
  updateTaskerPhone,
  updateTaskerService,
  updateTaskerUsername,
} from "../api/taskerProfileApi";

const REVIEW_PAGE_SIZE = 4;

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

function TaskerDashboardPage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [profile, setProfile] = useState(null);
  const [profileStatus, setProfileStatus] = useState("loading");
  const [profileError, setProfileError] = useState(null);

  const [services, setServices] = useState([]);
  const [servicesStatus, setServicesStatus] = useState("loading");

  const [reviews, setReviews] = useState([]);
  const [reviewsStatus, setReviewsStatus] = useState("loading");
  const [reviewsMeta, setReviewsMeta] = useState({
    currentPage: 1,
    totalPages: 1,
    totalReviews: 0,
  });

  const [forms, setForms] = useState({
    firstName: "",
    lastName: "",
    username: "",
    email: "",
    phone: "",
    city: "",
    hourRate: "",
    serviceId: "",
    availability: "",
    bio: "",
    oldPassword: "",
    newPassword: "",
  });

  const [feedback, setFeedback] = useState(null);
  const [submitting, setSubmitting] = useState(null);
  const [imageStatus, setImageStatus] = useState("idle");
  const [imagePreview, setImagePreview] = useState(null);

  useEffect(() => {
    if (!user) return;
    if (user.role !== "ROLE_TASKER") {
      navigate("/");
      return;
    }
    loadProfile();
    loadServices();
    loadReviews(1);
  }, [user, navigate]);

  const loadProfile = () => {
    setProfileStatus("loading");
    setProfileError(null);
    getTaskerProfile()
      .then((data) => {
        setProfile(data);
        setForms({
          firstName: data?.firstName ?? "",
          lastName: data?.lastName ?? "",
          username: data?.username ?? "",
          email: data?.email ?? "",
          phone: data?.phone ?? "",
          city: data?.addressCity ?? "",
          hourRate: data?.hourrate ?? "",
          serviceId: data?.serviceID ? String(data.serviceID) : "",
          availability: data?.availability ?? "",
          bio: data?.bio ?? "",
          oldPassword: "",
          newPassword: "",
        });
        setImagePreview(normalizeImage(data?.image) ?? null);
        setProfileStatus("success");
      })
      .catch((error) => {
        setProfileStatus("error");
        setProfileError(error);
      });
  };

  const loadServices = () => {
    setServicesStatus("loading");
    getAvailableServices()
      .then((data) => {
        const normalized =
          data?.map((item) => ({
            id: item?.serviceID ?? item?.serviceId ?? item?.id,
            name: item?.name ?? "Unnamed service",
          })) ?? [];
        setServices(normalized);
        setServicesStatus("success");
      })
      .catch(() => setServicesStatus("error"));
  };

  const loadReviews = (page = 1) => {
    setReviewsStatus("loading");
    getTaskerReviews({ page, pageSize: REVIEW_PAGE_SIZE })
      .then((data) => {
        setReviews(data?.reviews ?? []);
        setReviewsMeta({
          currentPage: data?.currentPage ?? page,
          totalPages: data?.totalPages ?? 1,
          totalReviews: data?.totalReviews ?? data?.reviews?.length ?? 0,
        });
        setReviewsStatus("success");
      })
      .catch(() => setReviewsStatus("error"));
  };

  const fullName = useMemo(() => {
    if (!profile) return "";
    const parts = [profile.firstName, profile.lastName].filter(Boolean);
    return parts.length ? parts.join(" ") : profile.username;
  }, [profile]);

  const dismissFeedback = () => setFeedback(null);

  const handleChange = (field, value) => {
    setForms((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const handleFirstNameSubmit = (event) => {
    event.preventDefault();
    setSubmitting("firstName");
    updateTaskerName({
      newFirstName: forms.firstName ?? "",
      newLastName: profile?.lastName ?? "",
    })
      .then(() => {
        setFeedback({ type: "success", message: "First name updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update first name.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleLastNameSubmit = (event) => {
    event.preventDefault();
    setSubmitting("lastName");
    updateTaskerName({
      newFirstName: profile?.firstName ?? "",
      newLastName: forms.lastName ?? "",
    })
      .then(() => {
        setFeedback({ type: "success", message: "Last name updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update last name.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleUsernameSubmit = (event) => {
    event.preventDefault();
    setSubmitting("username");
    updateTaskerUsername({ newUsername: forms.username ?? "" })
      .then(() => {
        setFeedback({ type: "success", message: "Username updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update username.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleEmailSubmit = (event) => {
    event.preventDefault();
    setSubmitting("email");
    updateTaskerEmail({ newEmail: forms.email ?? "" })
      .then(() => {
        setFeedback({ type: "success", message: "Email updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update email.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handlePhoneSubmit = (event) => {
    event.preventDefault();
    setSubmitting("phone");
    updateTaskerPhone({ newPhoneNumber: forms.phone ?? "" })
      .then(() => {
        setFeedback({ type: "success", message: "Phone updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update phone.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleCitySubmit = (event) => {
    event.preventDefault();
    if (!forms.city?.trim()) {
      setFeedback({ type: "error", message: "Enter your city before saving." });
      return;
    }
    setSubmitting("city");
    updateTaskerCity({ newAddressCity: forms.city.trim() })
      .then(() => {
        setFeedback({ type: "success", message: "City updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update city.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleServiceSubmit = (event) => {
    event.preventDefault();
    if (!forms.serviceId) {
      setFeedback({ type: "error", message: "Select a service." });
      return;
    }
    setSubmitting("service");
    updateTaskerService(forms.serviceId)
      .then(() => {
        setFeedback({ type: "success", message: "Service updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update service.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleHourRateSubmit = (event) => {
    event.preventDefault();
    if (forms.hourRate === "" || Number.isNaN(Number(forms.hourRate))) {
      setFeedback({ type: "error", message: "Enter a valid hourly rate." });
      return;
    }
    setSubmitting("hourRate");
    updateTaskerHourRate({ newHourRate: Number(forms.hourRate) })
      .then(() => {
        setFeedback({ type: "success", message: "Hourly rate updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update hourly rate.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleAvailabilitySubmit = (event) => {
    event.preventDefault();
    setSubmitting("availability");
    updateTaskerAvailability({ newAvailability: forms.availability ?? "" })
      .then(() => {
        setFeedback({ type: "success", message: "Availability updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update availability.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handleBioSubmit = (event) => {
    event.preventDefault();
    setSubmitting("bio");
    updateTaskerBio({ newBio: forms.bio ?? "" })
      .then(() => {
        setFeedback({ type: "success", message: "Bio updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update bio.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const handlePasswordSubmit = (event) => {
    event.preventDefault();
    if (!forms.oldPassword?.trim()) {
      setFeedback({
        type: "error",
        message: "Enter your current password first.",
      });
      return;
    }
    if (!forms.newPassword?.trim()) {
      setFeedback({ type: "error", message: "Enter a new password." });
      return;
    }
    setSubmitting("password");
    updateTaskerPassword({
      oldPassword: forms.oldPassword,
      newPassword: forms.newPassword,
    })
      .then(() => {
        setFeedback({ type: "success", message: "Password updated." });
        setForms((prev) => ({
          ...prev,
          oldPassword: "",
          newPassword: "",
        }));
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update password.",
        })
      )
      .finally(() => setSubmitting(null));
  };

  const fileToBase64 = (file) =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });

  const handleImageChange = async (event) => {
    const file = event.target.files?.[0];
    if (!file) return;
    setImageStatus("uploading");
    try {
      const base64 = await fileToBase64(file);
      const payload = base64.includes(",") ? base64.split(",")[1] : base64;
      await updateTaskerImage({ newImage: payload });
      setImagePreview(base64);
      loadProfile();
      setFeedback({ type: "success", message: "Profile photo updated." });
    } catch (error) {
      setFeedback({
        type: "error",
        message: error?.message ?? "Failed to upload image.",
      });
    } finally {
      setImageStatus("idle");
      event.target.value = "";
    }
  };

  const [expandedImage, setExpandedImage] = useState(null);

  const renderReviews = () => {
    if (reviewsStatus === "loading") {
      return <p className="tasker-card__meta">Loading reviews…</p>;
    }
    if (reviewsStatus === "error") {
      return (
        <p className="tasker-card__meta">Unable to load reviews right now.</p>
      );
    }
    if (!reviews.length) {
      return <p className="tasker-card__meta">No reviews yet.</p>;
    }
    return (
      <ul className="review-list">
        {reviews.map((review) => (
          <li
            key={review.reviewId ?? review.taskId}
            className="review-item"
            style={{
              listStyle: "none",
              padding: "16px 0",
              borderBottom: "1px solid #f3f4f6",
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
                {"@" + review.reviewerUsername || "Client"}
              </span>
              <span style={{ color: "var(--text-secondary)" }}>•</span>
              <span
                className="tasker-card__meta"
                style={{ fontSize: "0.85rem", color: "var(--text-secondary)" }}
              >
                {review.time ? new Date(review.time).toLocaleDateString() : ""}
              </span>
              <span style={{ color: "var(--text-secondary)" }}>•</span>
              <span
                style={{
                  color: "#fbbf24",
                  fontSize: "0.95rem",
                  fontWeight: "bold",
                }}
              >
                {review.rate?.toFixed ? review.rate.toFixed(1) : review.rate}★
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
              <div className="review-images">
                {review.reviewImages.map((img, index) => {
                  const imgSrc = img.imgFile.startsWith("data:")
                    ? img.imgFile
                    : `data:image/${img.format || "jpeg"};base64,${
                        img.imgFile
                      }`;
                  return (
                    <img
                      key={img.imgId ?? index}
                      src={imgSrc}
                      alt={img.imgName || "Review attachment"}
                      className="review-thumbnail"
                      onClick={() => setExpandedImage(imgSrc)}
                    />
                  );
                })}
              </div>
            )}
          </li>
        ))}
      </ul>
    );
  };

  // Updated handleTaskerLogout function for TaskerDashboardPage.jsx
  // Replace the existing handleTaskerLogout function with this:

  const handleTaskerLogout = async () => {
    try {
      // The logout function in AuthContext now handles setting offline status
      await logout();
      navigate("/");
    } catch (error) {
      console.error("Logout error:", error);
      // Still navigate even if there's an error
      navigate("/");
    }
  };

  return (
    <main className="page page--wide">
      <div className="profile-shell">
        <header>
          <p className="section-kicker">Tasker workspace</p>
          <h1 className="section-heading">Manage your HomeMate profile</h1>
          <p className="tasker-card__meta">
            Keep your availability, service details, and contact information up
            to date so clients can find you easily.
          </p>
          <div
            className="form-actions"
            style={{ justifyContent: "flex-start" }}
          >
            <button
              type="button"
              className="btn btn-secondary"
              onClick={handleTaskerLogout}
            >
              Sign out
            </button>
          </div>
        </header>

        {feedback && (
          <div
            className={`alert-banner ${
              feedback.type === "error" ? "error" : "success"
            } profile-alert`}
          >
            <span>{feedback.message}</span>
            <button
              type="button"
              className="alert-dismiss"
              onClick={dismissFeedback}
            >
              ×
            </button>
          </div>
        )}

        <section className="card profile-panel">
          {profileStatus === "loading" && (
            <p className="tasker-card__meta">Loading profile…</p>
          )}
          {profileStatus === "error" && (
            <div className="alert alert-error">
              <div>
                We couldn't load your tasker profile.{" "}
                {profileError?.message ?? "Please refresh and try again."}
              </div>
              <div className="form-actions" style={{ marginTop: "8px" }}>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={loadProfile}
                >
                  Retry
                </button>
              </div>
            </div>
          )}
          {profileStatus === "success" && profile && (
            <div className="profile-summary">
              <div className="profile-summary__header">
                <div className="profile-avatar">
                  {imagePreview ? (
                    <img src={imagePreview} alt="Tasker avatar" />
                  ) : (
                    <div className="avatar-placeholder">No photo</div>
                  )}
                </div>
                <div>
                  <p className="section-kicker">Profile overview</p>
                  <h2
                    className="section-heading"
                    style={{ marginBottom: "8px" }}
                  >
                    {fullName || "Tasker"}
                  </h2>
                  <p className="tasker-card__meta">
                    Username: {profile.username}
                  </p>
                </div>
              </div>
              <dl className="profile-summary__grid">
                <div>
                  <dt>Email</dt>
                  <dd>{profile.email ?? "—"}</dd>
                </div>
                <div>
                  <dt>Phone</dt>
                  <dd>{profile.phone ?? "—"}</dd>
                </div>
                <div>
                  <dt>City</dt>
                  <dd>{profile.addressCity ?? "—"}</dd>
                </div>
                <div>
                  <dt>Rating</dt>
                  <dd>
                    {profile.rating ? `${profile.rating.toFixed(1)} ★` : "—"}
                  </dd>
                </div>
                <div>
                  <dt>Hourly rate</dt>
                  <dd>{profile.hourrate ? `$${profile.hourrate}/hr` : "—"}</dd>
                </div>
                <div>
                  <dt>Service</dt>
                  <dd>{profile.serviceID ? `#${profile.serviceID}` : "—"}</dd>
                </div>
                <div>
                  <dt>Availability</dt>
                  <dd>{profile.availability ?? "—"}</dd>
                </div>
                <div>
                  <dt>Total earning</dt>
                  <dd>
                    {profile.totalEarning
                      ? `$${profile.totalEarning.toFixed(2)}`
                      : "—"}
                  </dd>
                </div>
                <div>
                  <dt>Worked hours</dt>
                  <dd>
                    {profile.workedHours !== undefined &&
                    profile.workedHours !== null
                      ? profile.workedHours.toFixed(2)
                      : "—"}
                  </dd>
                </div>
              </dl>
            </div>
          )}
        </section>

        <section className="profile-grid">
          <article className="card profile-panel">
            <p className="section-kicker">Identity</p>
            <h2 className="section-heading">Name & username</h2>

            <form className="profile-form" onSubmit={handleFirstNameSubmit}>
              <div className="form-field">
                <label htmlFor="first-name-input">First name</label>
                <input
                  id="first-name-input"
                  className="input"
                  value={forms.firstName}
                  onChange={(event) =>
                    handleChange("firstName", event.target.value)
                  }
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "firstName"}
                >
                  {submitting === "firstName" ? "Saving…" : "Save first name"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleLastNameSubmit}>
              <div className="form-field">
                <label htmlFor="last-name-input">Last name</label>
                <input
                  id="last-name-input"
                  className="input"
                  value={forms.lastName}
                  onChange={(event) =>
                    handleChange("lastName", event.target.value)
                  }
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "lastName"}
                >
                  {submitting === "lastName" ? "Saving…" : "Save last name"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleUsernameSubmit}>
              <div className="form-field">
                <label htmlFor="username-input">Username</label>
                <input
                  id="username-input"
                  className="input"
                  value={forms.username}
                  onChange={(event) =>
                    handleChange("username", event.target.value)
                  }
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "username"}
                >
                  {submitting === "username" ? "Saving…" : "Save username"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleEmailSubmit}>
              <div className="form-field">
                <label htmlFor="email-input">Email</label>
                <input
                  id="email-input"
                  type="email"
                  className="input"
                  value={forms.email}
                  onChange={(event) =>
                    handleChange("email", event.target.value)
                  }
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "email"}
                >
                  {submitting === "email" ? "Saving…" : "Save email"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handlePhoneSubmit}>
              <div className="form-field">
                <label htmlFor="phone-input">Phone</label>
                <input
                  id="phone-input"
                  className="input"
                  value={forms.phone}
                  onChange={(event) =>
                    handleChange("phone", event.target.value)
                  }
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "phone"}
                >
                  {submitting === "phone" ? "Saving…" : "Save phone"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleCitySubmit}>
              <div className="form-field">
                <label htmlFor="city-input">City</label>
                <input
                  id="city-input"
                  className="input"
                  value={forms.city}
                  onChange={(event) => handleChange("city", event.target.value)}
                  placeholder="e.g., New York City"
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "city"}
                >
                  {submitting === "city" ? "Saving…" : "Save city"}
                </button>
              </div>
            </form>
          </article>

          <article className="card profile-panel">
            <p className="section-kicker">Services</p>
            <h2 className="section-heading">Service & hourly rate</h2>

            <form className="profile-form" onSubmit={handleServiceSubmit}>
              <div className="form-field">
                <label htmlFor="service-select">Service</label>
                <select
                  id="service-select"
                  className="input"
                  value={forms.serviceId}
                  onChange={(event) =>
                    handleChange("serviceId", event.target.value)
                  }
                >
                  <option value="">Select a service</option>
                  {services.map((service) => (
                    <option key={service.id} value={service.id}>
                      {service.name}
                    </option>
                  ))}
                </select>
                {servicesStatus === "loading" && (
                  <p className="tasker-card__meta">Loading services…</p>
                )}
                {servicesStatus === "error" && (
                  <p className="tasker-card__meta">
                    We couldn't load services.{" "}
                    <button
                      type="button"
                      className="link-button"
                      onClick={loadServices}
                    >
                      Retry
                    </button>
                  </p>
                )}
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "service"}
                >
                  {submitting === "service" ? "Saving…" : "Save service"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleHourRateSubmit}>
              <div className="form-field">
                <label htmlFor="hour-rate-input">Hourly rate</label>
                <input
                  id="hour-rate-input"
                  type="number"
                  className="input"
                  value={forms.hourRate}
                  onChange={(event) =>
                    handleChange("hourRate", event.target.value)
                  }
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "hourRate"}
                >
                  {submitting === "hourRate" ? "Saving…" : "Save hourly rate"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleAvailabilitySubmit}>
              <div className="form-field">
                <label htmlFor="availability-input">Availability</label>
                <select
                  id="availability-input"
                  className="input"
                  value={forms.availability || ""}
                  onChange={(event) =>
                    handleChange("availability", event.target.value)
                  }
                >
                  <option value="">—</option>
                  <option value="AVAILABLE">Available</option>
                  <option value="UNAVAILABLE">Unavailable</option>
                </select>
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "availability"}
                >
                  {submitting === "availability"
                    ? "Saving…"
                    : "Save availability"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleBioSubmit}>
              <div className="form-field">
                <label htmlFor="bio-input">Bio</label>
                <textarea
                  id="bio-input"
                  className="input"
                  rows={4}
                  value={forms.bio}
                  onChange={(event) => handleChange("bio", event.target.value)}
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "bio"}
                >
                  {submitting === "bio" ? "Saving…" : "Save bio"}
                </button>
              </div>
            </form>
          </article>
        </section>

        <section className="profile-grid">
          <article className="card profile-panel">
            <p className="section-kicker">Account security</p>
            <h2 className="section-heading">Update password</h2>
            <form className="profile-form" onSubmit={handlePasswordSubmit}>
              <div className="form-field">
                <label htmlFor="old-password-input">Current password</label>
                <input
                  id="old-password-input"
                  type="password"
                  className="input"
                  value={forms.oldPassword}
                  onChange={(event) =>
                    handleChange("oldPassword", event.target.value)
                  }
                  maxLength={72}
                />
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px', textAlign: 'right' }}>
                  {forms.oldPassword.length}/72
                </div>
              </div>
              <div className="form-field">
                <label htmlFor="new-password-input">New password</label>
                <input
                  id="new-password-input"
                  type="password"
                  className="input"
                  value={forms.newPassword}
                  onChange={(event) =>
                    handleChange("newPassword", event.target.value)
                  }
                  maxLength={72}
                />
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px', textAlign: 'right' }}>
                  {forms.newPassword.length}/72
                </div>
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "password"}
                >
                  {submitting === "password" ? "Saving…" : "Save password"}
                </button>
              </div>
            </form>
          </article>

          <article className="card profile-panel">
            <p className="section-kicker">Profile photo</p>
            <h2 className="section-heading">Update image</h2>
            <div className="profile-avatar">
              {imagePreview ? (
                <img src={imagePreview} alt="Tasker avatar" />
              ) : (
                <div className="avatar-placeholder">Upload a photo</div>
              )}
            </div>
            <label className="btn btn-ghost" style={{ width: "fit-content" }}>
              {imageStatus === "uploading" ? "Uploading…" : "Upload new photo"}
              <input
                type="file"
                accept="image/*"
                style={{ display: "none" }}
                onChange={handleImageChange}
                disabled={imageStatus === "uploading"}
              />
            </label>
          </article>
        </section>

        <section className="card profile-panel">
          <div className="section-title" style={{ alignItems: "center" }}>
            <div>
              <p className="section-kicker">Client feedback</p>
              <h2 className="section-heading">Recent reviews</h2>
            </div>
            <div className="form-actions" style={{ gap: "8px" }}>
              <button
                type="button"
                className="btn btn-ghost"
                disabled={reviewsMeta.currentPage <= 1}
                onClick={() =>
                  loadReviews(Math.max(1, reviewsMeta.currentPage - 1))
                }
              >
                Previous
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                disabled={reviewsMeta.currentPage >= reviewsMeta.totalPages}
                onClick={() =>
                  loadReviews(
                    reviewsMeta.currentPage >= reviewsMeta.totalPages
                      ? reviewsMeta.currentPage
                      : reviewsMeta.currentPage + 1
                  )
                }
              >
                Next
              </button>
            </div>
          </div>
          {renderReviews()}
          <p className="tasker-card__meta" style={{ marginTop: "16px" }}>
            Page {reviewsMeta.currentPage} of {reviewsMeta.totalPages} •{" "}
            {reviewsMeta.totalReviews} total reviews
          </p>
        </section>
      </div>
      {expandedImage && (
        <div
          className="image-modal-overlay"
          onClick={() => setExpandedImage(null)}
        >
          <div
            className="image-modal-content"
            onClick={(e) => e.stopPropagation()}
          >
            <button
              type="button"
              className="image-modal-close"
              onClick={() => setExpandedImage(null)}
            >
              ×
            </button>
            <img
              src={expandedImage}
              alt="Expanded view"
              className="image-modal-img"
            />
          </div>
        </div>
      )}
    </main>
  );
}

export default TaskerDashboardPage;
