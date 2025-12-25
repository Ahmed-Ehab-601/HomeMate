import React, { useState } from "react";
import "./BannerLogo.css";

const BannerLogo = ({ onHoverChange }) => {
  const [isHovered, setIsHovered] = useState(false);

  const handleMouseEnter = () => {
    setIsHovered(true);
    if (onHoverChange) onHoverChange(true);
  };

  const handleMouseLeave = () => {
    setIsHovered(false);
    if (onHoverChange) onHoverChange(false);
  };

  return (
    <div 
      className="logo-container"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <svg width="40" height="40" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M20 4L4 14V28C4 30.2091 5.79086 32 8 32H32C34.2091 32 36 30.2091 36 28V14L20 4Z" fill="#c6ff4d"/>
        <path d="M20 4L4 14V28C4 30.2091 5.79086 32 8 32H32C34.2091 32 36 30.2091 36 28V14L20 4Z" stroke="#1a1a1a" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        <path d="M15 32V20H25V32" stroke="#1a1a1a" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        <circle cx="28" cy="18" r="2" fill="#1a1a1a"/>
      </svg>
      <span className="logo-text">HomeMate</span>
    </div>
  );
};

export default BannerLogo;