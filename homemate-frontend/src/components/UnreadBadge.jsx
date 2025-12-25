// components/UnreadBadge.jsx
import "../styles/UnreadBadge.css";

function UnreadBadge({ count, maxDisplay = 99 }) {
  if (!count || count === 0) return null;

  const displayCount = count > maxDisplay ? `${maxDisplay}+` : count;

  return (
    <span className="unread-badge" aria-label={`${count} unread messages`}>
      {displayCount}
    </span>
  );
}

export default UnreadBadge;