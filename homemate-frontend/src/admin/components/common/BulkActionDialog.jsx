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
  TextField,
} from '@mui/material';

const BulkActionDialog = ({ open, onClose, onConfirm, type = 'user', count = 0 }) => {
  const [action, setAction] = useState('');
  const [reason, setReason] = useState('');

  useEffect(() => {
    if (!open) {
      setAction('');
      setReason('');
    }
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
        {action === 'suspend' && (
          <TextField
            fullWidth
            multiline
            minRows={2}
            label="Reason (optional)"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="Provide a reason for suspending the selected items"
            sx={{ mt: 2 }}
          />
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          onClick={() => {
            onConfirm(action, reason || '');
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
