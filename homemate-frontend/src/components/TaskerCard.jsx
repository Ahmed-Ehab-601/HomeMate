import { useLocation, useNavigate } from "react-router-dom";
import { getTaskerById } from "../api/userProfileApi";
import { useAuth } from "../contexts/AuthContext";

function TaskerCard({ tasker, service, hideRequestButton = false }) {
  const navigate = useNavigate();
  const location = useLocation();
  const { isTasker } = useAuth();
  const hasPhoto = Boolean(tasker.photo);
  const initials = tasker.name
    .split(" ")
    .map((chunk) => chunk[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  const rating = Number.isFinite(tasker.rating) ? tasker.rating : 0;
  const locationText = tasker.location || "Not specified";
  const availabilityText = tasker.availability || "N/A";

  const handleViewProfile = async () => {
    try {
      if (tasker) {
        navigate(`/taskers/${tasker.id}`, {
          state: { service },
        });
      } else {
        alert("Tasker profile not found.");
      }
    } catch (error) {
      console.error("Failed to fetch tasker:", error);
      alert("Could not load tasker profile.");
    }
  };

  return (
    <article className="card tasker-card">
      <div className="tasker-card__header">
        <div className="tasker-card__avatar" aria-hidden="true">
          {hasPhoto ? (
            <img src={tasker.photo} alt={`${tasker.name} avatar`} />
          ) : (
            initials
          )}
        </div>
        <div>
          <h3 className="tasker-card__name">{tasker.name}</h3>
          <p className="tasker-card__meta">
            {service?.serviceName ?? "Home service"} • {locationText}
          </p>
          <p className="tasker-card__meta">
            {rating > 0 ? `Rating ${rating.toFixed(1)} ★` : 'No ratings yet'} • Status {availabilityText}
          </p>
        </div>
      </div>
      <div className="tasker-card__stats">
        <span>${tasker.hourRate}/hr starting</span>
      </div>
      <div className="tasker-card__actions">
        {!isTasker() && !hideRequestButton && (
          <button
            type="button"
            className="btn btn-primary"
            onClick={() =>
              navigate(`/taskers/${tasker.id}/request`, {
                state: { tasker, service, from: location.pathname },
              })
            }
          >
            Request Task
          </button>
        )}
        <button
          type="button"
          className="btn btn-ghost"
          onClick={handleViewProfile}
        >
          View Profile
        </button>
      </div>
    </article>
  );
}

export default TaskerCard;
