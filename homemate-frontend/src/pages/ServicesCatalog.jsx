// pages/ServicesCatalog.jsx
import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchServices } from "../api/servicesApi";
import ServiceCard from "../components/ServiceCard";

function ServicesCatalog() {
  const navigate = useNavigate();
  const sentinelRef = useRef(null);
  const [services, setServices] = useState([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [visibleCount, setVisibleCount] = useState(12);

  useEffect(() => {
    let cancelled = false;

    setLoading(true);
    setError(null);

    fetchServices()
      .then((data) => {
        if (!cancelled) {
          setServices(data);
          setLoading(false);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.message || "Failed to load services");
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, []);

  const filteredServices = useMemo(() => {
    if (!search) return services;
    const query = search.toLowerCase();
    return services.filter((service) => service.serviceName.toLowerCase().includes(query));
  }, [services, search]);

  useEffect(() => {
    setVisibleCount(12);
  }, [filteredServices]);

  useEffect(() => {
    if (!sentinelRef.current) return () => {};
    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;
        if (entry.isIntersecting) {
          setVisibleCount((prev) => Math.min(prev + 8, filteredServices.length));
        }
      },
      { rootMargin: "200px" },
    );
    observer.observe(sentinelRef.current);
    return () => observer.disconnect();
  }, [filteredServices.length]);

  const handleOpenTaskers = (service) => {
    navigate(`/services/${service.slug}/taskers`, { state: { service } });
  };

  return (
    <main className="page">
      <section className="catalog-wrapper">
        <header className="section-title">
          <div>
            <p className="section-kicker">Explore Services</p>
            <h1 className="section-heading">Find the right tasker by category</h1>
          </div>
        </header>
        
        <input
          className="search-input"
          type="search"
          placeholder="Search by service name…"
          value={search}
          onChange={(event) => setSearch(event.target.value)}
        />
        
        {loading ? (
          <div className="empty-state">Loading services...</div>
        ) : error ? (
          <div className="empty-state" style={{ color: "#dc2626" }}>
            {error}
          </div>
        ) : filteredServices.length === 0 ? (
          <div className="empty-state">No services match your search.</div>
        ) : (
          <>
            <div className="grid grid--services" style={{ marginTop: "24px" }}>
              {filteredServices.slice(0, visibleCount).map((service) => (
                <ServiceCard
                  key={service.serviceId}
                  service={service}
                  onSelect={handleOpenTaskers}
                />
              ))}
            </div>
            {visibleCount < filteredServices.length && (
              <div ref={sentinelRef} className="load-indicator">
                Scroll to load more services
              </div>
            )}
          </>
        )}
      </section>
    </main>
  );
}

export default ServicesCatalog;
