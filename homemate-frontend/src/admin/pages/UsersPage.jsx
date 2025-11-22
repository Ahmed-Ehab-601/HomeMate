import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Snackbar, Button, Box } from '@mui/material';
import { useSearchParams } from 'react-router-dom';
import BulkActionDialog from '../components/common/BulkActionDialog';
import PageHeader from '../components/common/PageHeader';
import UserFilters from '../components/filters/UserFilters';
import UserTable from '../components/tables/UserTable';
import { getUsers, suspendUser, promoteUser, demoteUser, reactiveUser, promoteUsers, demoteUsers, suspendUsers, reactiveUsers } from '../services/adminService';

const booleanOrUndefined = (value) => {
  if (value === '') return undefined;
  if (value === 'true') return true;
  if (value === 'false') return false;
  return value;
};

const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [searchParams, setSearchParams] = useSearchParams();

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

  const handleFilterChange = (field, value) => {
    setFilters((prev) => ({ ...prev, [field]: value }));
    setPagination((prev) => ({ ...prev, page: 0 }));
    // persist filters to URL
    const next = {
      ...Object.fromEntries([...searchParams]),
      [field]: value,
      page: 0,
    };
    if (next[field] === '') delete next[field];
    setSearchParams(next);
  };

  const handleClearFilters = () => {
    setFilters({ admin: '', suspended: '', username: '' });
    setPagination((prev) => ({ ...prev, page: 0 }));
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

  const handleSuspend = async (user) => {
    try {
      if (user.suspended) {
        await reactiveUser(user.userId);
      } else {
        await suspendUser(user.userId);
      }
      fetchUsers();
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

  const handleActionForSelected = async (action) => {
    if (!selectedIds.length) return;
    try {
      if (action === 'promote') {
        await promoteUsers(selectedIds);
      } else if (action === 'demote') {
        await demoteUsers(selectedIds);
      } else if (action === 'suspend') {
        await suspendUsers(selectedIds);
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

