import { createContext, useContext, useState, useEffect } from "react";
import { setUserOffline, setTaskerOffline } from "../api/authApi";

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

  // Helper function to decode JWT and extract ID
  const decodeJwtPayload = (token) => {
    if (!token) return null;
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return null;
      const payload = parts[1];
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      const pad = base64.length % 4;
      const padded = pad ? base64 + '='.repeat(4 - pad) : base64;
      const decoded = atob(padded);
      const json = decodeURIComponent(
        decoded.split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
      );
      return JSON.parse(json);
    } catch (err) {
      console.error('Failed to decode JWT:', err);
      return null;
    }
  };

  const login = (loginResponse) => {
    // loginResponse contains: role, username, firstname, lastname, token
    const token = loginResponse.token;
    const role = loginResponse.role || "ROLE_USER";
    
    // Decode JWT to extract ID
    const jwtPayload = decodeJwtPayload(token);
    console.log("JWT Payload:", jwtPayload); // Debug log
    
    // Extract ID from JWT payload
    const extractedId = jwtPayload?.id || jwtPayload?.sub;
    
    const isTaskerRole = role === "ROLE_TASKER";
    
    const userData = {
      role: role,
      username: loginResponse.username || "",
      firstname: loginResponse.firstname || "",
      lastname: loginResponse.lastname || "",
      userId: !isTaskerRole ? extractedId : undefined,
      taskerId: isTaskerRole ? extractedId : undefined,
      id: extractedId, // Unified ID
    };
    
    console.log("Login - User data:", userData); // Debug log
    
    setUser(userData);
    setToken(token);
    localStorage.setItem("homemate_user", JSON.stringify(userData));
    localStorage.setItem("homemate_token", token);
  };

  const signup = (signupResponse, role) => {
    // signupResponse is just the JWT token string
    // We need to extract user info from token or use defaults
    const userData = {
      role: role || "ROLE_USER",
      username: "",
      firstname: "",
      lastname: "",
    };
    setUser(userData);
    setToken(signupResponse);
    localStorage.setItem("homemate_user", JSON.stringify(userData));
    localStorage.setItem("homemate_token", signupResponse);
  };

  const logout = async () => {
    try {
      // Get current user data
      const currentUser = user || JSON.parse(localStorage.getItem("homemate_user") || "{}");
      const userRole = currentUser?.role;
      
      // Try multiple ID fields to ensure we get the ID
      let id = currentUser?.id || 
               currentUser?.userId || 
               currentUser?.taskerId || 
               currentUser?.userID || 
               currentUser?.taskerID;
      
      // Convert string ID to number if needed
      if (typeof id === 'string') {
        id = parseInt(id, 10);
      }
      
      console.log("Logout - User role:", userRole, "ID:", id); // Debug log
      
      if (!id || isNaN(id)) {
        console.warn("No valid user ID found for offline status update");
        // Continue with logout even without ID
      } else {
        // Call the appropriate offline endpoint based on role
        if (userRole === "ROLE_TASKER") {
          await setTaskerOffline(id);
          console.log("Tasker set to offline successfully");
        } else if (userRole === "ROLE_USER" || userRole === "ROLE_ADMIN") {
          await setUserOffline(id);
          console.log("User set to offline successfully");
        } else {
          console.warn("Unknown role:", userRole);
        }
      }
    } catch (err) {
      console.warn("Failed to set offline status:", err.message || err);
      // Continue with logout even if offline call fails
    } finally {
      // Clear local state and storage
      setUser(null);
      setToken(null);
      localStorage.removeItem("homemate_user");
      localStorage.removeItem("homemate_token");
      
      // Clear email verification related data
      localStorage.removeItem("verify_token");
      localStorage.removeItem("verified_email");
      localStorage.removeItem("verified_user_type");
    }
  };

  const getToken = () => {
    return token || localStorage.getItem("homemate_token");
  };

  // Get user role, default to ROLE_USER if not authenticated
  const getUserRole = () => {
    if (!user) return "ROLE_USER";
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