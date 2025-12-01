import React from 'react';
import { Button } from '@mui/material';

const ServicesButton = ({ disabled = false, selectedIds = [], onClick }) => {
  const handleClick = () => {
    if (onClick) onClick(selectedIds);
    else console.debug('Services clicked', { selectedIds });
  };

  return (
    <Button variant="outlined" color="secondary" size="small" disabled={disabled} onClick={handleClick}>
      Services
    </Button>
  );
};

export default ServicesButton;
