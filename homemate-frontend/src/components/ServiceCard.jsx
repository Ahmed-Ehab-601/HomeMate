import { useNavigate } from "react-router-dom";

function ServiceCard({ service, onSelect, variant = "default" }) {
  const navigate = useNavigate();
  const hasImage = Boolean(service.image);
  const initials = service.serviceName
    ? service.serviceName
        .split(" ")
        .map((word) => word[0])
        .join("")
        .slice(0, 2)
        .toUpperCase()
    : "HM";

  const handleClick = () => {
    if (onSelect) {
      onSelect(service);
      return;
    }
    navigate(`/services/${service.slug}/taskers`, { state: { service } });
  };

  const taskCount = Number.isFinite(service.totalTasks) ? service.totalTasks : 0;
  const taskLabel =
    taskCount === 1
      ? "1 task completed"
      : `${taskCount.toLocaleString()} tasks completed`;

  return (
    <article
      className={`card service-card${variant === "compact" ? " service-card--compact" : ""}`}
      role="button"
      tabIndex={0}
      onClick={handleClick}
      onKeyDown={(event) => {
        if (event.key === "Enter" || event.key === " ") {
          event.preventDefault();
          handleClick();
        }
      }}
    >
      <figure className={`service-card__media${hasImage ? "" : " service-card__media--fallback"}`}>
        {hasImage ? (
          <img src={service.image} alt={`${service.serviceName} preview`} draggable="false" />
        ) : (
          <div className="service-card__media-placeholder" aria-hidden="true">
            {initials}
          </div>
        )}
        <figcaption className="service-card__tasks">
          {taskLabel}
        </figcaption>
      </figure>
      <div className="service-card__body">
        <h3 className="service-card__title">{service.serviceName}</h3>
        <p className="service-card__description">{service.description}</p>
      </div>
    </article>
  );
}

export default ServiceCard;

