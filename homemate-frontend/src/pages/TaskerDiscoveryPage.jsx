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
    minHourlyRate: 0,
    maxHourlyRate: 500,
    availability: "any",
    minRating: 0,
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
          tasker.serviceId ? tasker : { ...tasker, serviceId }
        );
        setTaskers((previous) =>
          page === 0 ? enriched : [...previous, ...enriched]
        );
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
    filters.minHourlyRate,
    filters.maxHourlyRate,
    filters.availability,
    filters.minRating,
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
      { rootMargin: "200px 0px 200px 0px" }
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
    setFilters((previous) => ({ ...previous, ...partial }));
    setTaskers([]);
    setPage(0);
    setHasMore(true);
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
          {service && (
            <p className="tasker-card__meta">{service.description}</p>
          )}
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
        <input
          type="number"
          min="0"
          max="500"
          value={filters.minHourlyRate}
          onChange={(e) =>
            updateFilters({
              minHourlyRate: Math.max(
                0,
                Math.min(500, Number(e.target.value) || 0)
              ),
            })
          }
          placeholder="Min Hourly Rate ($)"
        />

        <input
          type="number"
          min="0"
          max="500"
          value={filters.maxHourlyRate}
          onChange={(e) =>
            updateFilters({
              maxHourlyRate: Math.max(
                0,
                Math.min(500, Number(e.target.value) || 0)
              ),
            })
          }
          placeholder="Max Hourly Rate ($)"
        />

        <input
          type="number"
          min="0"
          max="5"
          step="0.5"
          value={filters.minRating}
          onChange={(e) =>
            updateFilters({
              minRating: Math.max(0, Math.min(5, Number(e.target.value) || 0)),
            })
          }
          placeholder="Min Rating (0-5 ★)"
        />

        <select
          value={filters.availability}
          onChange={(event) =>
            updateFilters({ availability: event.target.value })
          }
        >
          <option value="any">Availability • Any</option>
          <option value="available">Available</option>
          <option value="unavailable">Unavailable</option>
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
