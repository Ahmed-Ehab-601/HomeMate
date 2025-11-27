import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authApi";
import { useAuth } from "../contexts/AuthContext";
import GoogleLogin from "../components/GoogleLogin";

function SignInPage() {
  const navigate = useNavigate();
  const { login: loginUser } = useAuth();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isGoogleLoading, setIsGoogleLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      const response = await login(email, password);
      // Store user data in AuthContext
      loginUser(response);



      // Redirect based on role
      if (response.role === "ROLE_ADMIN") {
        navigate("/admin/users");
      } else if (response.role === "ROLE_TASKER") {
        navigate("/tasker/profile");
      } else if (response.role === "SUSPENDED"){
        setError("User is suspended");
      } else {
        navigate("/");
      }
    } catch (err) {
      setError("Invalid email or password");
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleLoginSuccess = (response) => {
    // Store user data in AuthContext
    loginUser(response);

    if (response.role === "ROLE_ADMIN") {
      navigate("/admin/users");
    } else if (response.role === "ROLE_TASKER") {
      navigate("/tasker/profile");
    } else {
      navigate("/");
    }
  };

  const handleGoogleLoginError = () => {
    setError("Failed to sign in with Google. Please try again.");
  };

  return (
    <main className="page page--signin">
      <div className="signin-container">
        <div className="signin-card" style={{ maxWidth: "540px" }}>
          <h1 className="signin-title">Sign in to HomeMate</h1>
          <p className="signin-subtitle">
            Welcome back! Please enter your details.
          </p>

          {error && (
            <div className="alert alert-error" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="signin-form">
            <div className="form-field">
              <label htmlFor="email" className="form-label">
                Email
              </label>
              <input
                id="email"
                type="email"
                className="input"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
                required
                disabled={isLoading || isGoogleLoading}
              />
            </div>

            <div className="form-field">
              <label htmlFor="password" className="form-label">
                Password
              </label>
              <div className="password-input-wrapper">
                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  className="input"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  required
                  disabled={isLoading || isGoogleLoading}
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                  disabled={isLoading || isGoogleLoading}
                >
                  {showPassword ? (
                    <svg
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                      <line x1="1" y1="1" x2="23" y2="23"></line>
                    </svg>
                  ) : (
                    <svg
                      width="20"
                      height="20"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                      <circle cx="12" cy="12" r="3"></circle>
                    </svg>
                  )}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary signin-submit"
              disabled={isLoading || isGoogleLoading}
            >
              {isLoading ? "Signing in..." : "Sign in"}
            </button>
            <GoogleLogin
              onSuccess={handleGoogleLoginSuccess}
              onError={handleGoogleLoginError}
              disabled={isLoading || isGoogleLoading}
            />
          </form>

          <div style={{ marginTop: "24px", textAlign: "center" }}>
            <p>
              Don't have an account?{" "}
              <a
                href="/signup"
                style={{ color: "var(--primary)", textDecoration: "underline" }}
              >
                Sign up
              </a>
            </p>
          </div>
        </div>
      </div>
    </main>
  );
}

export default SignInPage;
