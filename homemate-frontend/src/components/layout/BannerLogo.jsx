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
      className="banner-container"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <div className={`tool chainsaw ${isHovered ? 'flying' : ''}`}>🪚</div>
      <div className="tool saw-blade">⚙️</div>
      <div className="tool drill">🔧</div>
      <div className="tool tape">📏</div>
      <div className="tool wrench">🔨</div>
      <div className="tool hammer">🛠️</div>
      <div className="tool screwdriver">🪛</div>
      <div className="tool pliers">🔩</div>
      <div className="banner-board">
        <div className="banner-text">Homemate</div>
      </div>
    </div>
  );
};

export default BannerLogo;