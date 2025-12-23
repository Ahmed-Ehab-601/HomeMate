import { useEffect, useState } from 'react';
import {
    Box,
    Typography,
    Paper,
    Grid,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    CircularProgress,
    Alert,
    Divider,
    Card,
    CardContent,
    Chip
} from '@mui/material';
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    Legend,
    ResponsiveContainer,
    Cell
} from 'recharts';
import PageHeader from '../components/common/PageHeader';
import { getAnalysis } from '../services/adminService';

const COLORS = ['#8884d8', '#82ca9d', '#ffc658', '#ff8042', '#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

const AnalysisPage = () => {
    const [analysisData, setAnalysisData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        fetchAnalysis();
    }, []);

    const fetchAnalysis = async () => {
        try {
            setLoading(true);
            const data = await getAnalysis();
            setAnalysisData(data);
            setError('');
        } catch (err) {
            console.error('Error fetching analysis:', err);
            setError(err.message || 'Failed to fetch analysis data');
        } finally {
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
                <CircularProgress size={60} />
            </Box>
        );
    }

    if (error) {
        return (
            <Box p={3}>
                <Alert severity="error">{error}</Alert>
            </Box>
        );
    }

    const { userAnalysis, taskerAnalysis, taskAnalysis } = analysisData || {};

    // Prepare chart data
    const ageChartData = userAnalysis?.ageDistribution
        ? Object.entries(userAnalysis.ageDistribution)
            .sort((a, b) => parseInt(a[0]) - parseInt(b[0]))
            .map(([age, count]) => ({ age: `${age}`, count }))
        : [];

    const taskersPerServiceData = taskerAnalysis?.taskersPerService
        ? Object.entries(taskerAnalysis.taskersPerService).map(([name, count]) => ({ name, count }))
        : [];

    const tasksPerServiceData = taskAnalysis?.tasksPerService
        ? Object.entries(taskAnalysis.tasksPerService)
            .sort((a, b) => b[1] - a[1])
            .map(([name, count]) => ({ name, count }))
        : [];

    const avgBillPerServiceData = taskAnalysis?.averageBillPerService
        ? Object.entries(taskAnalysis.averageBillPerService)
            .sort((a, b) => b[1] - a[1])
            .map(([name, value]) => ({ name, value: parseFloat(value.toFixed(2)) }))
        : [];

    return (
        <Box sx={{ p: 3 }}>
            <PageHeader title="Platform Analysis" subtitle="Comprehensive insights and metrics" />

            {/* User Analysis Section */}
            <Box mb={4}>
                <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ mt: 3, mb: 2 }}>
                    👥 User Analysis
                </Typography>

                <Grid container spacing={3}>
                    {/* Gender Distribution Card */}
                    <Grid item xs={12} md={6}>
                        <Card elevation={2}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Gender Distribution</Typography>
                                <Divider sx={{ mb: 2 }} />
                                {userAnalysis?.genderDistribution && Object.entries(userAnalysis.genderDistribution).map(([gender, count]) => (
                                    <Box key={gender} display="flex" justifyContent="space-between" mb={1}>
                                        <Typography variant="body1">{gender || 'Unknown'}</Typography>
                                        <Chip label={count} color="primary" size="small" />
                                    </Box>
                                ))}
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Key Metrics Card */}
                    <Grid item xs={12} md={6}>
                        <Card elevation={2}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Key Metrics</Typography>
                                <Divider sx={{ mb: 2 }} />
                                <Box mb={2}>
                                    <Typography variant="body2" color="text.secondary">Suspended Users</Typography>
                                    <Typography variant="h4" color="error">{userAnalysis?.suspendedUsersCount || 0}</Typography>
                                </Box>
                                <Box mb={2}>
                                    <Typography variant="body2" color="text.secondary">Avg Tasks Per User</Typography>
                                    <Typography variant="h4" color="primary">{userAnalysis?.averageTasksPerUser?.toFixed(2) || 0}</Typography>
                                </Box>
                                <Box>
                                    <Typography variant="body2" color="text.secondary">Users with No Completed Tasks</Typography>
                                    <Typography variant="h4" color="warning.main">{userAnalysis?.usersWithNoCompletedTasksCount || 0}</Typography>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Age Distribution Chart */}
                    <Grid item xs={12}>
                        <Card elevation={2} sx={{ minWidth: 600, width: '100%' }}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Age Distribution</Typography>
                                <Divider sx={{ mb: 2 }} />
                                <Box sx={{ width: '100%' }}>
                                    <ResponsiveContainer width="100%" height={500}>
                                        <BarChart data={ageChartData}>
                                            <CartesianGrid strokeDasharray="3 3" />
                                            <XAxis dataKey="age" label={{ value: 'Age (years)', position: 'insideBottom', offset: -5 }} />
                                            <YAxis label={{ value: 'Count', angle: -90, position: 'insideLeft' }} />
                                            <Tooltip />
                                            <Legend />
                                            <Bar dataKey="count" fill="#8884d8" name="User Count" barSize={40} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </Box>

            {/* Tasker Analysis Section */}
            <Box mb={4}>
                <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ mt: 4, mb: 2 }}>
                    🛠️ Tasker Analysis
                </Typography>

                <Grid container spacing={3}>
                    {/* Availability Distribution */}
                    <Grid item xs={12} md={6}>
                        <Card elevation={2}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Availability Distribution</Typography>
                                <Divider sx={{ mb: 2 }} />
                                {taskerAnalysis?.availabilityDistribution && Object.entries(taskerAnalysis.availabilityDistribution).map(([status, count]) => (
                                    <Box key={status} display="flex" justifyContent="space-between" mb={1}>
                                        <Typography variant="body1">{status}</Typography>
                                        <Chip label={count} color={status === 'Available' ? 'success' : 'default'} size="small" />
                                    </Box>
                                ))}
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Taskers Per Service Chart */}
                    <Grid item xs={12}>
                        <Card elevation={2} sx={{ minWidth: 600, width: '100%' }}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Taskers Per Service</Typography>
                                <Divider sx={{ mb: 2 }} />
                                <Box sx={{ width: '100%' }}>
                                    <ResponsiveContainer width="100%" height={500}>
                                        <BarChart data={taskersPerServiceData}>
                                            <CartesianGrid strokeDasharray="3 3" />
                                            <XAxis dataKey="name" label={{ value: 'Service', position: 'insideBottom', offset: -5 }} />
                                            <YAxis label={{ value: 'Number of Taskers', angle: -90, position: 'insideLeft' }} />
                                            <Tooltip />
                                            <Legend />
                                            <Bar dataKey="count" fill="#82ca9d" name="Tasker Count" barSize={40}>
                                                {taskersPerServiceData.map((entry, index) => (
                                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                                ))}
                                            </Bar>
                                        </BarChart>
                                    </ResponsiveContainer>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>
                </Grid>
            </Box>

            {/* Task Analysis Section */}
            <Box mb={4}>
                <Typography variant="h5" fontWeight="bold" gutterBottom sx={{ mt: 4, mb: 2 }}>
                    📋 Task Analysis
                </Typography>

                <Grid container spacing={3}>
                    {/* Status Overview */}
                    <Grid item xs={12} md={6}>
                        <Card elevation={2}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Task Status Overview</Typography>
                                <Divider sx={{ mb: 2 }} />
                                {taskAnalysis?.statusOverview && Object.entries(taskAnalysis.statusOverview).map(([status, count]) => (
                                    <Box key={status} display="flex" justifyContent="space-between" mb={1}>
                                        <Typography variant="body1">{status}</Typography>
                                        <Chip label={count} color="primary" size="small" />
                                    </Box>
                                ))}
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Key Task Metrics */}
                    <Grid item xs={12} md={6}>
                        <Card elevation={2}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Key Task Metrics</Typography>
                                <Divider sx={{ mb: 2 }} />
                                <Box mb={2}>
                                    <Typography variant="body2" color="text.secondary">Avg Task Duration</Typography>
                                    <Typography variant="h4" color="primary">
                                        {taskAnalysis?.averageTaskDurationHours?.toFixed(1) || 0}h
                                    </Typography>
                                </Box>
                                <Box mb={2}>
                                    <Typography variant="body2" color="text.secondary">Tasks with Reports</Typography>
                                    <Typography variant="h4" color="error">{taskAnalysis?.tasksWithReportsCount || 0}</Typography>
                                </Box>
                                <Box>
                                    <Typography variant="body2" color="text.secondary">Delayed Completions</Typography>
                                    <Typography variant="h4" color="warning.main">{taskAnalysis?.tasksWithDelayedCompletionCount || 0}</Typography>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Tasks Per Service Chart */}
                    <Grid item xs={12}>
                        <Card elevation={2} sx={{ minWidth: 600, width: '100%' }}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Tasks Per Service</Typography>
                                <Divider sx={{ mb: 2 }} />
                                <Box sx={{ width: '100%' }}>
                                    <ResponsiveContainer width="100%" height={500}>
                                        <BarChart data={tasksPerServiceData}>
                                            <CartesianGrid strokeDasharray="3 3" />
                                            <XAxis dataKey="name" label={{ value: 'Service', position: 'insideBottom', offset: -5 }} />
                                            <YAxis label={{ value: 'Task Count', angle: -90, position: 'insideLeft' }} />
                                            <Tooltip />
                                            <Legend />
                                            <Bar dataKey="count" fill="#0088FE" name="Task Count" barSize={40}>
                                                {tasksPerServiceData.map((entry, index) => (
                                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                                ))}
                                            </Bar>
                                        </BarChart>
                                    </ResponsiveContainer>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Average Bill Per Service Chart */}
                    <Grid item xs={12}>
                        <Card elevation={2} sx={{ minWidth: 600, width: '100%' }}>
                            <CardContent>
                                <Typography variant="h6" gutterBottom>Average Bill Per Service</Typography>
                                <Divider sx={{ mb: 2 }} />
                                <Box sx={{ width: '100%' }}>
                                    <ResponsiveContainer width="100%" height={500}>
                                        <BarChart data={avgBillPerServiceData}>
                                            <CartesianGrid strokeDasharray="3 3" />
                                            <XAxis dataKey="name" label={{ value: 'Service', position: 'insideBottom', offset: -5 }} />
                                            <YAxis label={{ value: 'Avg Bill ($)', angle: -90, position: 'insideLeft' }} />
                                            <Tooltip />
                                            <Legend />
                                            <Bar dataKey="value" fill="#00C49F" name="Average Bill ($)" barSize={40}>
                                                {avgBillPerServiceData.map((entry, index) => (
                                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                                ))}
                                            </Bar>
                                        </BarChart>
                                    </ResponsiveContainer>
                                </Box>
                            </CardContent>
                        </Card>
                    </Grid>

                    {/* Peak Creation Hours */}
                    <Grid item xs={12} md={6}>
                        <TableContainer component={Paper} elevation={2}>
                            <Table>
                                <TableHead>
                                    <TableRow sx={{ bgcolor: 'warning.main' }}>
                                        <TableCell sx={{ color: 'white', fontWeight: 'bold' }}>Hour of Day</TableCell>
                                        <TableCell sx={{ color: 'white', fontWeight: 'bold' }} align="right">Tasks Created</TableCell>
                                    </TableRow>
                                </TableHead>
                                <TableBody>
                                    {taskAnalysis?.peakCreationHours && Object.entries(taskAnalysis.peakCreationHours)
                                        .sort((a, b) => parseInt(a[0]) - parseInt(b[0]))
                                        .map(([hour, count]) => (
                                            <TableRow key={hour} hover>
                                                <TableCell>{hour}:00</TableCell>
                                                <TableCell align="right">{count}</TableCell>
                                            </TableRow>
                                        ))}
                                </TableBody>
                            </Table>
                        </TableContainer>
                    </Grid>
                </Grid>
            </Box>
        </Box>
    );
};

export default AnalysisPage;
