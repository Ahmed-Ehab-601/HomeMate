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
      role: loginResponse.role,
      username: loginResponse.username,
      firstname: loginResponse.firstname,
      lastname: loginResponse.lastname,
    };
    setUser(userData);
    setToken(loginResponse.token);
    localStorage.setItem("homemate_user", JSON.stringify(userData));
    localStorage.setItem("homemate_token", loginResponse.token);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("homemate_user");
    localStorage.removeItem("homemate_token");
  };

  const getToken = () => {
    return token || localStorage.getItem("homemate_token");
  };

  const value = {
    user,
    token,
    isAuthenticated: !!user && !!token,
    isLoading,
    login,
    logout,
    getToken,
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

