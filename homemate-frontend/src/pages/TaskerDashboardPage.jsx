import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import {
  getAvailableServices,
  getTaskerProfile,
  getTaskerReviews,
  updateTaskerAvailability,
  updateTaskerBio,
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
    identity: { firstName: "", lastName: "" },
    username: { username: "" },
    contact: { email: "", phone: "" },
    service: { hourRate: "", serviceId: "" },
    availability: { availability: "", bio: "" },
    password: { oldPassword: "", newPassword: "" },
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
          identity: {
            firstName: data?.firstName ?? "",
            lastName: data?.lastName ?? "",
          },
          username: { username: data?.username ?? "" },
          contact: {
            email: data?.email ?? "",
            phone: data?.phone ?? "",
          },
          service: {
            hourRate: data?.hourrate ?? "",
            serviceId: data?.serviceID ? String(data.serviceID) : "",
          },
          availability: {
            availability: data?.availability ?? "",
            bio: data?.bio ?? "",
          },
          password: { oldPassword: "", newPassword: "" },
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

  const handleChange = (section, field, value) => {
    setForms((prev) => ({
      ...prev,
      [section]: {
        ...prev[section],
        [field]: value,
      },
    }));
  };

  const handleIdentitySubmit = (event) => {
    event.preventDefault();
    setSubmitting("identity");
    updateTaskerName({
      newFirstName: forms.identity.firstName ?? "",
      newLastName: forms.identity.lastName ?? "",
    })
      .then(() => {
        setFeedback({ type: "success", message: "Name updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update your name.",
        }),
      )
      .finally(() => setSubmitting(null));
  };

  const handleUsernameSubmit = (event) => {
    event.preventDefault();
    setSubmitting("username");
    updateTaskerUsername({ newUsername: forms.username.username ?? "" })
      .then(() => {
        setFeedback({ type: "success", message: "Username updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update username.",
        }),
      )
      .finally(() => setSubmitting(null));
  };

  const handleContactSubmit = (event) => {
    event.preventDefault();
    setSubmitting("contact");
    Promise.all([
      updateTaskerEmail({ newEmail: forms.contact.email ?? "" }),
      updateTaskerPhone({ newPhoneNumber: forms.contact.phone ?? "" }),
    ])
      .then(() => {
        setFeedback({ type: "success", message: "Contact info updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update contact info.",
        }),
      )
      .finally(() => setSubmitting(null));
  };

  const handleServiceSubmit = (event) => {
    event.preventDefault();
    setSubmitting("service");
    const updates = [];
    if (forms.service.hourRate !== "" && !Number.isNaN(Number(forms.service.hourRate))) {
      updates.push(updateTaskerHourRate({ newHourRate: Number(forms.service.hourRate) }));
    }
    if (forms.service.serviceId) {
      updates.push(updateTaskerService(forms.service.serviceId));
    }
    Promise.all(updates.length ? updates : [Promise.resolve()])
      .then(() => {
        setFeedback({ type: "success", message: "Service details saved." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update service details.",
        }),
      )
      .finally(() => setSubmitting(null));
  };

  const handleAvailabilitySubmit = (event) => {
    event.preventDefault();
    setSubmitting("availability");
    Promise.all([
      updateTaskerAvailability({ newAvailability: forms.availability.availability ?? "" }),
      updateTaskerBio({ newBio: forms.availability.bio ?? "" }),
    ])
      .then(() => {
        setFeedback({ type: "success", message: "Availability updated." });
        loadProfile();
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update availability.",
        }),
      )
      .finally(() => setSubmitting(null));
  };

  const handlePasswordSubmit = (event) => {
    event.preventDefault();
    if (!forms.password.oldPassword?.trim()) {
      setFeedback({ type: "error", message: "Enter your current password first." });
      return;
    }
    if (!forms.password.newPassword?.trim()) {
      setFeedback({ type: "error", message: "Enter a new password." });
      return;
    }
    setSubmitting("password");
    updateTaskerPassword({
      oldPassword: forms.password.oldPassword,
      newPassword: forms.password.newPassword,
    })
      .then(() => {
        setFeedback({ type: "success", message: "Password updated." });
        setForms((prev) => ({
          ...prev,
          password: { oldPassword: "", newPassword: "" },
        }));
      })
      .catch((error) =>
        setFeedback({
          type: "error",
          message: error?.message ?? "Failed to update password.",
        }),
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

  const renderReviews = () => {
    if (reviewsStatus === "loading") {
      return <p className="tasker-card__meta">Loading reviews…</p>;
    }
    if (reviewsStatus === "error") {
      return <p className="tasker-card__meta">Unable to load reviews right now.</p>;
    }
    if (!reviews.length) {
      return <p className="tasker-card__meta">No reviews yet.</p>;
    }
    return (
      <ul className="review-list">
        {reviews.map((review) => (
          <li key={review.reviewId ?? review.taskId} className="review-item">
            <div className="review-item__header">
              <strong>{review.rate?.toFixed ? review.rate.toFixed(1) : review.rate} ★</strong>
              <span className="tasker-card__meta">
                {review.time ? new Date(review.time).toLocaleDateString() : "No date"}
              </span>
            </div>
            <p>{review.text ?? "No review text provided."}</p>
          </li>
        ))}
      </ul>
    );
  };

  const handleTaskerLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <main className="page page--wide">
      <div className="profile-shell">
        <header>
          <p className="section-kicker">Tasker workspace</p>
          <h1 className="section-heading">Manage your HomeMate profile</h1>
          <p className="tasker-card__meta">
            Keep your availability, service details, and contact information up to date so clients can
            find you easily.
          </p>
          <div className="form-actions" style={{ justifyContent: "flex-start" }}>
            <button type="button" className="btn btn-secondary" onClick={handleTaskerLogout}>
              Sign out
            </button>
          </div>
        </header>

        {feedback && (
          <div
            className={`alert-banner ${feedback.type === "error" ? "error" : "success"} profile-alert`}
          >
            <span>{feedback.message}</span>
            <button type="button" className="alert-dismiss" onClick={dismissFeedback}>
              ×
            </button>
          </div>
        )}

        <section className="card profile-panel">
          {profileStatus === "loading" && <p className="tasker-card__meta">Loading profile…</p>}
          {profileStatus === "error" && (
            <div className="alert alert-error">
              <div>
                We couldn’t load your tasker profile.{" "}
                {profileError?.message ?? "Please refresh and try again."}
              </div>
              <div className="form-actions" style={{ marginTop: "8px" }}>
                <button type="button" className="btn btn-primary" onClick={loadProfile}>
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
                  <h2 className="section-heading" style={{ marginBottom: "8px" }}>
                    {fullName || "Tasker"}
                  </h2>
                  <p className="tasker-card__meta">Username: {profile.username}</p>
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
                  <dt>Rating</dt>
                  <dd>{profile.rating ? `${profile.rating.toFixed(1)} ★` : "—"}</dd>
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
                  <dd>{profile.totalEarning ? `$${profile.totalEarning.toFixed(2)}` : "—"}</dd>
                </div>
                <div>
                  <dt>Worked hours</dt>
                  <dd>{profile.workedHours ?? "—"}</dd>
                </div>
              </dl>
            </div>
          )}
        </section>

        <section className="profile-grid">
          <article className="card profile-panel">
            <p className="section-kicker">Identity</p>
            <h2 className="section-heading">Name & username</h2>
            <form className="profile-form" onSubmit={handleIdentitySubmit}>
              <div className="form-field">
                <label htmlFor="first-name-input">First name</label>
                <input
                  id="first-name-input"
                  className="input"
                  value={forms.identity.firstName}
                  onChange={(event) => handleChange("identity", "firstName", event.target.value)}
                />
              </div>
              <div className="form-field">
                <label htmlFor="last-name-input">Last name</label>
                <input
                  id="last-name-input"
                  className="input"
                  value={forms.identity.lastName}
                  onChange={(event) => handleChange("identity", "lastName", event.target.value)}
                />
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary" disabled={submitting === "identity"}>
                  {submitting === "identity" ? "Saving…" : "Save name"}
                </button>
              </div>
            </form>

            <form className="profile-form" onSubmit={handleUsernameSubmit}>
              <div className="form-field">
                <label htmlFor="username-input">Username</label>
                <input
                  id="username-input"
                  className="input"
                  value={forms.username.username}
                  onChange={(event) => handleChange("username", "username", event.target.value)}
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

            <form className="profile-form" onSubmit={handleContactSubmit}>
              <div className="form-field">
                <label htmlFor="email-input">Email</label>
                <input
                  id="email-input"
                  type="email"
                  className="input"
                  value={forms.contact.email}
                  onChange={(event) => handleChange("contact", "email", event.target.value)}
                />
              </div>
              <div className="form-field">
                <label htmlFor="phone-input">Phone</label>
                <input
                  id="phone-input"
                  className="input"
                  value={forms.contact.phone}
                  onChange={(event) => handleChange("contact", "phone", event.target.value)}
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "contact"}
                >
                  {submitting === "contact" ? "Saving…" : "Save contact"}
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
                  value={forms.service.serviceId}
                  onChange={(event) => handleChange("service", "serviceId", event.target.value)}
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
                    We couldn’t load services.{" "}
                    <button type="button" className="link-button" onClick={loadServices}>
                      Retry
                    </button>
                  </p>
                )}
              </div>
              <div className="form-field">
                <label htmlFor="hour-rate-input">Hourly rate</label>
                <input
                  id="hour-rate-input"
                  type="number"
                  className="input"
                  value={forms.service.hourRate}
                  onChange={(event) => handleChange("service", "hourRate", event.target.value)}
                />
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

            <form className="profile-form" onSubmit={handleAvailabilitySubmit}>
              <div className="form-field">
                <label htmlFor="availability-input">Availability</label>
                <input
                  id="availability-input"
                  className="input"
                  value={forms.availability.availability}
                  onChange={(event) => handleChange("availability", "availability", event.target.value)}
                  placeholder="e.g., Weekdays 9-5, weekends only"
                />
              </div>
              <div className="form-field">
                <label htmlFor="bio-input">Bio</label>
                <textarea
                  id="bio-input"
                  className="input"
                  rows={4}
                  value={forms.availability.bio}
                  onChange={(event) => handleChange("availability", "bio", event.target.value)}
                />
              </div>
              <div className="form-actions">
                <button
                  type="submit"
                  className="btn btn-primary"
                  disabled={submitting === "availability"}
                >
                  {submitting === "availability" ? "Saving…" : "Save availability"}
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
                  value={forms.password.oldPassword}
                  onChange={(event) => handleChange("password", "oldPassword", event.target.value)}
                />
              </div>
              <div className="form-field">
                <label htmlFor="new-password-input">New password</label>
                <input
                  id="new-password-input"
                  type="password"
                  className="input"
                  value={forms.password.newPassword}
                  onChange={(event) => handleChange("password", "newPassword", event.target.value)}
                />
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
                onClick={() => loadReviews(Math.max(1, reviewsMeta.currentPage - 1))}
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
                      : reviewsMeta.currentPage + 1,
                  )
                }
              >
                Next
              </button>
            </div>
          </div>
          {renderReviews()}
          <p className="tasker-card__meta" style={{ marginTop: "16px" }}>
            Page {reviewsMeta.currentPage} of {reviewsMeta.totalPages} • {reviewsMeta.totalReviews} total
            reviews
          </p>
        </section>
      </div>
    </main>
  );
}

export default TaskerDashboardPage;


