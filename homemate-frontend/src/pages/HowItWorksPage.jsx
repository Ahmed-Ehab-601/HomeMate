// pages/HowItWorksPage.jsx
import "../styles/InfoPages.css";

function HowItWorksPage() {
  return (
    <main className="page info-page">
      <div className="info-hero">
        <h1 className="info-hero__title">How HomeMate Works</h1>
        <p className="info-hero__subtitle">
          Getting help for your home is easy with HomeMate
        </p>
      </div>

      <section className="info-section">
        <div className="info-content">
          <h2>For Customers</h2>
          <div className="steps-container">
            <div className="step-card">
              <div className="step-number">1</div>
              <h3>Choose a Service</h3>
              <p>
                Browse our wide range of home services or search for what you need.
                From cleaning to repairs, we've got you covered.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">2</div>
              <h3>Select a Tasker</h3>
              <p>
                View profiles, read reviews, and choose the perfect tasker for your
                job. Check their availability and pricing.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">3</div>
              <h3>Schedule & Book</h3>
              <p>
                Pick a date and time that works for you. Provide details about the
                task and confirm your booking.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">4</div>
              <h3>Get It Done</h3>
              <p>
                Your tasker arrives at the scheduled time and completes the work.
                Track progress and communicate through our platform.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">5</div>
              <h3>Rate & Review</h3>
              <p>
                After completion, rate your experience and leave a review to help
                other customers make informed decisions.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="info-section info-section--alt">
        <div className="info-content">
          <h2>For Taskers</h2>
          <div className="steps-container">
            <div className="step-card">
              <div className="step-number">1</div>
              <h3>Create Your Profile</h3>
              <p>
                Sign up as a tasker and create a professional profile showcasing
                your skills and services.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">2</div>
              <h3>Set Your Availability</h3>
              <p>
                Control your schedule by setting when you're available to take on
                tasks. Update it anytime.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">3</div>
              <h3>Receive Task Requests</h3>
              <p>
                Get notified when customers request your services. Review the details
                and accept tasks that fit your schedule.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">4</div>
              <h3>Complete the Task</h3>
              <p>
                Arrive on time, complete the work professionally, and communicate
                with customers through the platform.
              </p>
            </div>
            <div className="step-card">
              <div className="step-number">5</div>
              <h3>Get Paid</h3>
              <p>
                Receive secure payments through the platform. Build your reputation
                with great reviews.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="info-section">
        <div className="info-content">
          <h2>Key Features</h2>
          <div className="features-grid">
            <div className="feature-item">
              <span className="feature-icon">🔒</span>
              <div>
                <h4>Secure Platform</h4>
                <p>All transactions and communications are protected</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">⭐</span>
              <div>
                <h4>Rating System</h4>
                <p>Transparent ratings and reviews from real customers</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">💬</span>
              <div>
                <h4>In-App Chat</h4>
                <p>Communicate directly with taskers about your task</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">📅</span>
              <div>
                <h4>Flexible Scheduling</h4>
                <p>Book services at times that work for your schedule</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">🎯</span>
              <div>
                <h4>Task Tracking</h4>
                <p>Monitor the status of all your tasks in one place</p>
              </div>
            </div>
            <div className="feature-item">
              <span className="feature-icon">🛡️</span>
              <div>
                <h4>Verified Taskers</h4>
                <p>All taskers are verified for your peace of mind</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="info-section info-section--cta">
        <div className="info-content">
          <h2>Ready to Get Started?</h2>
          <p>
            Join thousands of satisfied customers and skilled taskers on HomeMate today.
          </p>
          <div className="info-cta-buttons">
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => (window.location.href = "/signup")}
            >
              Sign Up Now
            </button>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={() => (window.location.href = "/services")}
            >
              Browse Services
            </button>
          </div>
        </div>
      </section>
    </main>
  );
}

export default HowItWorksPage;
