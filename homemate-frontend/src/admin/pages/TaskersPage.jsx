import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Snackbar } from '@mui/material';
import PageHeader from '../components/common/PageHeader';
import TaskerFilters from '../components/filters/TaskerFilters';
import TaskerTable from '../components/tables/TaskerTable';
import { getTaskers, suspendTasker } from '../services/adminService';

const normalizeFilters = (filters) => ({
  suspended:
    filters.suspended === ''
      ? undefined
      : filters.suspended === 'true'
        ? true
        : filters.suspended === 'false'
          ? false
          : filters.suspended,
  minRate: filters.minRate || undefined,
});

const TaskersPage = () => {
  const [taskers, setTaskers] = useState([]);
  const [filters, setFilters] = useState({ suspended: '', minRate: '' });
  const [pagination, setPagination] = useState({ page: 0, size: 10, totalItems: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const normalizedFilters = useMemo(() => normalizeFilters(filters), [filters]);

  const fetchTaskers = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getTaskers({
        page: pagination.page,
        size: pagination.size,
        filters: normalizedFilters,
      });
      setTaskers(response?.items ?? []);
      setPagination((prev) => ({
        ...prev,
        totalItems: response?.totalItems ?? response?.items?.length ?? 0,
        totalPages: response?.totalPages ?? 0,
      }));
      setError('');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [pagination.page, pagination.size, normalizedFilters]);

  useEffect(() => {
    fetchTaskers();
  }, [fetchTaskers]);

  const handleFilterChange = (field, value) => {
    setFilters((prev) => ({ ...prev, [field]: value }));
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const handleClearFilters = () => {
    setFilters({ suspended: '', minRate: '' });
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const handlePageChange = (page) => {
    setPagination((prev) => ({ ...prev, page }));
  };

  const handleRowsPerPageChange = (size) => {
    setPagination({ page: 0, size, totalItems: pagination.totalItems });
  };

  const handleSuspend = async (tasker) => {
    try {
      await suspendTasker(tasker.taskerID, !tasker.suspended);
      fetchTaskers();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <>
      <PageHeader
        title="Taskers"
        subtitle="Manage service providers"
      />
      <TaskerFilters values={filters} onChange={handleFilterChange} onClear={handleClearFilters} />
      <TaskerTable
        rows={taskers}
        loading={loading}
        pagination={pagination}
        onPageChange={handlePageChange}
        onRowsPerPageChange={handleRowsPerPageChange}
        onSuspend={handleSuspend}
      />
      <Snackbar
        open={Boolean(error)}
        autoHideDuration={6000}
        onClose={() => setError('')}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="error" onClose={() => setError('')} sx={{ width: '100%' }}>
          {error}
        </Alert>
      </Snackbar>
    </>
  );
};

export default TaskersPage;

