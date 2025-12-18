import { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { GoogleLogin as GoogleOAuthLogin } from "@react-oauth/google";
import { initGoogleSignup } from "../api/signupApi";
import "../styles/VerifyEmail.css";

function SignupMethodChoicePage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  // Get user type from location state (either "user" or "tasker")
  const userType = location.state?.userType || "user";

  const handleEmailVerificationSignup = () => {
    // Navigate to email entry page for verification
    navigate("/verify-email", {
      state: {
        flowType: "EMAIL_VERIFICATION",
        userType: userType,
      },
    });
  };

  const handleGoogleSignup = async (credentialResponse) => {
    setIsLoading(true);
    setError("");

    try {
      // Call backend to validate Google token and get user details + verify token
      const googleData = await initGoogleSignup(
        credentialResponse?.credential,
        userType
      );

      // Persist verify token for later signup request
      if (googleData?.verifyToken) {
        localStorage.setItem("verify_token", googleData.verifyToken);
      }

      // Persist verified email and user type for hydration
      if (googleData?.email) {
        localStorage.setItem("verified_email", googleData.email);
      }
      if (userType) {
        localStorage.setItem("verified_user_type", userType);
      }

      // Navigate to signup page with Google data and verify token
      navigate("/signup", {
        state: {
          userType: userType,
          verifiedEmail: googleData.email,
          verifyToken: googleData.verifyToken,
          googleData: {
            firstName: googleData.firstName,
            lastName: googleData.lastName,
            username: googleData.username,
          },
        },
      });
    } catch (err) {
      setError(err.message || "Failed to sign up with Google. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleSignupError = () => {
    setError("Failed to sign up with Google. Please try again.");
  };

  return (
    <div className="verify-email-container">
      <div className="verify-email-card">
        <h1 className="verify-email-title">
          Sign Up as {userType === "tasker" ? "Tasker" : "User"}
        </h1>
        <p className="verify-email-description">
          Choose how you want to create your account
        </p>

        {error && <div className="error-message">{error}</div>}

        <div className="signup-method-options">
          {/* Google Signup Option */}
          <div className="signup-method-section">
            <h3 className="method-title">Continue with Google</h3>
            <p className="method-description">
              Quick signup using your Google account
            </p>
            <div className="google-button-wrapper">
              <GoogleOAuthLogin
                onSuccess={handleGoogleSignup}
                onError={handleGoogleSignupError}
                text="signup_with"
                theme="outline"
                size="large"
                width="100%"
                disabled={isLoading}
              />
            </div>
          </div>

          <div className="divider-section">
            <div className="divider-line"></div>
            <span className="divider-text">OR</span>
            <div className="divider-line"></div>
          </div>

          {/* Email Verification Option */}
          <div className="signup-method-section">
            <h3 className="method-title">Sign Up with Email</h3>
            <p className="method-description">
              Verify your email address and create your account
            </p>
            <button
              onClick={handleEmailVerificationSignup}
              className="submit-button"
            >
              Continue with Email Verification
            </button>
          </div>
        </div>

        <div className="back-link">
          <button
            onClick={() => navigate("/signup")}
            className="back-button"
          >
            ← Back to User Type Selection
          </button>
        </div>
      </div>
    </div>
  );
}

export default SignupMethodChoicePage;
