import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchServices } from "../api/servicesApi";
import ServiceCard from "../components/ServiceCard";

function ServicesCatalog() {
  const navigate = useNavigate();
  const [services, setServices] = useState([]);
  const [search, setSearch] = useState("");

  useEffect(() => {
    let cancelled = false;
    fetchServices().then((data) => {
      if (!cancelled) {
        setServices(data);
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
        {filteredServices.length === 0 ? (
          <div className="empty-state">No services match your search.</div>
        ) : (
          <div className="grid grid--services" style={{ marginTop: "24px" }}>
            {filteredServices.map((service) => (
              <ServiceCard
                key={service.serviceId}
                service={service}
                onSelect={handleOpenTaskers}
              />
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

export default ServicesCatalog;

