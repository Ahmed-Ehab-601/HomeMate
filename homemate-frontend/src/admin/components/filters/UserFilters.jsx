import { Paper, Stack, TextField, MenuItem, Button, InputAdornment } from '@mui/material';
import ManageAccountsIcon from '@mui/icons-material/ManageAccounts';
import GppMaybeIcon from '@mui/icons-material/GppMaybe';
import SearchIcon from '@mui/icons-material/Search';

const selectOptions = [
  { value: '', label: 'Any' },
  { value: 'true', label: 'Yes' },
  { value: 'false', label: 'No' },
];

const UserFilters = ({ values, onChange, onClear, onSearch }) => (
  <Paper
    sx={{
      p: 2,
      mb: 3,
      borderRadius: 3,
      border: '1px solid rgba(0,0,0,0.04)',
      background: 'linear-gradient(135deg, rgba(215,240,74,0.05), rgba(215,240,74,0.1))',
    }}
    elevation={0}
  >
    <Stack direction={{ xs: 'column', md: 'row' }} spacing={2}>
      <TextField
        label="Username"
        value={values.username ?? ''}
        onChange={(event) => onChange('username', event.target.value)}
        fullWidth
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon fontSize="small" />
            </InputAdornment>
          ),
        }}
      />
      <TextField
        select
        label="Admin"
        value={values.admin ?? ''}
        onChange={(event) => onChange('admin', event.target.value)}
        fullWidth
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <ManageAccountsIcon fontSize="small" />
            </InputAdornment>
          ),
        }}
      >
        {selectOptions.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </TextField>
      <TextField
        select
        label="Suspended"
        value={values.suspended ?? ''}
        onChange={(event) => onChange('suspended', event.target.value)}
        fullWidth
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <GppMaybeIcon fontSize="small" />
            </InputAdornment>
          ),
        }}
      >
        {selectOptions.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </TextField>
      <Button
        variant="contained"
        color="success"
        onClick={() => onSearch && onSearch()}
        sx={{ alignSelf: { xs: 'stretch', md: 'center' } }}
      >
        Search
      </Button>
      <Button variant="outlined" onClick={onClear} sx={{ alignSelf: { xs: 'stretch', md: 'center' } }}>
        Clear
      </Button>
    </Stack>
  </Paper>
);

export default UserFilters;

