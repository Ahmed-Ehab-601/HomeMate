import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { baseUrl } from '../../utils/apiClient';
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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Snackbar,
  Paper,
  Avatar,
  TextField,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import PersonIcon from '@mui/icons-material/Person';
import HandymanIcon from '@mui/icons-material/Handyman';
import AssignmentIcon from '@mui/icons-material/Assignment';
import BlockIcon from '@mui/icons-material/Block';
import HourglassEmptyIcon from '@mui/icons-material/HourglassEmpty';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import EmailIcon from '@mui/icons-material/Email';
import BadgeIcon from '@mui/icons-material/Badge';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import { suspendUser, suspendTasker } from '../api/adminApi';

const ReportDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [suspendLoading, setSuspendLoading] = useState(false);
  const [suspendDialog, setSuspendDialog] = useState({ open: false, type: null });
  const [suspendReason, setSuspendReason] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [completeLoading, setCompleteLoading] = useState(false);

  useEffect(() => {
    const fetchReportDetails = async () => {
      setLoading(true);
      try {
        const token = localStorage.getItem('homemate_token');
        const response = await fetch(`${baseUrl}/api/reports/${id}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });
        if (!response.ok) {
          throw new Error('Failed to fetch report details');
        }
        const data = await response.json();
        setReport(data);
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
      case 'done':
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

  const handleSuspendClick = (type) => {
    setSuspendDialog({ open: true, type });
    setSuspendReason('');
  };

  const handleSuspendConfirm = async () => {
    setSuspendLoading(true);
    try {
      if (suspendDialog.type === 'user') {
        await suspendUser(report.userID, true, suspendReason);
        setSuccessMessage(`User ${report.userUsername} has been suspended successfully`);
      } else if (suspendDialog.type === 'tasker') {
        await suspendTasker(report.taskerID, true, suspendReason);
        setSuccessMessage(`Tasker ${report.taskerUsername} has been suspended successfully`);
      }
      setSuspendDialog({ open: false, type: null });
      setSuspendReason('');
    } catch (err) {
      console.error('Error suspending user/tasker:', err);
      setError(`Failed to suspend ${suspendDialog.type}`);
    } finally {
      setSuspendLoading(false);
    }
  };

  const handleSuspendCancel = () => {
    setSuspendDialog({ open: false, type: null });
    setSuspendReason('');
  };

  const handleCompleteReport = async () => {
    setCompleteLoading(true);
    try {
      const token = localStorage.getItem('homemate_token');
      const response = await fetch(`${baseUrl}/api/reports/${id}/complete`, {
        method: 'PATCH',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });
      if (!response.ok) {
        throw new Error('Failed to complete report');
      }
      const data = await response.json();
      setReport(data);
      setSuccessMessage('Report marked as completed successfully');
    } catch (err) {
      console.error('Error completing report:', err);
      setError('Failed to complete report');
    } finally {
      setCompleteLoading(false);
    }
  };

  if (loading) {
    return (
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          minHeight: '100vh',
          background: '#f5f5f5',
        }}
      >
        <CircularProgress size={60} sx={{ color: 'white' }} />
      </Box>
    );
  }

  if (error || !report) {
    return (
      <Box sx={{ p: 3, minHeight: '100vh', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBack}
          sx={{ mb: 3, color: 'white', borderColor: 'white' }}
          variant="outlined"
        >
          Back to Reports
        </Button>
        <Alert severity="error" sx={{ borderRadius: 1 }}>{error || 'Report not found'}</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ 
      minHeight: '100vh',
      background: 'linear-gradient(180deg, #f8f9ff 0%, #f1f4ff 60%, #eff5ff 100%)',
      py: 4,
      px: { xs: 2, sm: 3 }
    }}>
      <Box sx={{ maxWidth: '1200px', margin: '0 auto' }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBack}
          sx={{ 
            mb: 3,
            color: '#667eea',
            borderColor: '#667eea',
            borderRadius: 1,
            '&:hover': {
              borderColor: '#764ba2',
              backgroundColor: 'rgba(102, 126, 234, 0.05)',
            }
          }}
          variant="outlined"
        >
          Back to Reports
        </Button>

        {/* Status Header Card */}
        <Paper
          elevation={0}
          sx={{
            borderRadius: 1,
            p: 3,
            mb: 3,
            background: '#ffffff',
            border: '1px solid #e0e0e0',
            boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
          }}
        >
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 2 }}>
            <Box>
              <Typography variant="h4" sx={{ fontWeight: 700, mb: 1, color: '#1a1a1a' }}>
                Report Details
              </Typography>
              <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                <Chip
                  icon={<AssignmentIcon />}
                  label={`Report #${report?.reportID}`}
                  size="small"
                  sx={{ backgroundColor: 'rgba(102, 126, 234, 0.1)', color: '#667eea', fontWeight: 600, borderRadius: 0.5 }}
                />
                <Chip
                  icon={<AssignmentIcon />}
                  label={`Task #${report?.taskID}`}
                  size="small"
                  sx={{ backgroundColor: 'rgba(118, 75, 162, 0.1)', color: '#764ba2', fontWeight: 600, borderRadius: 0.5 }}
                />
              </Box>
            </Box>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              {report?.adminStatus?.toLowerCase() === 'pending' && (
                <Button
                  variant="contained"
                  startIcon={<DoneAllIcon />}
                  onClick={handleCompleteReport}
                  disabled={completeLoading}
                  sx={{
                    backgroundColor: '#4caf50',
                    color: 'white',
                    fontWeight: 600,
                    textTransform: 'none',
                    py: 1,
                    px: 2,
                    borderRadius: 1,
                    '&:hover': {
                      backgroundColor: '#45a049',
                    },
                    '&:disabled': {
                      backgroundColor: 'rgba(76, 175, 80, 0.5)',
                      color: 'rgba(255,255,255,0.7)',
                    }
                  }}
                >
                  {completeLoading ? 'Completing...' : 'Complete'}
                </Button>
              )}
              <Chip
                icon={
                  report?.adminStatus?.toLowerCase() === 'done' ? <CheckCircleIcon /> : 
                  report?.adminStatus?.toLowerCase() === 'rejected' ? <BlockIcon /> : 
                  <HourglassEmptyIcon />
                }
                label={report?.adminStatus?.toUpperCase() || 'PENDING'}
                color={getStatusColor(report?.adminStatus)}
                sx={{ 
                  fontSize: '1rem', 
                  fontWeight: 700,
                  py: 2.5,
                  px: 2,
                  height: 'auto',
                  borderRadius: 1
                }}
              />
            </Box>
          </Box>
        </Paper>

        {/* Reporter Badge */}
        <Paper
          elevation={0}
          sx={{
            borderRadius: 1,
            p: 3,
            mb: 3,
            background: report?.reporter 
              ? 'linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(102, 126, 234, 0.05) 100%)'
              : 'linear-gradient(135deg, rgba(255, 152, 0, 0.1) 0%, rgba(255, 152, 0, 0.05) 100%)',
            backdropFilter: 'blur(20px)',
            border: '1px solid rgba(255,255,255,0.8)',
            boxShadow: '0 8px 32px rgba(0,0,0,0.08)',
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Avatar
              sx={{
                bgcolor: report?.reporter ? '#667eea' : '#ff9800',
                width: 56,
                height: 56,
              }}
            >
              {report?.reporter ? <PersonIcon fontSize="large" /> : <HandymanIcon fontSize="large" />}
            </Avatar>
            <Box>
              <Typography variant="overline" sx={{ color: 'text.secondary', fontWeight: 600 }}>
                Reported By
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 700, color: report?.reporter ? '#667eea' : '#ff9800' }}>
                {report?.reporter ? 'User' : 'Tasker'}
              </Typography>
            </Box>
          </Box>
        </Paper>

        {/* Report Content Card */}
        <Card
          elevation={0}
          sx={{
            borderRadius: 1,
            mb: 3,
            background: 'rgba(255,255,255,0.95)',
            backdropFilter: 'blur(20px)',
            boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
            overflow: 'hidden'
          }}
        >
          <Box sx={{ 
            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
            p: 3,
            color: 'white'
          }}>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              {report?.header}
            </Typography>
          </Box>
          <CardContent sx={{ p: 4 }}>
            <Typography variant="body1" sx={{ 
              whiteSpace: 'pre-wrap', 
              lineHeight: 1.8,
              color: 'text.primary',
              fontSize: '1.05rem'
            }}>
              {report?.body}
            </Typography>
          </CardContent>
        </Card>

        {/* User and Tasker Details Grid */}
        <Box sx={{ display: 'flex', flexDirection: 'row', gap: 3, mb: 4 }}>
          {/* User Details */}
          <Box sx={{ width: '100%' }}>
            <Card
              elevation={0}
              sx={{
                borderRadius: 1,
                width: '100%',
                background: 'rgba(255,255,255,0.95)',
                backdropFilter: 'blur(20px)',
                boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
                border: '2px solid rgba(102, 126, 234, 0.3)',
              }}
            >
              <Box sx={{ 
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                p: 2.5,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 2
              }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', width: 48, height: 48 }}>
                    <PersonIcon sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: 'white' }}>
                    User Details
                  </Typography>
                </Box>
                {report?.userSuspended && (
                  <Chip
                    label="SUSPENDED"
                    size="small"
                    sx={{
                      backgroundColor: '#f44336',
                      color: 'white',
                      fontWeight: 700,
                      fontSize: '0.7rem',
                      borderRadius: 0.5
                    }}
                  />
                )}
              </Box>
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <BadgeIcon sx={{ color: '#667eea', fontSize: 20 }} />
                    <Typography variant="overline" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      User ID
                    </Typography>
                  </Box>
                  <Typography variant="body1" sx={{ fontWeight: 600 }}>
                    {report?.userID}
                  </Typography>
                </Box>

                <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <PersonIcon sx={{ color: '#667eea', fontSize: 20 }} />
                    <Typography variant="overline" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      Username
                    </Typography>
                  </Box>
                  <Typography variant="body1" sx={{ fontWeight: 600 }}>
                    {report?.userUsername}
                  </Typography>
                </Box>

                <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <EmailIcon sx={{ color: '#667eea', fontSize: 20 }} />
                    <Typography variant="overline" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      Email
                    </Typography>
                  </Box>
                  <Typography variant="body1" sx={{ fontWeight: 600, wordBreak: 'break-word' }}>
                    {report?.userEmail}
                  </Typography>
                </Box>

                <Button
                  startIcon={<BlockIcon />}
                  variant="contained"
                  fullWidth
                  onClick={() => handleSuspendClick('user')}
                  disabled={suspendLoading}
                  sx={{
                    py: 1.5,
                    mt: 2,
                    backgroundColor: '#f44336',
                    color: 'white',
                    fontWeight: 700,
                    fontSize: '1rem',
                    borderRadius: 1,
                    '&:hover': {
                      backgroundColor: '#d32f2f',
                    }
                  }}
                >
                  Suspend User
                </Button>
              </CardContent>
            </Card>
          </Box>

          {/* Tasker Details */}
          <Box sx={{ width: '100%' }}>
            <Card
              elevation={0}
              sx={{
                borderRadius: 1,
                width: '100%',
                background: 'rgba(255,255,255,0.95)',
                backdropFilter: 'blur(20px)',
                boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
                border: '2px solid rgba(255, 152, 0, 0.3)',
              }}
            >
              <Box sx={{ 
                background: 'linear-gradient(135deg, #ff9800 0%, #f57c00 100%)',
                p: 2.5,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                gap: 2
              }}>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', width: 48, height: 48 }}>
                    <HandymanIcon sx={{ color: 'white' }} />
                  </Avatar>
                  <Typography variant="h6" sx={{ fontWeight: 700, color: 'white' }}>
                    Tasker Details
                  </Typography>
                </Box>
                {report?.taskerSuspended && (
                  <Chip
                    label="SUSPENDED"
                    size="small"
                    sx={{
                      backgroundColor: '#f44336',
                      color: 'white',
                      fontWeight: 700,
                      fontSize: '0.7rem',
                      borderRadius: 0.5
                    }}
                  />
                )}
              </Box>
              <CardContent sx={{ p: 3 }}>
                <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <BadgeIcon sx={{ color: '#ff9800', fontSize: 20 }} />
                    <Typography variant="overline" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      Tasker ID
                    </Typography>
                  </Box>
                  <Typography variant="body1" sx={{ fontWeight: 600 }}>
                    {report?.taskerID}
                  </Typography>
                </Box>

                <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <HandymanIcon sx={{ color: '#ff9800', fontSize: 20 }} />
                    <Typography variant="overline" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      Username
                    </Typography>
                  </Box>
                  <Typography variant="body1" sx={{ fontWeight: 600 }}>
                    {report?.taskerUsername}
                  </Typography>
                </Box>

                <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <EmailIcon sx={{ color: '#ff9800', fontSize: 20 }} />
                    <Typography variant="overline" sx={{ fontWeight: 600, color: 'text.secondary' }}>
                      Email
                    </Typography>
                  </Box>
                  <Typography variant="body1" sx={{ fontWeight: 600, wordBreak: 'break-word' }}>
                    {report?.taskerEmail}
                  </Typography>
                </Box>

                <Button
                  startIcon={<BlockIcon />}
                  variant="contained"
                  fullWidth
                  onClick={() => handleSuspendClick('tasker')}
                  disabled={suspendLoading}
                  sx={{
                    py: 1.5,
                    mt: 2,
                    backgroundColor: '#f44336',
                    color: 'white',
                    fontWeight: 700,
                    fontSize: '1rem',
                    borderRadius: 1,
                    '&:hover': {
                      backgroundColor: '#d32f2f',
                    }
                  }}
                >
                  Suspend Tasker
                </Button>
              </CardContent>
            </Card>
          </Box>
        </Box>
      </Box>

      {/* Suspend Confirmation Dialog */}
      <Dialog 
        open={suspendDialog.open} 
        onClose={handleSuspendCancel}
        PaperProps={{
          sx: {
            borderRadius: 1,
            minWidth: { xs: '90vw', sm: 400 },
          }
        }}
      >
        <DialogTitle sx={{ 
          fontWeight: 700, 
          fontSize: '1.5rem',
          background: 'linear-gradient(135deg, #f44336 0%, #d32f2f 100%)',
          color: 'white'
        }}>
          Confirm Suspension
        </DialogTitle>
        <DialogContent sx={{ mt: 3 }}>
          <Typography sx={{ fontSize: '1.1rem', mb: 2 }}>
            Are you sure you want to suspend this{' '}
            <strong>{suspendDialog.type === 'user' ? 'user' : 'tasker'}</strong>?
          </Typography>
          <Typography sx={{ mb: 3, color: 'text.secondary' }}>
            This action will restrict their access to the platform.
          </Typography>
          
          <TextField
            fullWidth
            label="Suspension Reason"
            placeholder="Enter the reason for suspension..."
            multiline
            rows={4}
            value={suspendReason}
            onChange={(e) => setSuspendReason(e.target.value)}
            variant="outlined"
            sx={{
              '& .MuiOutlinedInput-root': {
                borderRadius: 1,
                '&:hover fieldset': {
                  borderColor: '#f44336',
                },
                '&.Mui-focused fieldset': {
                  borderColor: '#f44336',
                },
              },
              '& .MuiInputBase-input::placeholder': {
                opacity: 0.7,
              },
            }}
          />
        </DialogContent>
        <DialogActions sx={{ p: 3, pt: 2 }}>
          <Button 
            onClick={handleSuspendCancel} 
            disabled={suspendLoading}
            variant="outlined"
            sx={{ fontWeight: 600, borderRadius: 1 }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleSuspendConfirm}
            variant="contained"
            color="error"
            disabled={suspendLoading}
            sx={{ fontWeight: 600, px: 3, borderRadius: 1 }}
          >
            {suspendLoading ? <CircularProgress size={24} color="inherit" /> : 'Suspend'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Success Snackbar */}
      <Snackbar
        open={Boolean(successMessage)}
        autoHideDuration={6000}
        onClose={() => setSuccessMessage('')}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert 
          onClose={() => setSuccessMessage('')} 
          severity="success" 
          sx={{ 
            width: '100%',
            borderRadius: 1,
            fontWeight: 600
          }}
          icon={<CheckCircleIcon />}
        >
          {successMessage}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ReportDetailsPage;