import { NavLink, useNavigate } from "react-router-dom";

function Header() {
  const navigate = useNavigate();

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
          <a className="nav-link" href="#how-it-works">
            How It Works
          </a>
          <a className="nav-link" href="#about">
            About
          </a>
        </nav>

        <div className="header-cta">
          <button type="button" className="btn btn-ghost">
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

