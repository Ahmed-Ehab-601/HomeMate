import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { verifyOtp, sendOtp } from "../api/otpApi";
import "../styles/VerifyEmail.css";

function VerifyOtpPage() {
  const [otp, setOtp] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [canResend, setCanResend] = useState(false);
  const [resendTimer, setResendTimer] = useState(60);
  const navigate = useNavigate();
  const location = useLocation();

  // Get email and flow type from location state
  const email = location.state?.email;
  const flowType = location.state?.flowType || "EMAIL_VERIFICATION";

  useEffect(() => {
    // Redirect if no email provided
    if (!email) {
      navigate("/verify-email");
      return;
    }

    // Start countdown timer for resend
    const timer = setInterval(() => {
      setResendTimer((prev) => {
        if (prev <= 1) {
          setCanResend(true);
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [email, navigate]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccessMessage("");

    if (!otp || otp.length < 4) {
      setError("Please enter a valid OTP code");
      return;
    }

    setIsLoading(true);

    try {
      const result = await verifyOtp(email, otp, flowType);

      if (result.success) {
        setSuccessMessage("Verification successful!");

        // Store the verification token if provided
        if (result.token) {
          localStorage.setItem("verify_token", result.token);
        }

        // Navigate based on flow type
        setTimeout(() => {
          if (flowType === "FORGOT_PASSWORD") {
            // Navigate to reset password page
            navigate("/reset-password", {
              state: {
                email: email,
                token: result.token,
              },
            });
          } else if (flowType === "EMAIL_VERIFICATION") {
            // Navigate to complete signup page (could be profile setup or signin)
            navigate("/signup/complete", {
              state: {
                email: email,
                token: result.token,
              },
            });
          }
        }, 1500);
      } else {
        setError(result.message || "Invalid OTP code");
      }
    } catch (err) {
      setError(err.message || "Verification failed. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleResendOtp = async () => {
    if (!canResend) return;

    setError("");
    setSuccessMessage("");
    setIsLoading(true);

    try {
      await sendOtp(email, flowType);
      setSuccessMessage("New OTP code sent to your email!");
      setCanResend(false);
      setResendTimer(60);

      // Restart timer
      const timer = setInterval(() => {
        setResendTimer((prev) => {
          if (prev <= 1) {
            setCanResend(true);
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch (err) {
      setError(err.message || "Failed to resend OTP");
    } finally {
      setIsLoading(false);
    }
  };

  const handleOtpChange = (e) => {
    const value = e.target.value.replace(/\D/g, ""); // Only allow digits
    setOtp(value);
  };

  return (
    <div className="verify-email-container">
      <div className="verify-email-card">
        <h1 className="verify-email-title">Enter Verification Code</h1>
        <p className="verify-email-description">
          We've sent a verification code to <strong>{email}</strong>
        </p>

        <form onSubmit={handleSubmit} className="verify-email-form">
          <div className="form-group">
            <label htmlFor="otp" className="form-label">
              Verification Code
            </label>
            <input
              type="text"
              id="otp"
              className="form-input otp-input"
              placeholder="Enter 6-digit code"
              value={otp}
              onChange={handleOtpChange}
              maxLength={6}
              disabled={isLoading}
              required
            />
          </div>

          {error && <div className="error-message">{error}</div>}
          {successMessage && (
            <div className="success-message">{successMessage}</div>
          )}

          <button
            type="submit"
            className="submit-button"
            disabled={isLoading || !otp}
          >
            {isLoading ? "Verifying..." : "Verify Code"}
          </button>
        </form>

        <div className="resend-section">
          {canResend ? (
            <button
              onClick={handleResendOtp}
              className="resend-button"
              disabled={isLoading}
            >
              Resend Code
            </button>
          ) : (
            <p className="resend-timer">
              Resend code in {resendTimer} seconds
            </p>
          )}
        </div>

        <div className="back-link">
          <button
            onClick={() => navigate("/verify-email", { state: { flowType } })}
            className="back-button"
            disabled={isLoading}
          >
            ← Change Email
          </button>
        </div>
      </div>
    </div>
  );
}

export default VerifyOtpPage;
