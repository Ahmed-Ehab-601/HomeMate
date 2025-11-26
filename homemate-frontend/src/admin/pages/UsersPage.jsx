import { useCallback, useEffect, useMemo, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { Alert, Snackbar, Button, Box } from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import BulkActionDialog from '../components/common/BulkActionDialog';
import PageHeader from '../components/common/PageHeader';
import UserFilters from '../components/filters/UserFilters';
import UserTable from '../components/tables/UserTable';
import { getUsers, suspendUser, promoteUser, demoteUser, reactiveUser, promoteUsers, demoteUsers, suspendUsers, reactiveUsers } from '../services/adminService';
import SuspensionReasonDialog from '../components/common/SuspensionReasonDialog';

const booleanOrUndefined = (value) => {
  if (value === '') return undefined;
  if (value === 'true') return true;
  if (value === 'false') return false;
  return value;
};

const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [searchParams, setSearchParams] = useSearchParams();
  const LOCAL_STORAGE_KEY = 'homemate.users.filters';
  const location = useLocation();

  const initialFilters = {
    admin: searchParams.get('admin') ?? '',
    suspended: searchParams.get('suspended') ?? '',
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

  const normalizedFilters = useMemo(
    () => ({
      admin: booleanOrUndefined(filters.admin),
      suspended: booleanOrUndefined(filters.suspended),
      username: filters.username || undefined,
    }),
    [filters],
  );

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getUsers({
        page: pagination.page,
        size: pagination.size,
        filters: normalizedFilters,
      });
      setUsers(response?.items ?? []);
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
    fetchUsers();
  }, [fetchUsers]);

  // on mount: try restoring from localStorage (localStorage takes precedence)
  useEffect(() => {
    try {
      const saved = localStorage.getItem(LOCAL_STORAGE_KEY);
      console.debug('UsersPage: restoring from localStorage key', LOCAL_STORAGE_KEY, 'raw=', saved);
      if (saved) {
        const parsed = JSON.parse(saved);
        console.debug('UsersPage: parsed stored filters', parsed);
        if (parsed?.filters) setFilters((prev) => ({ ...prev, ...parsed.filters }));
        if (parsed?.pagination) {
          setPagination((prev) => ({ ...prev, page: Number(parsed.pagination.page ?? prev.page), size: Number(parsed.pagination.size ?? prev.size) }));
        }

        // update URL so back/forward and bookmarking reflect restored filters
        const nextParams = { ...Object.fromEntries([...searchParams]) };
        if (parsed?.filters) {
          if (parsed.filters.admin !== undefined && parsed.filters.admin !== '') nextParams.admin = parsed.filters.admin;
          if (parsed.filters.suspended !== undefined && parsed.filters.suspended !== '') nextParams.suspended = parsed.filters.suspended;
          if (parsed.filters.username !== undefined && parsed.filters.username !== '') nextParams.username = parsed.filters.username;
        }
        if (parsed?.pagination) {
          nextParams.page = parsed.pagination.page ?? 0;
          nextParams.size = parsed.pagination.size ?? nextParams.size;
        }
        console.debug('UsersPage: updating URL search params with restored values', nextParams);
        setSearchParams(nextParams, { replace: true });
      }
    } catch (e) {
      console.debug('UsersPage: failed to restore from localStorage', e);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // also restore when navigating back to this route (client-side navigation)
  // Force-restore stored filters from localStorage when route becomes active.
  useEffect(() => {
    if (!location || !location.pathname) return;
    if (!location.pathname.includes('/users')) return;

    try {
      const saved = localStorage.getItem(LOCAL_STORAGE_KEY);
      console.debug('UsersPage: route-activated force restore, raw=', saved);
      if (saved) {
        const parsed = JSON.parse(saved);
        console.debug('UsersPage: force-restored parsed', parsed);
        if (parsed?.filters) setFilters((prev) => ({ ...prev, ...parsed.filters }));
        if (parsed?.pagination) setPagination((prev) => ({ ...prev, page: Number(parsed.pagination.page ?? prev.page), size: Number(parsed.pagination.size ?? prev.size) }));
        const nextParams = { ...Object.fromEntries([...searchParams]) };
        if (parsed?.filters) {
          nextParams.admin = parsed.filters.admin ?? '';
          nextParams.suspended = parsed.filters.suspended ?? '';
          nextParams.username = parsed.filters.username ?? '';
        }
        if (parsed?.pagination) {
          nextParams.page = parsed.pagination.page ?? 0;
          nextParams.size = parsed.pagination.size ?? nextParams.size;
        }
        console.debug('UsersPage: replacing URL search params with', nextParams);
        setSearchParams(nextParams, { replace: true });
      }
    } catch (e) {
      console.debug('UsersPage: route force restore failed', e);
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [location.pathname]);

  // persist filters + pagination to localStorage so they are available when navigating away
  useEffect(() => {
    try {
      const toSave = { filters, pagination: { page: pagination.page, size: pagination.size } };
      localStorage.setItem(LOCAL_STORAGE_KEY, JSON.stringify(toSave));
      console.debug('UsersPage: saved filters to localStorage', LOCAL_STORAGE_KEY, toSave);
    } catch (e) {
      console.debug('UsersPage: failed to save filters', e);
    }
  }, [filters, pagination.page, pagination.size]);

  // sync filters/pagination when URL search params change (back/forward navigation or external links)
  useEffect(() => {
    setFilters({
      admin: searchParams.get('admin') ?? '',
      suspended: searchParams.get('suspended') ?? '',
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
    // persist filters to URL
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
      console.debug('UsersPage: immediate save to localStorage', toSave);
    } catch (e) {
      console.debug('UsersPage: failed immediate save', e);
    }
  };

  const handleClearFilters = () => {
    setFilters({ admin: '', suspended: '', username: '' });
    setPagination((prev) => ({ ...prev, page: 0 }));
    const next = { ...Object.fromEntries([...searchParams]) };
    delete next.admin;
    delete next.suspended;
    delete next.username;
    next.page = 0;
    setSearchParams(next);
    try {
      localStorage.removeItem(LOCAL_STORAGE_KEY);
      console.debug('UsersPage: removed localStorage entry on clear');
    } catch (e) {
      console.debug('UsersPage: failed to remove localStorage entry', e);
    }
  };

  const handleStatusFilter = (suspendedValue) => {
    handleFilterChange('suspended', suspendedValue);
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
      console.debug('UsersPage: saved page change to localStorage', toSave);
    } catch (e) {
      console.debug('UsersPage: failed to save page change', e);
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
      console.debug('UsersPage: saved rows-per-page to localStorage', toSave);
    } catch (e) {
      console.debug('UsersPage: failed to save rows-per-page', e);
    }
  };

  const handleSuspend = async (user) => {
    try {
      if (user.suspended) {
        await reactiveUser(user.userId);
        fetchUsers();
        return;
      }
      // open dialog to collect reason then suspend
      setSuspendTarget({ mode: 'single', ids: [user.userId] });
      setSuspendDialogOpen(true);
    } catch (err) {
      setError(err.message);
    }
  };

  const handlePromote = async (user) => {
    try {
      await promoteUser(user.userId);
      fetchUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDemote = async (user) => {
    try {
      await demoteUser(user.userId);
      fetchUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  // selection helpers for bulk actions
  const onToggleSelect = (id) => {
    setSelectedIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

  const onSelectAll = (checked) => {
    if (checked) setSelectedIds(users.map((u) => u.userId));
    else setSelectedIds([]);
  };

  const handleBulkPromote = async () => {
    if (!selectedIds.length) return;
    try {
      await promoteUsers(selectedIds);
      setSelectedIds([]);
      fetchUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleActionForSelected = async (action, reason = '') => {
    if (!selectedIds.length) return;
    try {
      if (action === 'promote') {
        await promoteUsers(selectedIds);
      } else if (action === 'demote') {
        await demoteUsers(selectedIds);
      } else if (action === 'suspend') {
        await suspendUsers(selectedIds, reason || '');
      } else if (action === 'reactive') {
        await reactiveUsers(selectedIds);
      }
      setSelectedIds([]);
      setActionDialogOpen(false);
      fetchUsers();
    } catch (err) {
      setError(err.message);
    }
  };

  return (
    <>
      <PageHeader
        title="Users"
        subtitle="Monitor platform users"
      />
      <UserFilters values={filters} onChange={handleFilterChange} onClear={handleClearFilters} onSearch={fetchUsers} />
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 1 }}>
        <Button variant="contained" color="primary" size="small" disabled={!selectedIds.length} onClick={() => setActionDialogOpen(true)}>
          Action for selected
        </Button>
      </Box>
      <BulkActionDialog
        open={actionDialogOpen}
        onClose={() => setActionDialogOpen(false)}
        onConfirm={handleActionForSelected}
        type="user"
        count={selectedIds.length}
      />
      <UserTable
        rows={users}
        loading={loading}
        pagination={pagination}
        onPageChange={handlePageChange}
        onRowsPerPageChange={handleRowsPerPageChange}
        onSuspend={handleSuspend}
        onPromote={handlePromote}
        onDemote={handleDemote}
        onStatusFilter={handleStatusFilter}
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
        title="Suspend User(s)"
        description="Provide an optional reason for suspending the selected user(s)."
        onConfirm={async (reason) => {
          try {
            if (!suspendTarget) return;
            if (suspendTarget.mode === 'single') {
              await suspendUser(suspendTarget.ids[0], true, reason || '');
            } else {
              await suspendUsers(suspendTarget.ids, reason || '');
            }
            setSuspendDialogOpen(false);
            setSuspendTarget(null);
            fetchUsers();
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

export default UsersPage;

