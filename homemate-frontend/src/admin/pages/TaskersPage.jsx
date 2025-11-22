import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Snackbar, Button } from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import TaskerFilters from '../components/filters/TaskerFilters';
import TaskerTable from '../components/tables/TaskerTable';
import { getTaskers, suspendTasker, reactiveTasker, suspendTaskers, reactiveTaskers } from '../services/adminService';
import BulkActionDialog from '../components/common/BulkActionDialog';

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
  username: filters.username || undefined,
});

const TaskersPage = () => {
  const [taskers, setTaskers] = useState([]);
  const [searchParams, setSearchParams] = useSearchParams();

  const initialFilters = {
    suspended: searchParams.get('suspended') ?? '',
    minRate: searchParams.get('minRate') ?? '',
    username: searchParams.get('username') ?? '',
  };

  const [filters, setFilters] = useState(initialFilters);
  const [pagination, setPagination] = useState({
    page: Number(searchParams.get('page') ?? 0),
    size: Number(searchParams.get('size') ?? 10),
    totalItems: 0,
  });
  const [selectedIds, setSelectedIds] = useState([]);
  const [actionDialogOpen, setActionDialogOpen] = useState(false);
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
    const next = {
      ...Object.fromEntries([...searchParams]),
      [field]: value,
      page: 0,
    };
    if (next[field] === '') delete next[field];
    setSearchParams(next);
  };

  const handleClearFilters = () => {
    setFilters({ suspended: '', minRate: '', username: '' });
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const handlePageChange = (page) => {
    setPagination((prev) => ({ ...prev, page }));
    const next = {
      ...Object.fromEntries([...searchParams]),
      page,
    };
    setSearchParams(next);
  };

  const handleRowsPerPageChange = (size) => {
    setPagination({ page: 0, size, totalItems: pagination.totalItems });
    const next = {
      ...Object.fromEntries([...searchParams]),
      size,
      page: 0,
    };
    setSearchParams(next);
  };

  const handleSuspend = async (tasker) => {
    try {
      if (tasker.suspended) {
        await reactiveTasker(tasker.taskerID);
      } else {
        await suspendTasker(tasker.taskerID);
      }
      fetchTaskers();
    } catch (err) {
      setError(err.message);
    }
  };

  const onToggleSelect = (id) => {
    setSelectedIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const onSelectAll = (checked) => {
    if (checked) setSelectedIds(taskers.map((t) => t.taskerID));
    else setSelectedIds([]);
  };

  const handleActionForSelected = async (action) => {
    if (!selectedIds.length) return;
    try {
      if (action === 'suspend') {
        await suspendTaskers(selectedIds);
      } else if (action === 'reactive') {
        await reactiveTaskers(selectedIds);
      }
      setSelectedIds([]);
      setActionDialogOpen(false);
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
      <TaskerFilters values={filters} onChange={handleFilterChange} onClear={handleClearFilters} onSearch={fetchTaskers} />
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 8, marginBottom: 8 }}>
        <Button variant="contained" size="small" disabled={!selectedIds.length} onClick={() => setActionDialogOpen(true)}>
          Action for selected
        </Button>
      </div>
      <BulkActionDialog
        open={actionDialogOpen}
        onClose={() => setActionDialogOpen(false)}
        onConfirm={handleActionForSelected}
        type="tasker"
        count={selectedIds.length}
      />
      <TaskerTable
        rows={taskers}
        loading={loading}
        pagination={pagination}
        onPageChange={handlePageChange}
        onRowsPerPageChange={handleRowsPerPageChange}
        onSuspend={handleSuspend}
        selectedIds={selectedIds}
        onToggleSelect={onToggleSelect}
        onSelectAll={onSelectAll}
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

