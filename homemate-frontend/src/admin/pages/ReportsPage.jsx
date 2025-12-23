import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Alert, Snackbar, Dialog, DialogTitle, DialogContent, DialogActions, TextField, Button } from '@mui/material';
import PageHeader from '../components/common/PageHeader';
import ReportFilters from '../components/filters/ReportFilters';
import ReportTable from '../components/tables/ReportTable';
import { getReports, respondToReport } from '../services/adminService';

const ReportsPage = () => {
  const navigate = useNavigate();
  const [reports, setReports] = useState([]);
  const [filters, setFilters] = useState({
    header: '',
    body: '',
    taskID: '',
    reporterType: 'all',
    status: 'all',
  });
  const [appliedFilters, setAppliedFilters] = useState({});
  const [pagination, setPagination] = useState({
    page: 0,
    size: 20,
    totalItems: 0,
    totalPages: 0,
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // Response Dialog State
  const [respondDialogOpen, setRespondDialogOpen] = useState(false);
  const [selectedReportId, setSelectedReportId] = useState(null);
  const [responseMessage, setResponseMessage] = useState('');
  const [sendingResponse, setSendingResponse] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  const fetchReports = useCallback(async () => {
    setLoading(true);
    try {
      // Build filter params, excluding empty/default values
      const filterParams = {};

      if (appliedFilters.header && appliedFilters.header.trim()) {
        filterParams.header = appliedFilters.header.trim();
      }

      if (appliedFilters.body && appliedFilters.body.trim()) {
        filterParams.body = appliedFilters.body.trim();
      }

      if (appliedFilters.taskID && appliedFilters.taskID.trim()) {
        filterParams.taskID = parseInt(appliedFilters.taskID, 10);
      }

      if (appliedFilters.reporterType && appliedFilters.reporterType !== 'all') {
        // Convert 'user' to true, 'tasker' to false
        filterParams.reporter = appliedFilters.reporterType === 'user';
      }

      if (appliedFilters.status && appliedFilters.status !== 'all') {
        filterParams.adminStatus = appliedFilters.status;
      }

      const response = await getReports({
        pageNumber: pagination.page,
        pageSize: pagination.size,
        ...filterParams,
      });

      setReports(response?.data || []);
      setPagination((prev) => ({
        ...prev,
        totalItems: response?.totalElements || 0,
        totalPages: response?.totalPages || 0,
      }));
      setError('');
    } catch (err) {
      console.error('Error fetching reports:', err);
      setError(err.message || 'Failed to fetch reports');
    } finally {
      setLoading(false);
    }
  }, [pagination.page, pagination.size, appliedFilters]);

  useEffect(() => {
    fetchReports();
  }, [fetchReports]);

  const handleFilterChange = (field, value) => {
    setFilters((prev) => ({ ...prev, [field]: value }));
  };

  const handleApplyFilters = () => {
    setAppliedFilters(filters);
    setPagination((prev) => ({ ...prev, page: 0 })); // Reset to first page
  };

  const handlePageChange = (newPage) => {
    setPagination((prev) => ({ ...prev, page: newPage }));
  };

  const handleRowsPerPageChange = (newSize) => {
    setPagination((prev) => ({ ...prev, size: newSize, page: 0 }));
  };

  const handleViewDetails = (reportId) => {
    navigate(`/admin/reports/${reportId}`);
  };

  const handleOpenRespond = (reportId) => {
    setSelectedReportId(reportId);
    setResponseMessage('');
    setRespondDialogOpen(true);
  };

  const handleCloseRespond = () => {
    setRespondDialogOpen(false);
    setSelectedReportId(null);
    setResponseMessage('');
  };

  const handleSendResponse = async () => {
    if (!responseMessage.trim()) return;

    setSendingResponse(true);
    try {
      await respondToReport(selectedReportId, responseMessage);
      setSuccessMessage('Response sent successfully');
      handleCloseRespond();
    } catch (err) {
      console.error('Error sending response:', err);
      setError(err.message || 'Failed to send response');
    } finally {
      setSendingResponse(false);
    }
  };

  return (
    <div style={{ padding: '24px', maxWidth: '100%' }}>
      <PageHeader title="Reports Management" subtitle="View and manage all reports" />

      <ReportFilters
        filters={filters}
        onFilterChange={handleFilterChange}
        onApplyFilters={handleApplyFilters}
      />

      <ReportTable
        rows={reports}
        loading={loading}
        pagination={pagination}
        onPageChange={handlePageChange}
        onRowsPerPageChange={handleRowsPerPageChange}
        onViewDetails={handleViewDetails}
        onRespond={handleOpenRespond}
      />

      <Snackbar
        open={Boolean(error)}
        autoHideDuration={6000}
        onClose={() => setError('')}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={() => setError('')} severity="error" sx={{ width: '100%' }}>
          {error}
        </Alert>
      </Snackbar>

      <Snackbar
        open={Boolean(successMessage)}
        autoHideDuration={6000}
        onClose={() => setSuccessMessage('')}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert onClose={() => setSuccessMessage('')} severity="success" sx={{ width: '100%' }}>
          {successMessage}
        </Alert>
      </Snackbar>

      <Dialog open={respondDialogOpen} onClose={handleCloseRespond} maxWidth="sm" fullWidth>
        <DialogTitle>Respond to Report #{selectedReportId}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            id="response"
            label="Email Message"
            type="text"
            fullWidth
            multiline
            rows={4}
            variant="outlined"
            value={responseMessage}
            onChange={(e) => setResponseMessage(e.target.value)}
            inputProps={{ maxLength: 200 }}
            helperText={`${responseMessage.length}/200 characters`}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseRespond} disabled={sendingResponse}>Cancel</Button>
          <Button onClick={handleSendResponse} disabled={!responseMessage.trim() || sendingResponse} variant="contained">
            {sendingResponse ? 'Sending...' : 'Send'}
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default ReportsPage;
