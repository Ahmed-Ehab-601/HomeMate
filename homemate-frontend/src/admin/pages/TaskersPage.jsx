import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Alert, Snackbar, Button } from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import PageHeader from '../components/common/PageHeader';
import TaskerFilters from '../components/filters/TaskerFilters';
import TaskerTable from '../components/tables/TaskerTable';
import { getTaskers, suspendTasker, reactiveTasker, suspendTaskers, reactiveTaskers } from '../services/adminService';
import SuspensionReasonDialog from '../components/common/SuspensionReasonDialog';
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
  const LOCAL_STORAGE_KEY = 'homemate.taskers.filters';
  const location = useLocation();

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
  const [suspendDialogOpen, setSuspendDialogOpen] = useState(false);
  const [suspendTarget, setSuspendTarget] = useState(null);

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

  // on mount: try restoring from localStorage (localStorage takes precedence)
  useEffect(() => {
    try {
      const saved = localStorage.getItem(LOCAL_STORAGE_KEY);
      console.debug('TaskersPage: restoring from localStorage key', LOCAL_STORAGE_KEY, 'raw=', saved);
      if (saved) {
        const parsed = JSON.parse(saved);
        console.debug('TaskersPage: parsed stored filters', parsed);
        if (parsed?.filters) setFilters((prev) => ({ ...prev, ...parsed.filters }));
        if (parsed?.pagination) {
          setPagination((prev) => ({ ...prev, page: Number(parsed.pagination.page ?? prev.page), size: Number(parsed.pagination.size ?? prev.size) }));
        }

        const nextParams = { ...Object.fromEntries([...searchParams]) };
        if (parsed?.filters) {
          if (parsed.filters.suspended !== undefined && parsed.filters.suspended !== '') nextParams.suspended = parsed.filters.suspended;
          if (parsed.filters.minRate !== undefined && parsed.filters.minRate !== '') nextParams.minRate = parsed.filters.minRate;
          if (parsed.filters.username !== undefined && parsed.filters.username !== '') nextParams.username = parsed.filters.username;
        }
        if (parsed?.pagination) {
          nextParams.page = parsed.pagination.page ?? 0;
          nextParams.size = parsed.pagination.size ?? nextParams.size;
        }
        console.debug('TaskersPage: updating URL search params with restored values', nextParams);
        setSearchParams(nextParams, { replace: true });
      }
    } catch (e) {
      console.debug('TaskersPage: failed to restore from localStorage', e);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // also restore when navigating back to this route (client-side navigation)
  // Force-restore stored filters from localStorage when route becomes active.
  useEffect(() => {
    if (!location || !location.pathname) return;
    if (!location.pathname.includes('/taskers')) return;

    try {
      const saved = localStorage.getItem(LOCAL_STORAGE_KEY);
      console.debug('TaskersPage: route-activated force restore, raw=', saved);
      if (saved) {
        const parsed = JSON.parse(saved);
        console.debug('TaskersPage: force-restored parsed', parsed);
        if (parsed?.filters) setFilters((prev) => ({ ...prev, ...parsed.filters }));
        if (parsed?.pagination) setPagination((prev) => ({ ...prev, page: Number(parsed.pagination.page ?? prev.page), size: Number(parsed.pagination.size ?? prev.size) }));
        const nextParams = { ...Object.fromEntries([...searchParams]) };
        if (parsed?.filters) {
          nextParams.suspended = parsed.filters.suspended ?? '';
          nextParams.minRate = parsed.filters.minRate ?? '';
          nextParams.username = parsed.filters.username ?? '';
        }
        if (parsed?.pagination) {
          nextParams.page = parsed.pagination.page ?? 0;
          nextParams.size = parsed.pagination.size ?? nextParams.size;
        }
        console.debug('TaskersPage: replacing URL search params with', nextParams);
        setSearchParams(nextParams, { replace: true });
      }
    } catch (e) {
      console.debug('TaskersPage: route force restore failed', e);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname]);

  // persist filters + pagination to localStorage so they are available when navigating away
  useEffect(() => {
    try {
      const toSave = { filters, pagination: { page: pagination.page, size: pagination.size } };
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(toSave));
      console.debug('TaskersPage: saved filters to localStorage', LOCAL_STORAGE_KEY, toSave);
    } catch (e) {
      console.debug('TaskersPage: failed to save filters', e);
    }
  }, [filters, pagination.page, pagination.size]);

  // sync filters/pagination when URL search params change (back/forward navigation or external links)
  useEffect(() => {
    setFilters({
      suspended: searchParams.get('suspended') ?? '',
      minRate: searchParams.get('minRate') ?? '',
      username: searchParams.get('username') ?? '',
    });
    setPagination((prev) => ({
      ...prev,
      page: Number(searchParams.get('page') ?? prev.page),
      size: Number(searchParams.get('size') ?? prev.size),
    }));
  }, [searchParams]);

  const handleFilterChange = (field, value) => {
    const newFilters = { ...filters, [field]: value };
    setFilters(newFilters);
    setPagination((prev) => ({ ...prev, page: 0 }));
    const next = {
      ...Object.fromEntries([...searchParams]),
      [field]: value,
      page: 0,
    };
    if (next[field] === '') delete next[field];
    setSearchParams(next);
    try {
      const toSave = { filters: newFilters, pagination: { page: 0, size: pagination.size } };
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(toSave));
      console.debug('TaskersPage: immediate save to localStorage', toSave);
    } catch (e) {
      console.debug('TaskersPage: failed immediate save', e);
    }
  };

  const handleClearFilters = () => {
    setFilters({ suspended: '', minRate: '', username: '' });
    setPagination((prev) => ({ ...prev, page: 0 }));
    const next = { ...Object.fromEntries([...searchParams]) };
    delete next.suspended;
    delete next.minRate;
    delete next.username;
    next.page = 0;
    setSearchParams(next);
    try {
      localStorage.removeItem(LOCAL_STORAGE_KEY);
      console.debug('TaskersPage: removed localStorage entry on clear');
    } catch (e) {
      console.debug('TaskersPage: failed to remove localStorage entry', e);
    }
  };

  const handlePageChange = (page) => {
    setPagination((prev) => ({ ...prev, page }));
    const next = {
      ...Object.fromEntries([...searchParams]),
      page,
    };
    setSearchParams(next);
    try {
      const toSave = { filters, pagination: { page, size: pagination.size } };
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(toSave));
      console.debug('TaskersPage: saved page change to localStorage', toSave);
    } catch (e) {
      console.debug('TaskersPage: failed to save page change', e);
    }
  };

  const handleRowsPerPageChange = (size) => {
    setPagination({ page: 0, size, totalItems: pagination.totalItems });
    const next = {
      ...Object.fromEntries([...searchParams]),
      size,
      page: 0,
    };
    setSearchParams(next);
    try {
      const toSave = { filters, pagination: { page: 0, size } };
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(toSave));
      console.debug('TaskersPage: saved rows-per-page to localStorage', toSave);
    } catch (e) {
      console.debug('TaskersPage: failed to save rows-per-page', e);
    }
  };

  const handleSuspend = async (tasker) => {
    try {
      if (tasker.suspended) {
        await reactiveTasker(tasker.taskerID);
        fetchTaskers();
        return;
      }
      setSuspendTarget({ mode: 'single', ids: [tasker.taskerID] });
      setSuspendDialogOpen(true);
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

  const handleActionForSelected = async (action, reason = '') => {
    if (!selectedIds.length) return;
    try {
      if (action === 'suspend') {
        await suspendTaskers(selectedIds, reason || '');
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
      <SuspensionReasonDialog
        open={suspendDialogOpen}
        onClose={() => {
          setSuspendDialogOpen(false);
          setSuspendTarget(null);
        }}
        title="Suspend Tasker(s)"
        description="Provide an optional reason for suspending the selected tasker(s)."
        onConfirm={async (reason) => {
          try {
            if (!suspendTarget) return;
            if (suspendTarget.mode === 'single') {
              await suspendTasker(suspendTarget.ids[0], true, reason || '');
            } else {
              await suspendTaskers(suspendTarget.ids, reason || '');
            }
            setSuspendDialogOpen(false);
            setSuspendTarget(null);
            fetchTaskers();
          } catch (err) {
            setError(err.message);
          }
        }}
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

