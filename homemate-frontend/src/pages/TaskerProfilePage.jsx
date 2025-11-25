import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
// import { fetchTaskerProfile, fetchTaskerReviews } from "../api/taskersApi";

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
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

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

  const loadTaskerProfile = async () => {
    setLoading(true);
    setError(null);

    try {
      // TODO: Uncomment when backend is ready
      // const response = await fetchTaskerProfile(taskerId);
      // setTasker(response);

      // Mock implementation
      await new Promise((resolve) => setTimeout(resolve, 300));
      setTasker(MOCK_TASKER);
    } catch (err) {
      setError("Failed to load tasker profile. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const loadReviews = async () => {
    setReviewsLoading(true);

    try {
      // TODO: Uncomment when backend is ready
      // const response = await fetchTaskerReviews(taskerId, currentPage, pageSize);
      // setReviews(response.reviews || []);
      // setReviewsTotal(response.totalCount || 0);
      // setTotalPages(response.totalPages || 0);

      // Mock implementation with pagination
      await new Promise((resolve) => setTimeout(resolve, 200));
      const start = currentPage * pageSize;
      const end = start + pageSize;
      const paginatedReviews = MOCK_REVIEWS.slice(start, end);

      setReviews(paginatedReviews);
      setReviewsTotal(MOCK_REVIEWS.length);
      setTotalPages(Math.ceil(MOCK_REVIEWS.length / pageSize));
    } catch (err) {
      console.error("Failed to load reviews:", err);
    } finally {
      setReviewsLoading(false);
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
    navigate(`/taskers/${taskerId}/request`, {
      state: { tasker },
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
              {tasker.city}
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
          <button
            type="button"
            className="btn btn-primary btn-large"
            onClick={handleRequestTask}
          >
            Request Task
          </button>
        </div>
      </section>

      {/* Profile Stats */}
      <section className="profile-stats">
        <div className="profile-stat-card">
          <div className="profile-stat-icon">⭐</div>
          <div className="profile-stat-content">
            <div className="profile-stat-value">{tasker.rating.toFixed(1)}</div>
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
            <div className="profile-stat-value">{tasker.totalReviews}</div>
            <div className="profile-stat-label">Reviews</div>
          </div>
        </div>
        <div className="profile-stat-card">
          <div className="profile-stat-icon">🕐</div>
          <div className="profile-stat-content">
            <div className="profile-stat-value">
              {tasker.workedHours.toFixed(0)}h
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

      {/* Reviews Section */}
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
              <div className="reviews-list">
                {reviews.map((review) => (
                  <article key={review.reviewID} className="review-card">
                    <div className="review-card__header">
                      <div className="review-card__avatar">
                        {review.reviewerName.charAt(0).toUpperCase()}
                      </div>
                      <div className="review-card__info">
                        <h3 className="review-card__name">
                          {review.reviewerName}
                        </h3>
                        <p className="review-card__date">
                          {formatDate(review.time)}
                        </p>
                      </div>
                      <div className="review-card__rating">
                        {renderStars(review.rate)}
                        <span className="review-card__rating-value">
                          {review.rate.toFixed(1)}
                        </span>
                      </div>
                    </div>
                    <p className="review-card__text">{review.text}</p>
                    {review.images && review.images.length > 0 && (
                      <div className="review-card__images">
                        {review.images.map((img, idx) => (
                          <img
                            key={idx}
                            src={img.imageData}
                            alt={img.imageName}
                          />
                        ))}
                      </div>
                    )}
                  </article>
                ))}
              </div>

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
    </main>
  );
}

export default TaskerProfilePage;
