import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Card,
  CardContent,
  Typography,
  Box,
  Button,
  Chip,
  Grid,
  Divider,
  CircularProgress,
  Alert,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PersonIcon from '@mui/icons-material/Person';
import HandymanIcon from '@mui/icons-material/Handyman';
import AssignmentIcon from '@mui/icons-material/Assignment';

const ReportDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    // TODO: Fetch report details from backend
    // For now, using mock data
    const fetchReportDetails = async () => {
      setLoading(true);
      try {
        // Simulate API call
        await new Promise((resolve) => setTimeout(resolve, 500));
        
        // Mock data - replace with actual API call
        setReport({
          reportID: id,
          header: 'Sample Report Header',
          body: 'This is a detailed description of the report. The report contains information about an issue that occurred during task execution. The user has provided detailed feedback about the problem.',
          taskID: 123,
          reporter: true, // true = User, false = Tasker
          adminStatus: 'pending',
        });
        setError('');
      } catch (err) {
        console.error('Error fetching report details:', err);
        setError('Failed to fetch report details');
      } finally {
        setLoading(false);
      }
    };

    fetchReportDetails();
  }, [id]);

  const getStatusColor = (status) => {
    switch (status?.toLowerCase()) {
      case 'pending':
        return 'warning';
      case 'resolved':
        return 'success';
      case 'rejected':
        return 'error';
      default:
        return 'default';
    }
  };

  const handleBack = () => {
    navigate('/admin/reports');
  };

  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '400px',
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (error || !report) {
    return (
      <Box sx={{ p: 3 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBack}
          sx={{ mb: 3 }}
        >
          Back to Reports
        </Button>
        <Alert severity="error">{error || 'Report not found'}</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3, maxWidth: '1200px', margin: '0 auto' }}>
      <Button
        startIcon={<ArrowBackIcon />}
        onClick={handleBack}
        sx={{ mb: 3 }}
        variant="outlined"
      >
        Back to Reports
      </Button>

      <Card
        elevation={0}
        sx={{
          borderRadius: 1.5,
          border: (theme) =>
            theme.palette.mode === 'dark'
              ? '1px solid rgba(255,255,255,0.08)'
              : '1px solid rgba(215,240,74,0.1)',
          boxShadow: (theme) =>
            theme.palette.mode === 'dark'
              ? '0 20px 35px rgba(0,0,0,0.6)'
              : '0 20px 35px rgba(15,15,15,0.05)',
        }}
      >
        <CardContent sx={{ p: 4 }}>
          {/* Header Section */}
          <Box sx={{ mb: 4 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h4" sx={{ fontWeight: 600 }}>
                Report Details
              </Typography>
              <Chip
                label={report.adminStatus || 'Pending'}
                color={getStatusColor(report.adminStatus)}
                size="medium"
                sx={{ fontSize: '0.875rem', fontWeight: 500 }}
              />
            </Box>
            <Typography variant="body2" color="text.secondary">
              Report ID: {report.reportID}
            </Typography>
          </Box>

          <Divider sx={{ mb: 4 }} />

          {/* Info Grid */}
          <Grid container spacing={3} sx={{ mb: 4 }}>
            <Grid item xs={12} md={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                <AssignmentIcon sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="subtitle2" color="text.secondary">
                  Task ID
                </Typography>
              </Box>
              <Typography variant="h6">{report.taskID}</Typography>
            </Grid>
            <Grid item xs={12} md={6}>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                {report.reporter ? (
                  <PersonIcon sx={{ mr: 1, color: 'primary.main' }} />
                ) : (
                  <HandymanIcon sx={{ mr: 1, color: 'secondary.main' }} />
                )}
                <Typography variant="subtitle2" color="text.secondary">
                  Reporter
                </Typography>
              </Box>
              <Chip
                icon={report.reporter ? <PersonIcon /> : <HandymanIcon />}
                label={report.reporter ? 'User' : 'Tasker'}
                color={report.reporter ? 'primary' : 'secondary'}
                variant="outlined"
              />
            </Grid>
          </Grid>

          <Divider sx={{ mb: 4 }} />

          {/* Report Content */}
          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
              Report Header
            </Typography>
            <Typography variant="body1" sx={{ mb: 4 }}>
              {report.header}
            </Typography>

            <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
              Report Body
            </Typography>
            <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
              {report.body}
            </Typography>
          </Box>

          <Divider sx={{ my: 4 }} />

          {/* Action Buttons */}
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
            <Button variant="outlined" color="error">
              Reject Report
            </Button>
            <Button variant="contained" color="success">
              Resolve Report
            </Button>
          </Box>

          <Alert severity="info" sx={{ mt: 3 }}>
            Note: Report detail fetching and action buttons are not yet connected to backend
          </Alert>
        </CardContent>
      </Card>
    </Box>
  );
};

export default ReportDetailsPage;
