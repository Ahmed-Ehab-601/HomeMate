import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();

  return (
    <header className="sticky-header">
      <div className="sticky-header__content">
        <button
          type="button"
          className="brand"
          onClick={() => navigate("/")}
          aria-label="Go to HomeMate homepage"
        >
          <span className="brand__mark">HM</span>
          HomeMate
        </button>

        <nav className="nav-links">
          <NavLink className={({ isActive }) => `nav-link${isActive ? " active" : ""}`} to="/">
            Home
          </NavLink>
          <NavLink
            className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            to="/services"
          >
            Explore Services
          </NavLink>
          {isAuthenticated && (
            <>
              {user?.role !== "ROLE_TASKER" && (
                <NavLink
                  className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
                  to="/profile"
                >
                  Profile
                </NavLink>
              )}
              {user?.role === "ROLE_TASKER" && (
                <NavLink
                  className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
                  to="/tasker/profile"
                >
                  Tasker Hub
                </NavLink>
              )}
            </>
          )}
          <a className="nav-link" href="#how-it-works">
            How It Works
          </a>
          <a className="nav-link" href="#about">
            About
          </a>
        </nav>

        <div className="header-cta">
          <button type="button" className="btn btn-ghost" onClick={() => navigate("/signin")}>
            Sign in
          </button>
          <button type="button" className="btn btn-primary" onClick={() => navigate("/services")}>
            Get Started
          </button>
        </div>
      </div>
    </header>
  );
}

export default Header;

