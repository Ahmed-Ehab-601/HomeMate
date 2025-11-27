import { Stack, Typography, Button, Chip } from '@mui/material';

const PageHeader = ({ title, subtitle, actionLabel, onAction, statusChip }) => (
  <Stack
    direction={{ xs: 'column', sm: 'row' }}
    justifyContent="space-between"
    alignItems={{ xs: 'flex-start', sm: 'center' }}
    mb={3}
    spacing={2}
  >
    <Stack spacing={1}>
      <Stack direction="row" spacing={1} alignItems="center">
        <Typography variant="h4">{title}</Typography>
        {statusChip && <Chip size="small" color="secondary" label={statusChip} />}
      </Stack>
      {subtitle && (
        <Typography variant="body2" color="text.secondary">
          {subtitle}
        </Typography>
      )}
    </Stack>
    {actionLabel && (
      <Button variant="contained" onClick={onAction} sx={{ alignSelf: 'flex-start' }}>
        {actionLabel}
      </Button>
    )}
  </Stack>
);

export default PageHeader;

