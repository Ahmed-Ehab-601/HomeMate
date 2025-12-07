import { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Check for stored user and token on mount
  useEffect(() => {
    const storedUser = localStorage.getItem("homemate_user");
    const storedToken = localStorage.getItem("homemate_token");
    if (storedUser && storedToken) {
      try {
        setUser(JSON.parse(storedUser));
        setToken(storedToken);
      } catch (error) {
        console.error("Failed to parse stored user", error);
        localStorage.removeItem("homemate_user");
        localStorage.removeItem("homemate_token");
      }
    }
    setIsLoading(false);
  }, []);

  const login = (loginResponse) => {
    // loginResponse contains: role, username, firstname, lastname, token
    const userData = {
      role: loginResponse.role || "ROLE_USER", // Default to USER if no role
      username: loginResponse.username,
      firstname: loginResponse.firstname,
      lastname: loginResponse.lastname,
    };
    setUser(userData);
    setToken(loginResponse.token);
    localStorage.setItem("homemate_user", JSON.stringify(userData));
    localStorage.setItem("homemate_token", loginResponse.token);
  };

  const signup = (signupResponse, role) => {
    // signupResponse is just the JWT token string
    // We need to extract user info from token or use defaults
    const userData = {
      role: role || "ROLE_USER",
      username: "", // Will be populated from token or user input
      firstname: "",
      lastname: "",
    };
    setUser(userData);
    setToken(signupResponse);
    localStorage.setItem("homemate_user", JSON.stringify(userData));
    localStorage.setItem("homemate_token", signupResponse);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("homemate_user");
    localStorage.removeItem("homemate_token");
    // Redirect to home page
    window.location.href = "/";
  };

  const getToken = () => {
    return token || localStorage.getItem("homemate_token");
  };

  // Get user role, default to ROLE_USER if not authenticated
  const getUserRole = () => {
    if (!user) return "ROLE_USER"; // Default to regular user
    return user.role || "ROLE_USER";
  };

  // Check if user is a tasker
  const isTasker = () => {
    return getUserRole() === "ROLE_TASKER";
  };

  // Check if user is a regular user
  const isRegularUser = () => {
    return getUserRole() === "ROLE_USER" || getUserRole() === "ROLE_ADMIN";
  };

  // Check if user is an admin
  const isAdmin = () => {
    return getUserRole() === "ROLE_ADMIN";
  };

  const value = {
    user,
    token,
    isAuthenticated: !!user && !!token,
    isLoading,
    login,
    signup,
    logout,
    getToken,
    getUserRole,
    isTasker,
    isRegularUser,
    isAdmin,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
