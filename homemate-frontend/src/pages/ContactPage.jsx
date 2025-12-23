// pages/ContactPage.jsx
import { useState } from "react";
import "../styles/InfoPages.css";

function ContactPage() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    subject: "",
    message: "",
  });
  const [formStatus, setFormStatus] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name === "message") {
      // Limit message to 500 characters
      setFormData((prev) => ({ ...prev, [name]: value.slice(0, 500) }));
    } else {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFormStatus(null);

    try {
      // Send email using mailto link (opens user's email client)
      const mailtoLink = `mailto:homemateservice8@gmail.com?subject=${encodeURIComponent(
        formData.subject
      )}&body=${encodeURIComponent(
        `From: ${formData.name}\nEmail: ${formData.email}\n\nMessage:\n${formData.message}`
      )}`;
      
      window.location.href = mailtoLink;
      
      setFormStatus({
        type: "success",
        message: "Opening your email client... Please send the email from there.",
      });
      
      // Clear form after a delay
      setTimeout(() => {
        setFormData({ name: "", email: "", subject: "", message: "" });
      }, 2000);
    } catch (error) {
      setFormStatus({
        type: "error",
        message: "Oops! Something went wrong. Please try again later.",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <main className="page info-page">
      <div className="info-hero">
        <h1 className="info-hero__title">Contact Us</h1>
        <p className="info-hero__subtitle">
          We'd love to hear from you. Get in touch with our team.
        </p>
      </div>

      <section className="info-section">
        <div className="info-content">
          <div className="contact-container">
            <div className="contact-info">
              <h2>Get In Touch</h2>
              <p>
                Have a question or need support? Our team is here to help you with
                anything related to HomeMate.
              </p>

              <div className="contact-methods">
                <div className="contact-method">
                  <div className="contact-method__icon">📧</div>
                  <div>
                    <h4>Email Us</h4>
                    <p>homemateservice8@gmail.com</p>
                  </div>
                </div>
                <div className="contact-method">
                  <div className="contact-method__icon">📞</div>
                  <div>
                    <h4>Call Us</h4>
                    <p>+1 (555) 123-4567</p>
                  </div>
                </div>
                <div className="contact-method">
                  <div className="contact-method__icon">🕐</div>
                  <div>
                    <h4>Business Hours</h4>
                    <p>Monday - Friday: 9am - 6pm</p>
                    <p>Saturday - Sunday: 10am - 4pm</p>
                  </div>
                </div>
                <div className="contact-method">
                  <div className="contact-method__icon">📍</div>
                  <div>
                    <h4>Visit Us</h4>
                    <p>123 Service Street</p>
                    <p>City, State 12345</p>
                  </div>
                </div>
              </div>

              <div className="contact-faq-link">
                <h4>Looking for quick answers?</h4>
                <p>
                  Check out our <a href="/how-it-works">How It Works</a> page for common
                  questions about using HomeMate.
                </p>
              </div>
            </div>

            <div className="contact-form-wrapper">
              <h3>Send Us a Message</h3>
              <form className="contact-form" onSubmit={handleSubmit}>
                {formStatus && (
                  <div className={`form-status form-status--${formStatus.type}`}>
                    {formStatus.message}
                  </div>
                )}

                <div className="form-group">
                  <label htmlFor="name">Your Name *</label>
                  <input
                    type="text"
                    id="name"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    placeholder="John Doe"
                    disabled={isSubmitting}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="email">Your Email *</label>
                  <input
                    type="email"
                    id="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    placeholder="john@example.com"
                    disabled={isSubmitting}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="subject">Subject *</label>
                  <input
                    type="text"
                    id="subject"
                    name="subject"
                    value={formData.subject}
                    onChange={handleChange}
                    required
                    placeholder="How can we help?"
                    disabled={isSubmitting}
                  />
                </div>

                <div className="form-group">
                  <label htmlFor="message">Message * (max 500 characters)</label>
                  <textarea
                    id="message"
                    name="message"
                    value={formData.message}
                    onChange={handleChange}
                    required
                    rows="6"
                    placeholder="Tell us more about your inquiry..."
                    disabled={isSubmitting}
                    maxLength={500}
                  />
                  <div style={{ textAlign: 'right', fontSize: '0.9em', color: formData.message.length === 500 ? 'red' : '#666' }}>
                    {formData.message.length}/500
                  </div>
                </div>

                <button
                  type="submit"
                  className="btn btn-primary btn-full"
                  disabled={isSubmitting}
                >
                  {isSubmitting ? "Sending..." : "Send Message"}
                </button>
              </form>
            </div>
          </div>
        </div>
      </section>
    </main>
  );
}

export default ContactPage;
