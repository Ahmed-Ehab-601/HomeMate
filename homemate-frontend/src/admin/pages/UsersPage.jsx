import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Snackbar } from '@mui/material';
import PageHeader from '../components/common/PageHeader';
import UserFilters from '../components/filters/UserFilters';
import UserTable from '../components/tables/UserTable';
import { getUsers, suspendUser } from '../services/adminService';

const booleanOrUndefined = (value) => {
  if (value === '') return undefined;
  if (value === 'true') return true;
  if (value === 'false') return false;
  return value;
};

const UsersPage = () => {
  const [users, setUsers] = useState([]);
  const [filters, setFilters] = useState({ admin: '', suspended: '', username: '' });
  const [pagination, setPagination] = useState({ page: 0, size: 10, totalItems: 0 });
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
  };

  const handleRowsPerPageChange = (size) => {
    setPagination({ page: 0, size, totalItems: pagination.totalItems });
  };

  const handleSuspend = async (user) => {
    try {
      await suspendUser(user.userId, !user.suspended);
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
      <UserTable
        rows={users}
        loading={loading}
        pagination={pagination}
        onPageChange={handlePageChange}
        onRowsPerPageChange={handleRowsPerPageChange}
        onSuspend={handleSuspend}
        onStatusFilter={handleStatusFilter}
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

