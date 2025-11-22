import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  RadioGroup,
  FormControlLabel,
  Radio,
  Typography,
} from '@mui/material';

const BulkActionDialog = ({ open, onClose, onConfirm, type = 'user', count = 0 }) => {
  const [action, setAction] = useState('');

  useEffect(() => {
    if (!open) setAction('');
  }, [open]);

  const userOptions = [
    { value: 'promote', label: 'Promote to Admin' },
    { value: 'demote', label: 'Demote from Admin' },
    { value: 'suspend', label: 'Suspend selected' },
    { value: 'reactive', label: 'Unsuspend (Activate) selected' },
  ];

  const taskerOptions = [
    { value: 'suspend', label: 'Suspend selected' },
    { value: 'reactive', label: 'Unsuspend (Activate) selected' },
  ];

  const options = type === 'tasker' ? taskerOptions : userOptions;

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="xs">
      <DialogTitle>Action for {count} selected {type === 'tasker' ? 'tasker(s)' : 'user(s)'}</DialogTitle>
      <DialogContent>
        <Typography variant="body2" sx={{ mb: 1 }}>
          Choose the action to apply to the selected items.
        </Typography>
        <RadioGroup value={action} onChange={(e) => setAction(e.target.value)}>
          {options.map((opt) => (
            <FormControlLabel key={opt.value} value={opt.value} control={<Radio />} label={opt.label} />
          ))}
        </RadioGroup>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          onClick={() => {
            onConfirm(action);
          }}
          disabled={!action}
        >
          Apply
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default BulkActionDialog;
