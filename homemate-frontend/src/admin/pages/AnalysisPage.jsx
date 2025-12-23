import { useState } from 'react';
import {
    Box,
    Typography,
    Paper,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    TextField,
    Button,
    CircularProgress,
    Alert,
    Card,
    CardContent,
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
} from 'recharts';
import PageHeader from '../components/common/PageHeader';
import { 
    getUserGenderCount, 
    getUserStatusCounts, 
    getUserAgeBuckets, 
    getUserNewAccounts 
} from '../services/adminService';
import { generateTimeRanges, formatRangeLabel } from '../utility/userAnalysisUtility';

const AnalysisPage = () => {
    const [analysisType, setAnalysisType] = useState('');
    const [metric, setMetric] = useState('');
    const [selectedYear, setSelectedYear] = useState('');
    const [selectedMonth, setSelectedMonth] = useState('');
    const [selectedDate, setSelectedDate] = useState('');
    const [groupBy, setGroupBy] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [chartData, setChartData] = useState(null);

    const metricOptions = {
        user: [
            { value: 'age', label: 'Age Distribution' },
            { value: 'status', label: 'Status Distribution' },
            { value: 'gender', label: 'Gender Distribution' },
            { value: 'newAccounts', label: 'New Accounts' },
        ],
        tasker: [
            { value: 'placeholder1', label: 'Placeholder 1' },
            { value: 'placeholder2', label: 'Placeholder 2' },
        ],
        task: [
            { value: 'placeholder1', label: 'Placeholder 1' },
            { value: 'placeholder2', label: 'Placeholder 2' },
        ],
    };

    const handleAnalysisTypeChange = (e) => {
        setAnalysisType(e.target.value);
        setMetric('');
        setSelectedYear('');
        setSelectedMonth('');
        setSelectedDate('');
        setGroupBy('');
        setChartData(null);
        setError('');
    };

    const handleMetricChange = (e) => {
        setMetric(e.target.value);
        setSelectedYear('');
        setSelectedMonth('');
        setSelectedDate('');
        setGroupBy('');
        setChartData(null);
        setError('');
    };

    const handleGroupByChange = (e) => {
        setGroupBy(e.target.value);
        setSelectedYear('');
        setSelectedMonth('');
        setSelectedDate('');
        setChartData(null);
        setError('');
    };



    const handleFetch = async () => {
        if (!analysisType || !metric) {
            setError('Please select analysis type and metric');
            return;
        }

        if (metric === 'newAccounts') {
            if (!groupBy) {
                setError('Please select a grouping option');
                return;
            }
            if (groupBy === 'year' && !selectedYear) {
                setError('Please select a year');
                return;
            }
            if (groupBy === 'month' && !selectedMonth) {
                setError('Please select a month');
                return;
            }
            if (groupBy === 'day' && !selectedDate) {
                setError('Please select a date');
                return;
            }
        }

        setLoading(true);
        setError('');
        setChartData(null);

        try {
            let data;
            let formattedData = [];

            if (analysisType === 'user') {
                if (metric === 'gender') {
                    data = await getUserGenderCount();
                    formattedData = [
                        { name: 'Male', value: data.male },
                        { name: 'Female', value: data.female },
                    ];
                } else if (metric === 'status') {
                    data = await getUserStatusCounts();
                    formattedData = [
                        { name: 'Suspended', value: data.suspended },
                        { name: 'Admin (Active)', value: data.adminActive },
                        { name: 'Non-Admin (Active)', value: data.nonAdminActive },
                    ];
                } else if (metric === 'age') {
                    data = await getUserAgeBuckets();
                    formattedData = Object.entries(data.buckets).map(([range, count]) => ({
                        name: range,
                        value: count,
                    }));
                } else if (metric === 'newAccounts') {
                    const ranges = generateTimeRanges(groupBy, selectedYear, selectedMonth, selectedDate);
                    data = await getUserNewAccounts(ranges);
                    formattedData = data.counts.map((count, index) => {
                        const label = formatRangeLabel(ranges[index], groupBy);
                        return {
                            name: label,
                            value: count,
                        };
                    });
                }
            } else {
                // Placeholder for tasker and task analysis
                setError('This analysis type is not yet implemented');
                return;
            }

            setChartData(formattedData);
        } catch (err) {
            console.error('Error fetching analysis:', err);
            setError(err.message || 'Failed to fetch analysis data');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Box sx={{ p: 3 }}>
            <PageHeader title="Platform Analysis" subtitle="Comprehensive insights and metrics" />

            <Card elevation={2} sx={{ mt: 3, mb: 3 }}>
                <CardContent>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                        {/* Analysis Type Dropdown */}
                        <FormControl fullWidth>
                            <InputLabel>Analysis Type</InputLabel>
                            <Select
                                value={analysisType}
                                label="Analysis Type"
                                onChange={handleAnalysisTypeChange}
                            >
                                <MenuItem value="user">User Analysis</MenuItem>
                                <MenuItem value="tasker">Tasker Analysis</MenuItem>
                                <MenuItem value="task">Task Analysis</MenuItem>
                            </Select>
                        </FormControl>

                        {/* Metric Dropdown */}
                        {analysisType && (
                            <FormControl fullWidth>
                                <InputLabel>Metric</InputLabel>
                                <Select
                                    value={metric}
                                    label="Metric"
                                    onChange={handleMetricChange}
                                >
                                    {metricOptions[analysisType]?.map((option) => (
                                        <MenuItem key={option.value} value={option.value}>
                                            {option.label}
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        )}

                        {/* New Accounts Specific Inputs */}
                        {metric === 'newAccounts' && (
                            <>
                                <FormControl fullWidth>
                                    <InputLabel>Group By</InputLabel>
                                    <Select
                                        value={groupBy}
                                        label="Group By"
                                        onChange={handleGroupByChange}
                                    >
                                        <MenuItem value="year">Year</MenuItem>
                                        <MenuItem value="month">Month</MenuItem>
                                        <MenuItem value="day">Day</MenuItem>
                                    </Select>
                                </FormControl>

                                {groupBy === 'year' && (
                                    <TextField
                                        fullWidth
                                        label="Select Year"
                                        type="number"
                                        value={selectedYear}
                                        onChange={(e) => setSelectedYear(e.target.value)}
                                        InputLabelProps={{ shrink: true }}
                                        inputProps={{ min: 2000, max: new Date().getFullYear() }}
                                        placeholder={`e.g., ${new Date().getFullYear() - 1}`}
                                    />
                                )}

                                {groupBy === 'month' && (
                                    <TextField
                                        fullWidth
                                        label="Select Month"
                                        type="month"
                                        value={selectedMonth}
                                        onChange={(e) => setSelectedMonth(e.target.value)}
                                        InputLabelProps={{ shrink: true }}
                                    />
                                )}

                                {groupBy === 'day' && (
                                    <TextField
                                        fullWidth
                                        label="Select Start Date"
                                        type="date"
                                        value={selectedDate}
                                        onChange={(e) => setSelectedDate(e.target.value)}
                                        InputLabelProps={{ shrink: true }}
                                    />
                                )}
                            </>
                        )}

                        {/* Fetch Button */}
                        {metric && (
                            <Button
                                variant="contained"
                                color="primary"
                                onClick={handleFetch}
                                disabled={loading}
                                sx={{ mt: 2 }}
                            >
                                {loading ? <CircularProgress size={24} /> : 'Fetch Data'}
                            </Button>
                        )}
                    </Box>
                </CardContent>
            </Card>

            {/* Error Display */}
            {error && (
                <Alert severity="error" sx={{ mb: 3 }}>
                    {error}
                </Alert>
            )}

            {/* Chart Display */}
            {chartData && chartData.length > 0 && (
                <Card elevation={2}>
                    <CardContent>
                        <Typography variant="h6" gutterBottom>
                            {metricOptions[analysisType]?.find(m => m.value === metric)?.label || 'Results'}
                        </Typography>
                        <Box sx={{ width: '100%', height: 500, mt: 2 }}>
                            <ResponsiveContainer width="100%" height="100%">
                                <BarChart data={chartData}>
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="value" fill="#8884d8" name="Count" />
                                </BarChart>
                            </ResponsiveContainer>
                        </Box>
                    </CardContent>
                </Card>
            )}

            {chartData && chartData.length === 0 && (
                <Alert severity="info">No data available for the selected analysis</Alert>
            )}
        </Box>
    );
};

export default AnalysisPage;
