import { Paper, Stack, TextField, MenuItem, Button, InputAdornment } from '@mui/material';
import PriceChangeIcon from '@mui/icons-material/PriceChange';
import ShieldMoonIcon from '@mui/icons-material/ShieldMoon';

const suspendedOptions = [
  { value: '', label: 'Any' },
  { value: 'true', label: 'Suspended' },
  { value: 'false', label: 'Active' },
];

const TaskerFilters = ({ values, onChange, onClear }) => (
  <Paper
    sx={{
      p: 2,
      mb: 3,
      borderRadius: 3,
      border: '1px solid rgba(0,0,0,0.04)',
      background: 'linear-gradient(135deg, rgba(0,191,166,0.08), rgba(103,80,164,0.05))',
    }}
    elevation={0}
  >
    <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
      <TextField
        select
        label="Suspended"
        value={values.suspended ?? ''}
        onChange={(event) => onChange('suspended', event.target.value)}
        fullWidth
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <ShieldMoonIcon fontSize="small" />
            </InputAdornment>
          ),
        }}
      >
        {suspendedOptions.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        label="Min hour rate"
        type="number"
        value={values.minRate ?? ''}
        onChange={(event) => onChange('minRate', event.target.value)}
        fullWidth
        inputProps={{ min: 0, step: 1 }}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <PriceChangeIcon fontSize="small" />
            </InputAdornment>
          ),
        }}
      />
      <Button variant="outlined" onClick={onClear} sx={{ alignSelf: { xs: 'stretch', md: 'center' } }}>
        Clear
      </Button>
    </Stack>
  </Paper>
);

export default TaskerFilters;

