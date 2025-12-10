import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { sendOtp } from "../api/otpApi";
import "../styles/VerifyEmail.css";

function EnterEmailPage() {
  const [email, setEmail] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const location = useLocation();

  // Get the flow type from location state (either 'FORGOT_PASSWORD' or 'EMAIL_VERIFICATION')
  const flowType = location.state?.flowType || "EMAIL_VERIFICATION";

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!email || !email.includes("@")) {
      setError("Please enter a valid email address");
      return;
    }

    setIsLoading(true);

    try {
      await sendOtp(email, flowType);
      
      // Navigate to OTP verification page with email and flow type
      navigate("/verify-otp", {
        state: {
          email: email,
          flowType: flowType,
        },
      });
    } catch (err) {
      setError(err.message || "Failed to send OTP. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="verify-email-container">
      <div className="verify-email-card">
        <h1 className="verify-email-title">
          {flowType === "FORGOT_PASSWORD" 
            ? "Reset Your Password" 
            : "Verify Your Email"}
        </h1>
        <p className="verify-email-description">
          {flowType === "FORGOT_PASSWORD"
            ? "Enter your email address and we'll send you a code to reset your password."
            : "Enter your email address to receive a verification code."}
        </p>

        <form onSubmit={handleSubmit} className="verify-email-form">
          <div className="form-group">
            <label htmlFor="email" className="form-label">
              Email Address
            </label>
            <input
              type="email"
              id="email"
              className="form-input"
              placeholder="example@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              disabled={isLoading}
              required
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button
            type="submit"
            className="submit-button"
            disabled={isLoading}
          >
            {isLoading ? "Sending..." : "Send Verification Code"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default EnterEmailPage;
