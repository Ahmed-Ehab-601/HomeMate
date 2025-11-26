import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { signupUser, signupTasker } from "../api/signupApi";
import { useAuth } from "../contexts/AuthContext";
import { fetchServices } from "../api/servicesApi";

function SignUpPage() {
  const navigate = useNavigate();
  const { signup } = useAuth();
  const [userType, setUserType] = useState(null); // null, "user", or "tasker"
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [services, setServices] = useState([]);

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

  // Load services when tasker is selected
  useEffect(() => {
    if (userType === "tasker") {
      fetchServices()
        .then((data) => setServices(data))
        .catch(() => setServices([]));
    }
  }, [userType]);

  const handleUserTypeSelect = (type) => {
    setUserType(type);
    setError("");
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

  if (!userType) {
    return (
      <main className="page page--signup">
        <div className="signin-container">
          <div className="signin-card">
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
                  Email *
                </label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  className="input"
                  value={userForm.email}
                  onChange={handleUserFormChange}
                  required
                  disabled={isLoading}
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
                    <option value="O">Other</option>
                  </select>
                </div>
              </div>

              <div className="form-field">
                <label htmlFor="password" className="form-label">
                  Password *
                </label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  className="input"
                  value={userForm.password}
                  onChange={handleUserFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="form-field">
                <label htmlFor="confirmPassword" className="form-label">
                  Confirm Password *
                </label>
                <input
                  id="confirmPassword"
                  name="confirmPassword"
                  type="password"
                  className="input"
                  value={userForm.confirmPassword}
                  onChange={handleUserFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary signin-submit"
                disabled={isLoading}
              >
                {isLoading ? "Signing up..." : "Sign up"}
              </button>
            </form>
          ) : (
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
                  Email *
                </label>
                <input
                  id="tasker-email"
                  name="email"
                  type="email"
                  className="input"
                  value={taskerForm.email}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading}
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
                  <select
                    id="tasker-serviceID"
                    name="serviceID"
                    className="input"
                    value={taskerForm.serviceID}
                    onChange={handleTaskerFormChange}
                    required
                    disabled={isLoading}
                  >
                    <option value="">Select a service</option>
                    {services.map((service) => (
                      <option key={service.serviceId} value={service.serviceId}>
                        {service.serviceName}
                      </option>
                    ))}
                  </select>
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
                <input
                  id="tasker-password"
                  name="password"
                  type="password"
                  className="input"
                  value={taskerForm.password}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <div className="form-field">
                <label htmlFor="tasker-confirmPassword" className="form-label">
                  Confirm Password *
                </label>
                <input
                  id="tasker-confirmPassword"
                  name="confirmPassword"
                  type="password"
                  className="input"
                  value={taskerForm.confirmPassword}
                  onChange={handleTaskerFormChange}
                  required
                  disabled={isLoading}
                />
              </div>

              <button
                type="submit"
                className="btn btn-primary signin-submit"
                disabled={isLoading}
              >
                {isLoading ? "Signing up..." : "Sign up as Tasker"}
              </button>
            </form>
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

