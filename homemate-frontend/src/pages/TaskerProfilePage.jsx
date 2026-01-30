import { useEffect, useState } from "react";
import {
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import { getTaskerById } from "../api/userProfileApi";
import { useAuth } from "../contexts/AuthContext";
import { getTaskerReviews } from "../api/taskersApi";

const normalizeImage = (imageValue) => {
  if (!imageValue) return null;
  if (typeof imageValue === "string") {
    return imageValue;
  }
  return null;
};

// Mock data for tasker profile
const MOCK_TASKER = {
  username: "john_plumber",
  email: "john@example.com",
  phoneNumber: "+1-555-0123",
  dateOfBirth: "1990-05-15",
  bio: "Experienced plumber with over 10 years of expertise in residential and commercial plumbing. I specialize in leak repairs, pipe installations, and emergency services. Customer satisfaction is my top priority!",
  profileImage:
    "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&w=400&q=80",
  serviceID: 1,
  hourRate: 45.0,
  workedHours: 320.5,
  earning: 14422.5, // Not shown in profile
  firstName: "John",
  lastName: "Smith",
  city: "Cairo",
  rating: 4.8,
  totalReviews: 23,
  serviceName: "Plumbing",
  availability: "Available Weekdays",
};

// Mock reviews data
const MOCK_REVIEWS = [
  {
    reviewID: 1,
    text: "John did an excellent job fixing our kitchen sink leak. Very professional and arrived on time!",
    rate: 5.0,
    time: "2025-11-20T14:30:00Z",
    images: [],
    reviewerName: "Sarah Johnson",
  },
  {
    reviewID: 2,
    text: "Great service! Fixed our bathroom pipes quickly and explained everything clearly.",
    rate: 4.5,
    time: "2025-11-18T10:15:00Z",
    images: [],
    reviewerName: "Mike Davis",
  },
  {
    reviewID: 3,
    text: "Highly recommend! Very knowledgeable and fair pricing.",
    rate: 5.0,
    time: "2025-11-15T16:45:00Z",
    images: [],
    reviewerName: "Emily Chen",
  },
  {
    reviewID: 4,
    text: "Good work, but took a bit longer than expected. Still happy with the results.",
    rate: 4.0,
    time: "2025-11-10T09:20:00Z",
    images: [],
    reviewerName: "Robert Wilson",
  },
  {
    reviewID: 5,
    text: "Excellent plumber! Solved a complex drainage issue that others couldn't fix.",
    rate: 5.0,
    time: "2025-11-05T13:00:00Z",
    images: [],
    reviewerName: "Lisa Anderson",
  },
];

function TaskerProfilePage() {
  const { taskerId } = useParams();
  const { user, isTasker } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams, setSearchParams] = useSearchParams();
  const [isConnectingStripe, setIsConnectingStripe] = useState(false);

  // Get service from navigation state (passed from TaskerCard)
  const serviceFromState = location.state?.service;

  const initialPage = parseInt(searchParams.get("page") || "0", 10);

  const [tasker, setTasker] = useState(null);

  const [reviews, setReviews] = useState([]);
  const [reviewsTotal, setReviewsTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [reviewsLoading, setReviewsLoading] = useState(false);
  const [error, setError] = useState(null);

  const pageSize = 5;

  const [expandedImage, setExpandedImage] = useState(null);

  // Fetch tasker profile
  useEffect(() => {
    loadTaskerProfile();
  }, [taskerId]);

  // Fetch reviews when page changes
  useEffect(() => {
    if (tasker) {
      loadReviews();
    }
  }, [currentPage, tasker]);

  const loadReviews = () => {
    setReviewsLoading(true);
    // Adjust page to 1-based if backend expects it, or keep 0-based.
    // Dashboard uses 1-based page in variable `page` passed to getTaskerReviews({page, pageSize}).
    // And getTaskerReviews in taskersApi.js now sends `page`.
    // Let's assume 1-based for consistency with Dashboard.
    const pageToSend = currentPage + 1;

    getTaskerReviews(taskerId, pageToSend, pageSize)
      .then((data) => {
        setReviews(data?.reviews ?? []);
        // Update total pages and counts
        setReviewsTotal(data?.totalReviews ?? 0);
        setTotalPages(data?.totalPages ?? 1);
        setReviewsLoading(false);
      })
      .catch((err) => {
        console.error("Failed to load reviews", err);
        setReviewsLoading(false);
      });
  };

  const loadTaskerProfile = async () => {
    setLoading(true);
    setError(null);
    console.log("tasker prop:", taskerId);
    try {
      const response = await getTaskerById(taskerId);
      console.log("🔍 [TASKER PROFILE] Full API Response:", response);
      console.log("🔍 [TASKER PROFILE] Response Keys:", Object.keys(response));
      if (!response) {
        setError("Tasker profile not found.");
        return;
      }
      // Normalize the profile image from backend
      const normalizedTasker = {
        ...response,
        profileImage: normalizeImage(response.image || response.profileImage || response.imageBase64),
      };
      console.log("🔍 [TASKER PROFILE] Normalized Tasker:", normalizedTasker);
      setTasker(normalizedTasker);
    } catch (err) {
      console.error("Failed to load tasker profile:", err);

      let errorMessage = "Failed to load tasker profile. Please try again.";
      if (err.status === 0 || err.error === "NETWORK_ERROR") {
        errorMessage = "No internet connection. Please check your network.";
      } else if (err.status === 404) {
        errorMessage = "Tasker not found.";
      } else if (err.status === 500) {
        errorMessage = "Server error. Please try again later.";
      } else if (err.message) {
        errorMessage = err.message;
      }

      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  const handlePreviousPage = () => {
    if (currentPage > 0) {
      const newPage = currentPage - 1;
      setCurrentPage(newPage);
      setSearchParams({ page: String(newPage) });
      window.scrollTo({
        top: document.getElementById("reviews-section")?.offsetTop - 100,
        behavior: "smooth",
      });
    }
  };

  const handleNextPage = () => {
    if (currentPage < totalPages - 1) {
      const newPage = currentPage + 1;
      setCurrentPage(newPage);
      setSearchParams({ page: String(newPage) });
      window.scrollTo({
        top: document.getElementById("reviews-section")?.offsetTop - 100,
        behavior: "smooth",
      });
    }
  };

  const handleRequestTask = () => {
    // Ensure the tasker object has an 'id' field for RequestTaskPage
    const taskerWithId = {
      ...tasker,
      id: tasker.id || tasker.taskerId || taskerId,
      name:
        tasker.name ||
        `${tasker.firstName} ${tasker.lastName}`.trim() ||
        tasker.username,
      hourRate: tasker.hourRate || tasker.hourrate || 0,
    };

    // Use service from navigation state (passed from TaskerCard)
    // Fallback to creating service object from tasker data if not available
    let serviceInfo;
    if (serviceFromState) {
      serviceInfo = serviceFromState;
    } else {
      // Fallback: try to construct from tasker data
      const serviceId =
        tasker.serviceID || tasker.serviceId || tasker.service_id;
      serviceInfo = {
        serviceId: serviceId,
        serviceName: tasker.serviceName || "Service",
      };
    }

    console.log("🔵 [TASKER PROFILE] Navigation to request task:", {
      tasker: taskerWithId,
      service: serviceInfo,
      serviceFromState,
    });

    navigate(`/taskers/${taskerId}/request`, {
      state: {
        tasker: taskerWithId,
        service: serviceInfo,
      },
    });
  };

  const handleCreateStripeAccount = () => {
    // Only tasker themselves should create connected account for now
    const currentUserId = user?.id || user?.taskerId || user?.taskerID || user?.id;
    const profileTaskerId = tasker?.id || tasker?.taskerId || taskerId;
    if (!currentUserId || String(currentUserId) !== String(profileTaskerId)) {
      // Not the owner; do nothing
      setIsConnectingStripe(false);
      return;
    }
    const idToSend = profileTaskerId;
    setIsConnectingStripe(true);
    import("../api/paymentApi")
      .then(({ createStripeConnectedAccount }) =>
        createStripeConnectedAccount(idToSend)
          .then((res) => {
            const possibleUrl =
              (typeof res === "string" && /^https?:\/\//.test(res) && res) ||
              res?.onboardingUrl || res?.onboarding_url || res?.url || res?.redirectUrl || res?.redirect_url || null;
            if (possibleUrl) {
              const newWin = window.open(possibleUrl, "_blank", "noopener,noreferrer");
              try {
                if (newWin) newWin.opener = null;
              } catch (e) {
                // ignore
              }
              if (!newWin) {
                setFeedback({
                  type: "success",
                  message: `Onboarding opened in a new tab. If nothing happened, open this link: ${possibleUrl}`,
                });
              }
              return;
            }
            loadTaskerProfile();
            setFeedback({ type: "success", message: res?.message ?? "Stripe connected account created successfully." });
          })
          .catch((err) => {
            console.error(err);
            setFeedback({ type: "error", message: err?.message ?? "Failed to create Stripe connected account." });
          })
          .finally(() => setIsConnectingStripe(false)),
      )
      .catch((err) => {
        console.error(err);
        setIsConnectingStripe(false);
      });
  };

  const getInitials = (firstName, lastName) => {
    return `${firstName?.charAt(0) || ""}${
      lastName?.charAt(0) || ""
    }`.toUpperCase();
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
  };

  const renderStars = (rating) => {
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;
    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);

    return (
      <span className="rating-stars">
        {"★".repeat(fullStars)}
        {hasHalfStar && "⯨"}
        {"☆".repeat(emptyStars)}
      </span>
    );
  };

  if (loading) {
    return (
      <main className="page">
        <div className="load-indicator">Loading profile…</div>
      </main>
    );
  }

  if (error || !tasker) {
    return (
      <main className="page">
        <div className="empty-state">
          <p>{error || "We could not load this tasker profile."}</p>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => navigate(-1)}
          >
            Go Back
          </button>
        </div>
      </main>
    );
  }

  return (
    <main className="page tasker-profile-page">
      {/* Profile Header */}
      <section className="profile-header">
        <div className="profile-header__avatar">
          {tasker.profileImage ? (
            <img
              src={tasker.profileImage}
              alt={`${tasker.firstName} ${tasker.lastName}`}
            />
          ) : (
            <div className="profile-avatar-placeholder">
              {getInitials(tasker.firstName, tasker.lastName)}
            </div>
          )}
        </div>
        <div className="profile-header__info">
          <h1 className="profile-header__name">
            {tasker.firstName} {tasker.lastName}
          </h1>
          <p className="profile-header__username">@{tasker.username}</p>
          <div className="profile-header__meta">
            <span className="profile-meta-item">
              <span className="profile-meta-icon">📍</span>
              {tasker.addressCity}
            </span>
            <span className="profile-meta-item">
              <span className="profile-meta-icon">💼</span>
              {tasker.serviceName}
            </span>
            <span className="profile-meta-item">
              <span className="profile-meta-icon">⏰</span>
              {tasker.availability}
            </span>
          </div>
        </div>
        <div className="profile-header__actions">
          {!isTasker() && (
            <button
              type="button"
              className="btn btn-primary btn-large"
              onClick={handleRequestTask}
            >
              Request Task
            </button>
          )}

          {/* If the current user is the tasker owner, show Stripe connect button */}
              {isTasker() && (tasker?.id || tasker?.taskerId || taskerId) && String(user?.id || user?.taskerId) === String(tasker?.id || tasker?.taskerId || taskerId) && !(tasker?.stripeAccountId || tasker?.stripe_account_id) && (
                <div style={{ marginTop: 8 }}>
                  {/* In-card Enable payouts removed — header CTA provides the primary action */}
                </div>
              )}
        </div>
      </section>

      {/* Profile Stats */}
      <section className="profile-stats">
        <div className="profile-stat-card">
          <div className="profile-stat-icon">⭐</div>
          <div className="profile-stat-content">
            <div className="profile-stat-value">{tasker.rating > 0 ? tasker.rating.toFixed(1) : 'N/A'}</div>
            <div className="profile-stat-label">Rating</div>
          </div>
        </div>
        <div className="profile-stat-card">
          <div className="profile-stat-icon">💵</div>
          <div className="profile-stat-content">
            <div className="profile-stat-value">${tasker.hourRate}/hr</div>
            <div className="profile-stat-label">Hourly Rate</div>
          </div>
        </div>
        <div className="profile-stat-card">
          <div className="profile-stat-icon">📝</div>
          <div className="profile-stat-content">
            <div className="profile-stat-value">{reviewsTotal}</div>
            <div className="profile-stat-label">Reviews</div>
          </div>
        </div>
        <div className="profile-stat-card">
          <div className="profile-stat-icon">🕐</div>
          <div className="profile-stat-content">
            <div className="profile-stat-value">
              {tasker.workedHours.toFixed(2)}h
            </div>
            <div className="profile-stat-label">Worked Hours</div>
          </div>
        </div>
      </section>

      {/* About Section */}
      <section className="profile-section">
        <h2 className="profile-section__title">About</h2>
        <div className="profile-section__content">
          <p className="profile-bio">{tasker.bio}</p>
        </div>
      </section>

      <section className="profile-section" id="reviews-section">
        <h2 className="profile-section__title">Reviews ({reviewsTotal})</h2>
        <div className="profile-section__content">
          {reviewsLoading && (
            <div className="load-indicator">Loading reviews…</div>
          )}

          {!reviewsLoading && reviews.length === 0 && (
            <div className="empty-state-reviews">
              <p>No reviews yet.</p>
            </div>
          )}

          {!reviewsLoading && reviews.length > 0 && (
            <>
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
                        {"@" + (review.reviewerUsername || "Client")}
                      </span>
                      <span style={{ color: "var(--text-secondary)" }}>•</span>
                      <span
                        className="tasker-card__meta"
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
                      <div className="review-images">
                        {review.reviewImages.map((img, index) => {
                          const imgSrc = img.imgFile || '';
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

              {totalPages > 1 && (
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
                    Page {currentPage + 1} of {totalPages}
                  </span>

                  <button
                    type="button"
                    className="pagination-btn"
                    onClick={handleNextPage}
                    disabled={currentPage >= totalPages - 1}
                  >
                    Next
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </section>
      {/* Image Modal */}
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
              className="image-modal-close"
              onClick={() => setExpandedImage(null)}
            >
              ×
            </button>
            <img
              src={expandedImage}
              alt="Full size"
              className="image-modal-img"
            />
          </div>
        </div>
      )}
    </main>
  );
}

export default TaskerProfilePage;
