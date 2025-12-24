// contexts/UnreadContext.jsx
import { createContext, useContext, useState, useEffect, useCallback } from "react";
import { calculateUnreadMessages } from "../api/chatApi";
import { useAuth } from "./AuthContext";

const UnreadContext = createContext(null);

export function UnreadProvider({ children }) {
  const { user, isAuthenticated } = useAuth();
  const [hasUnread, setHasUnread] = useState(false);
  const [isChecking, setIsChecking] = useState(false);

  const checkUnreadMessages = useCallback(async () => {
    if (!isAuthenticated || !user?.id) {
      setHasUnread(false);
      return;
    }

    setIsChecking(true);
    try {
      const result = await calculateUnreadMessages(user.id, user.role);
      setHasUnread(result === true);
    } catch (error) {
      console.error("Failed to check unread messages:", error);
      setHasUnread(false);
    } finally {
      setIsChecking(false);
    }
  }, [isAuthenticated, user?.id, user?.role]);

  // Check on mount and when user changes
  useEffect(() => {
    if (isAuthenticated && user?.id) {
      checkUnreadMessages();
    } else {
      setHasUnread(false);
    }
  }, [isAuthenticated, user?.id, checkUnreadMessages]);

  const value = {
    hasUnread,
    isChecking,
    refreshUnread: checkUnreadMessages,
  };

  return <UnreadContext.Provider value={value}>{children}</UnreadContext.Provider>;
}

export function useUnread() {
  const context = useContext(UnreadContext);
  if (!context) {
    throw new Error("useUnread must be used within an UnreadProvider");
  }
  return context;
}