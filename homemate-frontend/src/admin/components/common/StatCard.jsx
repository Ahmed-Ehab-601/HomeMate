import { Card, CardContent, Typography, Stack, Chip } from '@mui/material';

const StatCard = ({ label, value, trendLabel, trendColor = 'success' }) => (
  <Card elevation={0} sx={{ borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
    <CardContent>
      <Typography variant="subtitle2" color="text.secondary">
        {label}
      </Typography>
      <Stack direction="row" alignItems="baseline" spacing={1} mt={1}>
        <Typography variant="h4" fontWeight={700}>
          {value}
        </Typography>
        {trendLabel && <Chip size="small" color={trendColor} label={trendLabel} />}
      </Stack>
    </CardContent>
  </Card>
);

export default StatCard;

