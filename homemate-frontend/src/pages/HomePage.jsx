// pages/HomePage.jsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchServices } from "../api/servicesApi";
import ServiceCard from "../components/ServiceCard";

const HERO_IMAGE =
  "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1000&q=80";

function HomePage() {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");

    fetchServices()
      .then((data) => {
        if (!cancelled) {
          setServices(data);
          setStatus("success");
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message || "We could not load services. Please try again.");
          setStatus("error");
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  // Search filtering
  const matchingServices = useMemo(() => {
    if (!searchText) return services;
    const query = searchText.toLowerCase();
    return services.filter((svc) => svc.serviceName.toLowerCase().includes(query));
  }, [services, searchText]);

  const suggestions = searchText ? matchingServices.slice(0, 4) : [];
  const hasNoMatches = Boolean(searchText) && suggestions.length === 0;

  // Popular services: top 6 by totalTasks, filtered to exclude 0 tasks
  const popularServices = useMemo(
    () =>
      [...services]
        .filter((s) => s.totalTasks > 0)
        .sort((a, b) => b.totalTasks - a.totalTasks)
        .slice(0, 2),
    [services]
  );

  const handleSelectService = (service) => {
    navigate(`/services/${service.slug}/taskers`, { state: { service } });
  };

  const handleSearchSubmit = () => {
    if (matchingServices.length > 0) {
      handleSelectService(matchingServices[0]);
    }
  };

  return (
    <main className="page page--wide">
      {/* Hero Section */}
      <section className="hero">
        <div>
          <p className="hero__eyebrow">Home services on demand</p>
          <h1 className="hero__title">Find trusted taskers near you</h1>
          <p className="hero__subtitle">
            Connect with skilled professionals for cleaning, repairs, moving, and more. Book the
            right tasker in minutes.
          </p>
          <div className="search-panel" aria-live="polite">
            <div className="search-icon" aria-hidden="true">🔍</div>
            <label htmlFor="service-search" className="sr-only">Search for a service</label>
            <input
              id="service-search"
              type="search"
              value={searchText}
              placeholder="What service do you need?"
              onChange={(e) => setSearchText(e.target.value)}
            />
            <button type="button" onClick={handleSearchSubmit}>Search</button>
          </div>

          {suggestions.length > 0 && (
            <div className="suggestions">
              {suggestions.map((service) => (
                <button
                  type="button"
                  key={service.serviceId}
                  className="suggestion-item"
                  onClick={() => handleSelectService(service)}
                >
                  <div>
                    <div className="suggestion-item__name">{service.serviceName}</div>
                    <div className="tasker-card__meta">{service.totalTasks} tasks completed</div>
                  </div>
                </button>
              ))}
            </div>
          )}

          {hasNoMatches && (
            <div className="no-results">No matching service found.</div>
          )}
        </div>
        <div className="hero__image">
          <img src={HERO_IMAGE} alt="Professional tasker ready for work" />
        </div>
      </section>

      {/* Popular Services Section */}
      <section>
        <div className="section-title">
          <div>
            <p className="section-kicker">Highly Requested</p>
            <h2 className="section-heading">Popular services near you</h2>
          </div>
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => navigate("/services")}
          >
            Explore services
          </button>
        </div>

        {status === "loading" && <div className="load-indicator">Loading services…</div>}
        {status === "error" && <div className="no-results">{error}</div>}
        {status === "success" && popularServices.length > 0 && (
          <div className="grid grid--services">
            {popularServices.map((service) => (
              <ServiceCard
                key={service.serviceId}
                service={service}
                onSelect={handleSelectService}
                variant="compact"
              />
            ))}
          </div>
        )}
      </section>

      {/* Service Discovery Section */}
      {/* <section id="how-it-works">
        <div className="section-title">
          <div>
            <p className="section-kicker">Service discovery</p>
            <h2 className="section-heading">
              {searchText ? `Results for "${searchText}"` : "Explore every HomeMate service"}
            </h2>
          </div>
        </div>

        {status === "loading" && <div className="load-indicator">Loading services…</div>}
        {status === "error" && <div className="no-results">{error}</div>}

        {status === "success" && (
          <>
            {hasNoMatches ? (
              <div className="empty-state">
                We couldn't find a service named "{searchText}". Try another search or browse all
                taskers below.
              </div>
            ) : (
              <div className="grid grid--services">
                {matchingServices.map((service) => (
                  <ServiceCard
                    key={service.serviceId}
                    service={service}
                    onSelect={handleSelectService}
                  />
                ))}
              </div>
            )}
          </>
        )}
      </section> */}
    </main>
  );
}

export default HomePage;
