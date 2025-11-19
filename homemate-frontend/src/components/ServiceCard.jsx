import { useNavigate } from "react-router-dom";

function ServiceCard({ service, onSelect, variant = "default" }) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (onSelect) {
      onSelect(service);
      return;
    }
    navigate(`/services/${service.slug}/taskers`, { state: { service } });
  };

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
      <figure className="service-card__media">
        <img src={service.image} alt={`${service.serviceName} preview`} draggable="false" />
        <figcaption className="service-card__tasks">
          {service.totalTasks} tasks completed
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

