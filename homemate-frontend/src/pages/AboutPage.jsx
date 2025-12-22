// pages/AboutPage.jsx
import "../styles/InfoPages.css";

function AboutPage() {
  return (
    <main className="page info-page">
      <div className="info-hero">
        <h1 className="info-hero__title">About HomeMate</h1>
        <p className="info-hero__subtitle">
          Connecting communities through trusted home services
        </p>
      </div>

      <section className="info-section">
        <div className="info-content">
          <h2>Our Mission</h2>
          <p>
            HomeMate was founded with a simple mission: to make home services accessible,
            reliable, and convenient for everyone. We believe that finding help for your
            home should be as easy as a few clicks.
          </p>
        </div>
      </section>

      <section className="info-section info-section--alt">
        <div className="info-content">
          <h2>What We Do</h2>
          <div className="info-grid">
            <div className="info-card">
              <div className="info-card__icon">🏠</div>
              <h3>Home Services</h3>
              <p>
                From cleaning and repairs to moving and assembly, we connect you with
                skilled professionals for all your home service needs.
              </p>
            </div>
            <div className="info-card">
              <div className="info-card__icon">👥</div>
              <h3>Trusted Taskers</h3>
              <p>
                Every tasker on our platform is carefully vetted to ensure quality service
                and reliability for our customers.
              </p>
            </div>
            <div className="info-card">
              <div className="info-card__icon">⚡</div>
              <h3>Quick Booking</h3>
              <p>
                Book services in minutes, schedule at your convenience, and track everything
                from your dashboard.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="info-section">
        <div className="info-content">
          <h2>Why Choose HomeMate?</h2>
          <ul className="info-list">
            <li>
              <strong>Quality Assurance:</strong> All taskers are verified and rated by real
              customers
            </li>
            <li>
              <strong>Transparent Pricing:</strong> Clear pricing with no hidden fees
            </li>
            <li>
              <strong>Flexible Scheduling:</strong> Book services at times that work for you
            </li>
            <li>
              <strong>Secure Payments:</strong> Safe and secure payment processing
            </li>
            <li>
              <strong>24/7 Support:</strong> Our support team is always here to help
            </li>
          </ul>
        </div>
      </section>

      <section className="info-section info-section--cta">
        <div className="info-content">
          <h2>Join Our Community</h2>
          <p>
            Whether you're looking for help with your home or want to offer your skills as a
            tasker, HomeMate is the platform for you.
          </p>
          <div className="info-cta-buttons">
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => (window.location.href = "/signup")}
            >
              Get Started
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => (window.location.href = "/how-it-works")}
            >
              Learn How It Works
            </button>
          </div>
        </div>
      </section>
    </main>
  );
}

export default AboutPage;
