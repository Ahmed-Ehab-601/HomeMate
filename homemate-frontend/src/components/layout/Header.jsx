import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, isTasker, isRegularUser, logout } = useAuth();

  // Default to showing regular user menu if not authenticated
  const showUserMenu = !isAuthenticated || isRegularUser();
  const showTaskerMenu = isAuthenticated && isTasker();

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
          <NavLink
            className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            to="/"
          >
            Home
          </NavLink>
          
          {showUserMenu && (
            <NavLink
              className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
              to="/services"
            >
              Explore Services
            </NavLink>
          )}

          {isAuthenticated && (
            <NavLink
              className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
              to="/profile"
            >
              Profile
            </NavLink>
          )}

          {showUserMenu && (
            <NavLink
              className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
              to="/my-tasks"
            >
              My Tasks
            </NavLink>
          )}

          {showTaskerMenu && (
            <NavLink
              className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
              to="/tasker/my-tasks"
            >
              Tasker Tasks
            </NavLink>
          )}

          <a className="nav-link" href="#how-it-works">
            How It Works
          </a>
          <a className="nav-link" href="#about">
            About
          </a>
        </nav>

        <div className="header-cta">
          {isAuthenticated ? (
            <button type="button" className="btn btn-ghost" onClick={logout}>
              Sign out
            </button>
          ) : (
            <>
              <button type="button" className="btn btn-ghost" onClick={() => navigate("/signin")}>
                Sign in
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => navigate("/signup")}
              >
                Get Started
              </button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}

export default Header;
