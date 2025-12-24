// components/UnreadIndicator.jsx
import "../styles/UnreadIndicator.css";

function UnreadIndicator({ show = false, size = "small", position = "top-right" }) {
  if (!show) return null;

  return (
    <span 
      className={`unread-indicator unread-indicator--${size} unread-indicator--${position}`}
      aria-label="Has unread messages"
    />
  );
}

export default UnreadIndicator;