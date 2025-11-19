import { useLocation, useNavigate } from "react-router-dom";
import { useMemo } from "react";
import servicesData from "../data/services";
import { normalizeService } from "../utils/services";

function TaskerCard({ tasker }) {
  const navigate = useNavigate();
  const location = useLocation();
  const services = useMemo(() => servicesData.map(normalizeService), []);
  const service = services.find((svc) => svc.slug === tasker.serviceSlug) ?? null;
  const hasPhoto = Boolean(tasker.photo);
  const initials = tasker.name
    .split(" ")
    .map((chunk) => chunk[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <article className="card tasker-card">
      <div className="tasker-card__header">
        <div className="tasker-card__avatar" aria-hidden="true">
          {hasPhoto ? <img src={tasker.photo} alt={`${tasker.name} avatar`} /> : initials}
        </div>
        <div>
          <h3 className="tasker-card__name">{tasker.name}</h3>
          <p className="tasker-card__meta">
            {tasker.location} • Rating {tasker.rating.toFixed(1)} ★
          </p>
          <p className="tasker-card__meta">Availability: {tasker.availability}</p>
        </div>
      </div>
      <p className="tasker-card__meta">{tasker.bio}</p>
      <div className="tasker-card__stats">
        <span>${tasker.hourRate}/hr starting</span>
      </div>
      <div className="tasker-card__actions">
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
        <button
          type="button"
          className="btn btn-ghost"
          onClick={() => navigate(`/taskers/${tasker.id}`, { state: { tasker, service } })}
        >
          View Profile
        </button>
      </div>
    </article>
  );
}

export default TaskerCard;

