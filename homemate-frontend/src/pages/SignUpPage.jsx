import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { GoogleLogin as GoogleOAuthLogin } from "@react-oauth/google";
import { signupUser, signupTasker } from "../api/signupApi";
import { useAuth } from "../contexts/AuthContext";
import { fetchServices } from "../api/servicesApi";
import Modal from "../components/Modal";
import ServiceCard from "../components/ServiceCard";

function SignUpPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { signup } = useAuth();
  const [userType, setUserType] = useState(null); // null, "user", or "tasker"
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [services, setServices] = useState([]);
  const [isServiceModalOpen, setIsServiceModalOpen] = useState(false);
  const [userEmailLocked, setUserEmailLocked] = useState(false);
  const [taskerEmailLocked, setTaskerEmailLocked] = useState(false);
  const [verifyToken, setVerifyToken] = useState(null); // Store verification token
  const [showUserPassword, setShowUserPassword] = useState(false);
  const [showUserConfirmPassword, setShowUserConfirmPassword] = useState(false);
  const [showTaskerPassword, setShowTaskerPassword] = useState(false);
  const [showTaskerConfirmPassword, setShowTaskerConfirmPassword] = useState(false);

  // Load persisted verify token on mount (e.g., after refresh)
  useEffect(() => {
    const storedToken = localStorage.getItem("verify_token");
    if (storedToken) {
      console.log("Loaded verify_token from localStorage:", storedToken);
      setVerifyToken(storedToken);
    } else {
      console.warn("No verify_token found in localStorage");
    }
    const storedEmail = localStorage.getItem("verified_email");
    const storedUserType = localStorage.getItem("verified_user_type");

    if (storedUserType && !userType) {
      setUserType(storedUserType);
    }

    if (storedEmail && storedUserType) {
      if (storedUserType === "user") {
        setUserForm((prev) => ({
          ...prev,
          email: storedEmail,
        }));
        setUserEmailLocked(true);
      } else if (storedUserType === "tasker") {
        setTaskerForm((prev) => ({
          ...prev,
          email: storedEmail,
        }));
        setTaskerEmailLocked(true);
      }
    }
  }, []);

  // User form fields
  const [userForm, setUserForm] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    birthDate: "",
    gender: "",
    phone: "",
  });

  // Tasker form fields
  const [taskerForm, setTaskerForm] = useState({
    username: "",
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
    phoneNumber: "",
    dateOfBirth: "",
    bio: "",
    serviceID: "",
    hourRate: "",
    city: "",
    profileImage: null,
  });

  const decodeGoogleToken = (token) => {
    if (!token) return null;
    try {
      const payload = token.split(".")[1];
      const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
      const decoded = JSON.parse(atob(base64));
      console.log(decoded);
      return decoded;
    } catch {
      return null;
    }
  };

  // Handle location state (verified email or Google signup)
  useEffect(() => {
    if (location.state) {
      const { userType: stateUserType, verifiedEmail, verifyToken: stateVerifyToken, googleData } = location.state;
      
      // Set user type if provided
      if (stateUserType) {
        setUserType(stateUserType);
      }

      // Handle verified email (from OTP or Google)
      if (verifiedEmail && stateVerifyToken) {
        setVerifyToken(stateVerifyToken);
        localStorage.setItem("verify_token", stateVerifyToken);
        if (verifiedEmail) {
          localStorage.setItem("verified_email", verifiedEmail);
        }
        if (stateUserType) {
          localStorage.setItem("verified_user_type", stateUserType);
        }
        
        if (stateUserType === "user") {
          setUserForm(prev => ({
            ...prev,
            email: verifiedEmail,
            firstName: googleData?.firstName || prev.firstName,
            lastName: googleData?.lastName || prev.lastName,
            username: googleData?.username || prev.username,
          }));
          setUserEmailLocked(true);
        } else if (stateUserType === "tasker") {
          setTaskerForm(prev => ({
            ...prev,
            email: verifiedEmail,
            firstName: googleData?.firstName || prev.firstName,
            lastName: googleData?.lastName || prev.lastName,
            username: googleData?.username || prev.username,
          }));
          setTaskerEmailLocked(true);
        }
      }
    }
  }, [location.state]);

  // Load services when tasker is selected
  useEffect(() => {
    if (userType === "tasker") {
      fetchServices()
        .then((data) => setServices(data))
        .catch(() => setServices([]));
    }
  }, [userType]);

  const handleUserTypeSelect = (type) => {
    // Navigate to signup method choice page
    navigate("/signup/method", {
      state: { userType: type },
    });
  };

  const handleUserFormChange = (e) => {
    setUserForm({
      ...userForm,
      [e.target.name]: e.target.value,
    });
  };

  const handleTaskerFormChange = (e) => {
    if (e.target.name === "profileImage") {
      setTaskerForm({
        ...taskerForm,
        profileImage: e.target.files[0] || null,
      });
    } else {
      setTaskerForm({
        ...taskerForm,
        [e.target.name]: e.target.value,
      });
    }
  };

  const handleUserSignup = async (e) => {
    e.preventDefault();
    setError("");

    if (userForm.password !== userForm.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    const tokenToUse = verifyToken || localStorage.getItem("verify_token");
    if (!tokenToUse) {
      setError("Please verify your email first.");
      return;
    }
    console.log("User signup - Token to use:", tokenToUse.substring(0, 20) + "...");

    setIsLoading(true);

    try {
      // Convert birthDate to Timestamp format (ISO string)
      const birthDateTimestamp = userForm.birthDate
        ? new Date(userForm.birthDate).toISOString()
        : null;

      const token = await signupUser({
        username: userForm.username,
        firstName: userForm.firstName,
        lastName: userForm.lastName,
        email: userForm.email,
        password: userForm.password,
        birthDate: birthDateTimestamp,
        gender: userForm.gender || null,
        phone: userForm.phone,
        verifyToken: tokenToUse, // Always include verification token
      });

      // Save with ROLE_USER
      signup(token, "ROLE_USER");
      navigate("/");
    } catch (err) {
      setError(err.message || "Signup failed. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleTaskerSignup = async (e) => {
    e.preventDefault();
    setError("");

    if (taskerForm.password !== taskerForm.confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    const tokenToUse = verifyToken || localStorage.getItem("verify_token");
    if (!tokenToUse) {
      setError("Please verify your email first.");
      return;
    }
    console.log("Tasker signup - Token to use:", tokenToUse.substring(0, 20) + "...");

    setIsLoading(true);

    try {
      const token = await signupTasker({
        username: taskerForm.username,
        firstName: taskerForm.firstName,
        lastName: taskerForm.lastName,
        email: taskerForm.email,
        password: taskerForm.password,
        phoneNumber: taskerForm.phoneNumber,
        dateOfBirth: taskerForm.dateOfBirth,
        bio: taskerForm.bio,
        serviceID: taskerForm.serviceID ? Number(taskerForm.serviceID) : null,
        hourRate: taskerForm.hourRate ? Number(taskerForm.hourRate) : null,
        city: taskerForm.city,
        profileImage: taskerForm.profileImage,
        verifyToken: tokenToUse, // Always include verification token
      });

      // Save with ROLE_TASKER
      signup(token, "ROLE_TASKER");
      navigate("/");
    } catch (err) {
      setError(err.message || "Signup failed. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoogleUserSignup = (credentialResponse) => {
    const decoded = decodeGoogleToken(credentialResponse?.credential);
    if (!decoded) {
      setError("Failed to read Google account information. Please try again.");
      return;
    }

    const email = decoded.email || "";
    const firstName = decoded.given_name || "";
    const lastName = decoded.family_name || "";
    const usernameFromEmail = email ? email.split("@")[0] : "";

    setUserForm((prev) => ({
      ...prev,
      email: email || prev.email,
      firstName: firstName || prev.firstName,
      lastName: lastName || prev.lastName,
      username: usernameFromEmail || prev.username,
    }));

    if (email) {
      setUserEmailLocked(true);
    }
  };

  const handleGoogleTaskerSignup = (credentialResponse) => {
    const decoded = decodeGoogleToken(credentialResponse?.credential);
    if (!decoded) {
      setError("Failed to read Google account information. Please try again.");
      return;
    }

    const email = decoded.email || "";
    const firstName = decoded.given_name || "";
    const lastName = decoded.family_name || "";
    const usernameFromEmail = email ? email.split("@")[0] : "";

    setTaskerForm((prev) => ({
      ...prev,
      email: email || prev.email,
      firstName: firstName || prev.firstName,
      lastName: lastName || prev.lastName,
      username: usernameFromEmail || prev.username,
    }));

    if (email) {
      setTaskerEmailLocked(true);
    }
  };

  const handleGoogleSignupError = () => {
    setError("Failed to sign up with Google. Please try again.");
  };

  const handleOpenServiceModal = () => {
    setIsServiceModalOpen(true);
  };

  const handleCloseServiceModal = () => {
    setIsServiceModalOpen(false);
  };

  const handleSelectServiceForTasker = (service) => {
    setTaskerForm((prev) => ({
      ...prev,
      serviceID: service.serviceId ? String(service.serviceId) : "",
    }));
    setIsServiceModalOpen(false);
  };

  if (!userType) {
    return (
      <main className="page page--signup">
        <div className="signin-container">
          <div className="signin-card" style={{ maxWidth: "540px" }}>
            <h1 className="signin-title">Join HomeMate</h1>
            <p className="signin-subtitle">Choose how you want to use HomeMate</p>

            <div className="user-type-selection" style={{ display: "flex", gap: "16px", flexDirection: "column" }}>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => handleUserTypeSelect("user")}
                style={{ padding: "20px", fontSize: "18px" }}
              >
                I want to request tasks
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => handleUserTypeSelect("tasker")}
                style={{ padding: "20px", fontSize: "18px" }}
              >
                I want to be a tasker
              </button>
            </div>

            <div style={{ marginTop: "24px", textAlign: "center" }}>
              <p>
                Already have an account?{" "}
                <a href="/signin" style={{ color: "var(--primary)", textDecoration: "underline" }}>
                  Sign in
                </a>
              </p>
            </div>
          </div>
        </div>
      </main>
    );
  }

  return (
    <main className="page page--signup">
      <div className="signin-container">
        <div className="signin-card">
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "24px" }}>
            <h1 className="signin-title">
              {userType === "user" ? "Sign up as User" : "Sign up as Tasker"}
            </h1>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setUserType(null)}
              style={{ fontSize: "14px" }}
            >
              ← Back
            </button>
          </div>

          {error && (
            <div className="alert alert-error" role="alert">
              {error}
            </div>
          )}

          {userType === "user" ? (
            <>
              <form onSubmit={handleUserSignup} className="signin-form">
              <div className="form-field">
                <label htmlFor="username" className="form-label">
                  Username *
                </label>
                <input
                  id="username"
                  name="username"
                  type="text"
                  className="input"
                  value={userForm.username}
                  onChange={handleUserFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
                <div className="form-field">
                  <label htmlFor="firstName" className="form-label">
                    First Name *
                  </label>
                  <input
                    id="firstName"
                    name="firstName"
                    type="text"
                    className="input"
                    value={userForm.firstName}
                    onChange={handleUserFormChange}
                    required
                    disabled={isLoading}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="lastName" className="form-label">
                    Last Name *
                  </label>
                  <input
                    id="lastName"
                    name="lastName"
                    type="text"
                    className="input"
                    value={userForm.lastName}
                    onChange={handleUserFormChange}
                    required
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="email" className="form-label">
                  Email * {userEmailLocked && <span style={{ color: "#22c55e", fontSize: "0.875rem" }}>✓ Verified</span>}
                </label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  className="input"
                  value={userForm.email}
                  onChange={handleUserFormChange}
                  required
                  disabled={isLoading || userEmailLocked}
                  readOnly={userEmailLocked}
                  style={userEmailLocked ? { backgroundColor: "#f0fdf4", cursor: "not-allowed" } : {}}
                />
              </div>

              <div className="form-field">
                <label htmlFor="phone" className="form-label">
                  Phone *
                </label>
                <input
                  id="phone"
                  name="phone"
                  type="tel"
                  className="input"
                  value={userForm.phone}
                  onChange={handleUserFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
                <div className="form-field">
                  <label htmlFor="birthDate" className="form-label">
                    Date of Birth *
                  </label>
                  <input
                    id="birthDate"
                    name="birthDate"
                    type="date"
                    className="input"
                    value={userForm.birthDate}
                    onChange={handleUserFormChange}
                    required
                    disabled={isLoading}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="gender" className="form-label">
                    Gender
                  </label>
                  <select
                    id="gender"
                    name="gender"
                    className="input"
                    value={userForm.gender}
                    onChange={handleUserFormChange}
                    disabled={isLoading}
                  >
                    <option value="">Select</option>
                    <option value="M">Male</option>
                    <option value="F">Female</option>
                  </select>
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="password" className="form-label">
                  Password *
                </label>
                <div className="password-input-wrapper">
                  <input
                    id="password"
                    name="password"
                    type={showUserPassword ? "text" : "password"}
                    className="input"
                    value={userForm.password}
                    onChange={handleUserFormChange}
                    maxLength={72}
                    required
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowUserPassword(!showUserPassword)}
                    aria-label={showUserPassword ? "Hide password" : "Show password"}
                    disabled={isLoading}
                  >
                    {showUserPassword ? (
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
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px', textAlign: 'right' }}>
                  {userForm.password.length}/72
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="confirmPassword" className="form-label">
                  Confirm Password *
                </label>
                <div className="password-input-wrapper">
                  <input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showUserConfirmPassword ? "text" : "password"}
                    className="input"
                    value={userForm.confirmPassword}
                    onChange={handleUserFormChange}
                    maxLength={72}
                    required
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowUserConfirmPassword(!showUserConfirmPassword)}
                    aria-label={showUserConfirmPassword ? "Hide password" : "Show password"}
                    disabled={isLoading}
                  >
                    {showUserConfirmPassword ? (
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
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px', textAlign: 'right' }}>
                  {userForm.confirmPassword.length}/72
                </div>
              </div>

              <button
                type="submit"
                className="btn btn-primary signin-submit"
                disabled={isLoading}
              >
                {isLoading ? "Signing up..." : "Sign up"}
              </button>
            </form>
            </>
          ) : (
            <>
              <form onSubmit={handleTaskerSignup} className="signin-form">
              <div className="form-field">
                <label htmlFor="tasker-username" className="form-label">
                  Username *
                </label>
                <input
                  id="tasker-username"
                  name="username"
                  type="text"
                  className="input"
                  value={taskerForm.username}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
                <div className="form-field">
                  <label htmlFor="tasker-firstName" className="form-label">
                    First Name *
                  </label>
                  <input
                    id="tasker-firstName"
                    name="firstName"
                    type="text"
                    className="input"
                    value={taskerForm.firstName}
                    onChange={handleTaskerFormChange}
                    required
                    disabled={isLoading}
                  />
                </div>

                <div className="form-field">
                  <label htmlFor="tasker-lastName" className="form-label">
                    Last Name *
                  </label>
                  <input
                    id="tasker-lastName"
                    name="lastName"
                    type="text"
                    className="input"
                    value={taskerForm.lastName}
                    onChange={handleTaskerFormChange}
                    required
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="tasker-email" className="form-label">
                  Email * {taskerEmailLocked && <span style={{ color: "#22c55e", fontSize: "0.875rem" }}>✓ Verified</span>}
                </label>
                <input
                  id="tasker-email"
                  name="email"
                  type="email"
                  className="input"
                  value={taskerForm.email}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading || taskerEmailLocked}
                  readOnly={taskerEmailLocked}
                  style={taskerEmailLocked ? { backgroundColor: "#f0fdf4", cursor: "not-allowed" } : {}}
                />
              </div>

              <div className="form-field">
                <label htmlFor="tasker-phoneNumber" className="form-label">
                  Phone Number *
                </label>
                <input
                  id="tasker-phoneNumber"
                  name="phoneNumber"
                  type="tel"
                  className="input"
                  value={taskerForm.phoneNumber}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="form-field">
                <label htmlFor="tasker-dateOfBirth" className="form-label">
                  Date of Birth *
                </label>
                <input
                  id="tasker-dateOfBirth"
                  name="dateOfBirth"
                  type="date"
                  className="input"
                  value={taskerForm.dateOfBirth}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="form-field">
                <label htmlFor="tasker-city" className="form-label">
                  City *
                </label>
                <input
                  id="tasker-city"
                  name="city"
                  type="text"
                  className="input"
                  value={taskerForm.city}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "16px" }}>
                <div className="form-field">
                  <label htmlFor="tasker-serviceID" className="form-label">
                    Service *
                  </label>
                  <div style={{ display: "flex", gap: "8px" }}>
                    <input
                      id="tasker-serviceID"
                      type="text"
                      className="input"
                      value={
                        taskerForm.serviceID
                          ? services.find((s) => String(s.serviceId) === String(taskerForm.serviceID))
                              ?.serviceName || ""
                          : ""
                      }
                      placeholder="Choose a service"
                      readOnly
                      disabled={isLoading}
                    />
                    <button
                      type="button"
                      className="btn btn-secondary"
                      onClick={handleOpenServiceModal}
                      disabled={isLoading || services.length === 0}
                    >
                      Choose
                    </button>
                  </div>
                </div>

                <div className="form-field">
                  <label htmlFor="tasker-hourRate" className="form-label">
                    Hourly Rate ($) *
                  </label>
                  <input
                    id="tasker-hourRate"
                    name="hourRate"
                    type="number"
                    step="0.01"
                    min="0"
                    className="input"
                    value={taskerForm.hourRate}
                    onChange={handleTaskerFormChange}
                    required
                    disabled={isLoading}
                  />
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="tasker-bio" className="form-label">
                  Bio
                </label>
                <textarea
                  id="tasker-bio"
                  name="bio"
                  className="input"
                  rows="4"
                  value={taskerForm.bio}
                  onChange={handleTaskerFormChange}
                  disabled={isLoading}
                />
              </div>

              <div className="form-field">
                <label htmlFor="tasker-profileImage" className="form-label">
                  Profile Image
                </label>
                <input
                  id="tasker-profileImage"
                  name="profileImage"
                  type="file"
                  accept="image/*"
                  className="input"
                  onChange={handleTaskerFormChange}
                  disabled={isLoading}
                />
              </div>

              <div className="form-field">
                <label htmlFor="tasker-password" className="form-label">
                  Password *
                </label>
                <div className="password-input-wrapper">
                  <input
                    id="tasker-password"
                    name="password"
                    type={showTaskerPassword ? "text" : "password"}
                    className="input"
                    value={taskerForm.password}
                    onChange={handleTaskerFormChange}
                    maxLength={72}
                    required
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowTaskerPassword(!showTaskerPassword)}
                    aria-label={showTaskerPassword ? "Hide password" : "Show password"}
                    disabled={isLoading}
                  >
                    {showTaskerPassword ? (
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
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px', textAlign: 'right' }}>
                  {taskerForm.password.length}/72
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="tasker-confirmPassword" className="form-label">
                  Confirm Password *
                </label>
                <div className="password-input-wrapper">
                  <input
                    id="tasker-confirmPassword"
                    name="confirmPassword"
                    type={showTaskerConfirmPassword ? "text" : "password"}
                    className="input"
                    value={taskerForm.confirmPassword}
                    onChange={handleTaskerFormChange}
                    maxLength={72}
                    required
                    disabled={isLoading}
                  />
                  <button
                    type="button"
                    className="password-toggle"
                    onClick={() => setShowTaskerConfirmPassword(!showTaskerConfirmPassword)}
                    aria-label={showTaskerConfirmPassword ? "Hide password" : "Show password"}
                    disabled={isLoading}
                  >
                    {showTaskerConfirmPassword ? (
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
                <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '4px', textAlign: 'right' }}>
                  {taskerForm.confirmPassword.length}/72
                </div>
              </div>

              <button
                type="submit"
                className="btn btn-primary signin-submit"
                disabled={isLoading}
              >
                {isLoading ? "Signing up..." : "Sign up as Tasker"}
              </button>
            </form>

            {isServiceModalOpen && (
              <Modal
                title="Choose a service"
                onClose={handleCloseServiceModal}
                width={800}
              >
                <div style={{ maxHeight: "80vh", overflowY: "auto" }}>
                  {services.length === 0 ? (
                    <div className="load-indicator">Loading services…</div>
                  ) : (
                    <div className="grid grid--services">
                      {services.map((service) => (
                        <ServiceCard
                          key={service.serviceId}
                          service={service}
                          onSelect={handleSelectServiceForTasker}
                          variant="compact"
                        />
                      ))}
                    </div>
                  )}
                </div>
              </Modal>
            )}
            </>
          )}

          <div style={{ marginTop: "24px", textAlign: "center" }}>
            <p>
              Already have an account?{" "}
              <a href="/signin" style={{ color: "var(--primary)", textDecoration: "underline" }}>
                Sign in
              </a>
            </p>
          </div>
        </div>
      </div>
    </main>
  );
}

export default SignUpPage;

