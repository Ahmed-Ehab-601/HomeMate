import { useEffect, useMemo, useRef, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import TaskerCard from "../components/TaskerCard";
import { fetchTaskers } from "../api/taskersApi";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { fetchServices } from "../api/servicesApi";

const PAGE_SIZE = 12;

function TaskerDiscoveryPage() {
  const { slug } = useParams();
  const location = useLocation();
  const serviceFromState = location.state?.service;
  const [services, setServices] = useState([]);
  const [servicesError, setServicesError] = useState("");
  const [servicesLoading, setServicesLoading] = useState(false);

  const [taskers, setTaskers] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const [filters, setFilters] = useState({
    rate: "any",
    availability: "any",
    rating: "any",
    location: "",
  });
  const [sortOption, setSortOption] = useState("rating-desc");
  const [search, setSearch] = useState("");
  const debouncedSearch = useDebouncedValue(search, 350);

  useEffect(() => {
    let cancelled = false;
    if (serviceFromState) {
      setServices([serviceFromState]);
      return () => {};
    }
    setServicesLoading(true);
    setServicesError("");
    fetchServices()
      .then((data) => {
        if (!cancelled) {
          setServices(data);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setServicesError("Unable to load services right now.");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setServicesLoading(false);
        }
      });
    return () => {
      cancelled = true;
    };
  }, [serviceFromState]);

  const service = useMemo(() => {
    if (serviceFromState) return serviceFromState;
    return services.find((candidate) => candidate.slug === slug) ?? null;
  }, [serviceFromState, services, slug]);

  const serviceId = service?.serviceId ?? null;

  useEffect(() => {
    setTaskers([]);
    setPage(0);
    setHasMore(true);
  }, [serviceId, debouncedSearch]);

  useEffect(() => {
    let cancelled = false;
    if (!serviceId) return;

    setLoading(true);
    setError("");

    fetchTaskers({
      serviceId,
      page,
      size: PAGE_SIZE,
      searchTerm: debouncedSearch,
      filters,
      sortOption,
    })
      .then((response) => {
        if (cancelled) return;
        const enriched = response.data.map((tasker) =>
          tasker.serviceId ? tasker : { ...tasker, serviceId },
        );
        setTaskers((previous) => (page === 0 ? enriched : [...previous, ...enriched]));
        setHasMore(response.hasMore);
      })
      .catch(() => {
        if (!cancelled) {
          setError("We hit a snag loading taskers. Please retry.");
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [
    serviceId,
    page,
    debouncedSearch,
    filters.rate,
    filters.availability,
    filters.rating,
    filters.location,
    sortOption,
  ]);

  const observerRef = useRef(null);
  const sentinelRef = useRef(null);

  useEffect(() => {
    if (!sentinelRef.current) return () => {};

    if (observerRef.current) {
      observerRef.current.disconnect();
    }

    observerRef.current = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;
        if (entry.isIntersecting && hasMore && !loading) {
          setPage((prev) => prev + 1);
        }
      },
      { rootMargin: "200px 0px 200px 0px" },
    );

    observerRef.current.observe(sentinelRef.current);

    return () => {
      if (observerRef.current) {
        observerRef.current.disconnect();
      }
    };
  }, [hasMore, loading]);

  const resetPagination = () => {
    setTaskers([]);
    setPage(0);
    setHasMore(true);
  };

  const updateFilters = (partial) => {
    resetPagination();
    setFilters((previous) => ({ ...previous, ...partial }));
  };

  const showEmptyState = !loading && taskers.length === 0;

  if (!service && servicesLoading) {
    return <main className="page">Loading services…</main>;
  }

  if (!service && servicesError) {
    return <main className="page">{servicesError}</main>;
  }

  if (!service) {
    return <main className="page">We couldn't find that service.</main>;
  }

  return (
    <main className="page">
      <header className="section-title">
        <div>
          <p className="section-kicker">Tasker discovery</p>
          <h1 className="section-heading">
            {service ? `${service.serviceName} taskers` : "Find taskers"}
          </h1>
          {service && <p className="tasker-card__meta">{service.description}</p>}
        </div>
      </header>

      <input
        className="search-input"
        type="search"
        placeholder="Search taskers by name or skill…"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
      />

      <section className="filters-bar">
        <select value={filters.rate} onChange={(event) => updateFilters({ rate: event.target.value })}>
          <option value="any">Hourly rate • Any</option>
          <option value="low">Under $35/hr</option>
          <option value="medium">$35-$55/hr</option>
          <option value="premium">$55+/hr</option>
        </select>

        <select
          value={filters.availability}
          onChange={(event) => updateFilters({ availability: event.target.value })}
        >
          <option value="any">Availability • Any</option>
          <option value="available">Available</option>
          <option value="unavailable">Unavailable</option>
        </select>

        <select
          value={filters.rating}
          onChange={(event) => updateFilters({ rating: event.target.value })}
        >
          <option value="any">Rating • Any</option>
          <option value="3.5">3.5 ★ & up</option>
          <option value="4">4.0 ★ & up</option>
          <option value="4.5">4.5 ★ & up</option>
          <option value="4.8">4.8 ★ & up</option>
          <option value="5">5.0 ★ </option>
        </select>

        <input
          type="text"
          value={filters.location}
          onChange={(event) => updateFilters({ location: event.target.value })}
          placeholder="Filter by location"
        />

        <select
          value={sortOption}
          onChange={(event) => {
            resetPagination();
            setSortOption(event.target.value);
          }}
        >
          <option value="rating-desc">Sort • Highest Rated</option>
          <option value="price-asc">Sort • Lowest Price</option>
          <option value="price-desc">Sort • Highest Price</option>
          <option value="rating-asc">Sort • Lowest Rated</option>
        </select>
      </section>

      {error && <div className="no-results">{error}</div>}

      {showEmptyState ? (
        <div className="empty-state">No matching taskers found.</div>
      ) : (
        <div className="grid grid--taskers">
          {taskers.map((tasker) => (
            <TaskerCard key={tasker.id} tasker={tasker} service={service} />
          ))}
        </div>
      )}

      {(loading || hasMore) && (
        <div ref={sentinelRef} className="load-indicator">
          {loading ? "Loading taskers…" : "Scroll for more taskers"}
        </div>
      )}
    </main>
  );
}

export default TaskerDiscoveryPage;
