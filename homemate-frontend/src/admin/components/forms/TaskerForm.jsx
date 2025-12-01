import { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  TextField,
  MenuItem,
  Button,
  FormControlLabel,
  Switch,
} from '@mui/material';

const defaultValues = {
  email: '',
  fname: '',
  lname: '',
  phone: '',
  username: '',
  admin: false,
  suspended: false,
  gender: 'M',
  hourRate: '',
  numberOfTasks: 0,
};

const genders = [
  { value: 'M', label: 'Male' },
  { value: 'F', label: 'Female' },
];

const TaskerForm = ({ open, initialValues, onClose, onSubmit }) => {
  const [values, setValues] = useState(defaultValues);

  useEffect(() => {
    setValues({ ...defaultValues, ...initialValues });
  }, [initialValues, open]);

  const handleChange = (field) => (event) => {
    const value =
      event.target.type === 'checkbox' ? event.target.checked : event.target.value;
    setValues((prev) => ({ ...prev, [field]: value }));
  };

  const handleSubmit = (event) => {
    event.preventDefault();
    onSubmit({
      ...values,
      hourRate: Number(values.hourRate || 0),
      numberOfTasks: Number(values.numberOfTasks || 0),
    });
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth component="form" onSubmit={handleSubmit}>
      <DialogTitle>{initialValues?.taskerID ? 'Edit tasker' : 'Create tasker'}</DialogTitle>
      <DialogContent dividers>
        <Grid container spacing={2} mt={0.5}>
          <Grid item xs={12} sm={6}>
            <TextField label="First name" fullWidth value={values.fname} onChange={handleChange('fname')} required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Last name" fullWidth value={values.lname} onChange={handleChange('lname')} required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Username"
              fullWidth
              value={values.username}
              onChange={handleChange('username')}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Email" type="email" fullWidth value={values.email} onChange={handleChange('email')} required />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField label="Phone" fullWidth value={values.phone} onChange={handleChange('phone')} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Hourly rate"
              type="number"
              fullWidth
              value={values.hourRate}
              onChange={handleChange('hourRate')}
              required
              inputProps={{ min: 0, step: 1 }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              label="Number of tasks"
              type="number"
              fullWidth
              value={values.numberOfTasks}
              onChange={handleChange('numberOfTasks')}
              inputProps={{ min: 0, step: 1 }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField select label="Gender" fullWidth value={values.gender} onChange={handleChange('gender')}>
              {genders.map((gender) => (
                <MenuItem key={gender.value} value={gender.value}>
                  {gender.label}
                </MenuItem>
              ))}
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={<Switch checked={values.admin} onChange={handleChange('admin')} />}
              label="Admin"
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <FormControlLabel
              control={<Switch checked={values.suspended} onChange={handleChange('suspended')} />}
              label="Suspended"
            />
          </Grid>
        </Grid>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button variant="contained" type="submit">
          Save
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default TaskerForm;

