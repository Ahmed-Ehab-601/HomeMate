
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import BannerLogo from "./BannerLogo";

function Header() {
  const navigate = useNavigate();
  const { isAuthenticated, isTasker, isRegularUser, isAdmin, logout } =
    useAuth();

  // Default to showing regular user menu if not authenticated
  const showUserMenu = !isAuthenticated || (isRegularUser() && !isAdmin());
  const showTaskerMenu = isAuthenticated && isTasker();
  const showAdminMenu = isAuthenticated && isAdmin();

  return (
    <header className="sticky-header">
      <div className="sticky-header__content">
        <button
          type="button"
          className="brand"
          onClick={() => navigate("/")}
          aria-label="Go to HomeMate homepage"
          style={{ padding: 0, background: "none", border: "none" }}
        >
          <BannerLogo />
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
              className={({ isActive }) =>
                `nav-link${isActive ? " active" : ""}`
              }
              to="/services"
            >
              Explore Services
            </NavLink>
          )}

          {isAuthenticated && (
            <>
              {showUserMenu && (
                <NavLink
                  className={({ isActive }) =>
                    `nav-link${isActive ? " active" : ""}`
                  }
                  to="/profile"
                >
                  Profile
                </NavLink>
              )}
              {showTaskerMenu && (
                <NavLink
                  className={({ isActive }) =>
                    `nav-link${isActive ? " active" : ""}`
                  }
                  to="/tasker/profile"
                >
                  Tasker Hub
                </NavLink>
              )}
            </>
          )}

          {showUserMenu && (
            <NavLink
              className={({ isActive }) =>
                `nav-link${isActive ? " active" : ""}`
              }
              to="/my-tasks"
            >
              My Tasks
            </NavLink>
          )}

          {showTaskerMenu && (
            <NavLink
              className={({ isActive }) =>
                `nav-link${isActive ? " active" : ""}`
              }
              to="/tasker/my-tasks"
            >
              Tasker Tasks
            </NavLink>
          )}

          {showAdminMenu && (
            <>
              <NavLink
                className={({ isActive }) =>
                  `nav-link${isActive ? " active" : ""}`
                }
                to="/admin/users"
              >
                Users
              </NavLink>
              <NavLink
                className={({ isActive }) =>
                  `nav-link${isActive ? " active" : ""}`
                }
                to="/admin/taskers"
              >
                Taskers
              </NavLink>
              <NavLink
                className={({ isActive }) =>
                  `nav-link${isActive ? " active" : ""}`
                }
                to="/admin/services"
              >
                Services
              </NavLink>
            </>
          )}

          <NavLink
            className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            to="/how-it-works"
          >
            How It Works
          </NavLink>
          <NavLink
            className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            to="/about"
          >
            About
          </NavLink>
          <NavLink
            className={({ isActive }) => `nav-link${isActive ? " active" : ""}`}
            to="/contact"
          >
            Contact
          </NavLink>
        </nav>

        <div className="header-cta">
          {isAuthenticated ? (
            <button type="button" className="btn btn-ghost" onClick={logout}>
              Sign out
            </button>
          ) : (
            <>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => navigate("/signin")}
              >
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
