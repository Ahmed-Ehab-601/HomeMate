import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Typography,
} from '@mui/material';

const SuspensionReasonDialog = ({ open, onClose, onConfirm, title = 'Suspend', description }) => {
  const [reason, setReason] = useState('');

  useEffect(() => {
    if (!open) setReason('');
  }, [open]);

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{title}</DialogTitle>
      <DialogContent>
        {description && (
          <Typography variant="body2" sx={{ mb: 1 }}>
            {description}
          </Typography>
        )}
        <TextField
          autoFocus
          multiline
          minRows={3}
          fullWidth
          label="Reason (optional)"
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          placeholder="Provide a brief reason for suspension (optional)"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          variant="contained"
          onClick={() => {
            onConfirm(reason || '');
          }}
        >
          Confirm
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default SuspensionReasonDialog;
