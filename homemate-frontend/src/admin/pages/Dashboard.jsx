import { useEffect, useMemo, useState } from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  List,
  ListItem,
  ListItemText,
  Divider,
  Chip,
  Stack,
  LinearProgress,
  Box,
} from '@mui/material';
import StatCard from '../components/common/StatCard';
import { getUsers, getTaskers } from '../services/adminService';
import PageHeader from '../components/common/PageHeader';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalTaskers: 0,
    suspendedUsers: 0,
    suspendedTaskers: 0,
  });
  const [recentUsers, setRecentUsers] = useState([]);
  const [recentTaskers, setRecentTaskers] = useState([]);

  const [loading, setLoading] = useState(false);

  const trendChip = useMemo(() => {
    if (!stats.totalUsers) return 'Live data';
    const ratio = stats.suspendedUsers / (stats.totalUsers || 1);
    if (ratio > 0.4) return 'Action required';
    if (ratio > 0.2) return 'Monitor';
    return 'Healthy';
  }, [stats]);

  useEffect(() => {
    const loadSummary = async () => {
      try {
        setLoading(true);
        const [userRes, taskerRes] = await Promise.all([
          getUsers({ size: 5 }),
          getTaskers({ size: 5 }),
        ]);
        const userItems = userRes?.items ?? [];
        const taskerItems = taskerRes?.items ?? [];
        setStats({
          totalUsers: userRes?.totalItems ?? userItems.length,
          totalTaskers: taskerRes?.totalItems ?? taskerItems.length,
          suspendedUsers: userItems.filter((user) => user.suspended).length,
          suspendedTaskers: taskerItems.filter((tasker) => tasker.suspended).length,
        });
        setRecentUsers(userItems);
        setRecentTaskers(taskerItems);
      } catch (error) {
        console.error('Failed to load dashboard summary', error);
      } finally {
        setLoading(false);
      }
    };
    loadSummary();
  }, []);

  const renderList = (items, emptyLabel) => (
    <List>
      {items.map((item) => (
        <ListItem key={item.userId ?? item.taskerID} disablePadding>
          <ListItemText
            primary={`${item.fName ?? item.fname} ${item.lName ?? item.lname}`}
            secondary={item.email}
          />
        </ListItem>
      ))}
      {!items.length && (
        <ListItem>
          <ListItemText primary={emptyLabel} />
        </ListItem>
      )}
    </List>
  );

  return (
    <Box sx={{ width: '100vw', maxWidth: '100vw', margin: 0, boxSizing: 'border-box' }}>
      <PageHeader
        title="Admin Overview"
        subtitle="Monitor platform performance at a glance"
        statusChip={trendChip}
      />
      <Grid container spacing={3} mb={3}>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard label="Total users" value={stats.totalUsers} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard label="Total taskers" value={stats.totalTaskers} />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard label="Suspended users" value={stats.suspendedUsers} trendColor="warning" />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            label="Suspended taskers"
            value={stats.suspendedTaskers}
            trendColor="warning"
          />
        </Grid>
      </Grid>
      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card
            elevation={0}
            sx={{
              background: 'linear-gradient(135deg, #6750A4, #00BFA6)',
              color: '#fff',
            }}
          >
            <CardContent>
              <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
                <Typography variant="h6">Live activity</Typography>
                {loading && (
                  <Chip
                    label="Refreshing…"
                    size="small"
                    sx={{ bgcolor: 'rgba(255,255,255,0.2)', color: '#fff' }}
                  />
                )}
              </Stack>
              <Typography variant="body2" sx={{ mb: 2, opacity: 0.8 }}>
                Auto-refresh pulls the latest user and tasker stats every visit.
              </Typography>
              <LinearProgress
                variant="determinate"
                value={Math.min((stats.totalUsers % 100) + 20, 100)}
                sx={{ bgcolor: 'rgba(255,255,255,0.2)', '& .MuiLinearProgress-bar': { bgcolor: '#fff' } }}
              />
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card elevation={0}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent users
              </Typography>
              <Divider sx={{ mb: 2 }} />
              {renderList(recentUsers, 'No users available')}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card elevation={0}>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent taskers
              </Typography>
              <Divider sx={{ mb: 2 }} />
              {renderList(recentTaskers, 'No taskers available')}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;

