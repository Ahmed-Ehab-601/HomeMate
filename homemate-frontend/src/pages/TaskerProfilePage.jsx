import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { fetchTaskerById } from "../api/taskersApi";
import servicesData from "../data/services";
import { normalizeService } from "../utils/services";

function TaskerProfilePage() {
  const { taskerId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const services = useMemo(() => servicesData.map(normalizeService), []);
  const [tasker, setTasker] = useState(location.state?.tasker ?? null);
  const [status, setStatus] = useState(tasker ? "success" : "loading");
  const service =
    location.state?.service ??
    (tasker ? services.find((svc) => svc.serviceId === tasker.serviceId) : null);

  useEffect(() => {
    if (tasker) return;
    let cancelled = false;

    fetchTaskerById(taskerId)
      .then((data) => {
        if (!cancelled) {
          setTasker(data);
          setStatus("success");
        }
      })
      .catch(() => {
        if (!cancelled) {
          setStatus("error");
        }
      });

    return () => {
      cancelled = true;
    };
  }, [tasker, taskerId]);

  if (status === "loading") {
    return <div className="page">Loading profile…</div>;
  }

  if (status === "error" || !tasker) {
    return <div className="page">We could not load this tasker profile.</div>;
  }

  return (
    <main className="page">
      <article className="card">
        <h1 className="hero__title" style={{ fontSize: "2rem" }}>
          {tasker.name}
        </h1>
        <p className="tasker-card__meta">
          {tasker.location} • {tasker.availability}
        </p>
        <p className="tasker-card__stats">
          Rating {tasker.rating.toFixed(1)} ★ • ${tasker.hourRate}/hr
        </p>
        <p>{tasker.bio}</p>
        <div className="tasker-card__actions" style={{ marginTop: "32px" }}>
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
          <button type="button" className="btn btn-ghost" disabled>
            View Profile (coming soon)
          </button>
        </div>
      </article>
    </main>
  );
}

export default TaskerProfilePage;

