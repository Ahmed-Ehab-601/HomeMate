import React from 'react';
import {
  Card,
  CardContent,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  IconButton,
  Checkbox,
  Chip,
  Tooltip,
  LinearProgress,
  Stack,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import ArrowUpwardIcon from '@mui/icons-material/ArrowUpward';
import ArrowDownwardIcon from '@mui/icons-material/ArrowDownward';

const UserTable = ({
  rows = [],
  loading,
  pagination,
  onPageChange,
  onRowsPerPageChange,
  onSuspend,
  onPromote,
  onDemote,
  onStatusFilter,
  selectedIds,
  onToggleSelect,
  onSelectAll,
}) => (
  <Card
    elevation={0}
    sx={{
      borderRadius: { xs: 0, sm: 4 },
      border: (theme) =>
        theme.palette.mode === 'dark'
          ? '1px solid rgba(255,255,255,0.08)'
          : '1px solid rgba(215,240,74,0.1)',
      boxShadow: (theme) =>
        theme.palette.mode === 'dark'
          ? '0 20px 35px rgba(0,0,0,0.6)'
          : '0 20px 35px rgba(15,15,15,0.05)',
      width: '100%',
      maxWidth: '100%',
      margin: 0,
      boxSizing: 'border-box',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column',
      px: 0,
    }}
  >
    {loading && <LinearProgress />}
    <CardContent sx={{ p: 0, display: 'flex', flexDirection: 'column', width: '100%' }}>
      <div style={{ width: '100%', overflowX: 'auto' }}>
        <TableContainer
          sx={{
            width: '100%',
            overflowX: 'auto',
            '&::-webkit-scrollbar': {
              height: '8px',
            },
            '&::-webkit-scrollbar-track': {
              backgroundColor: 'rgba(0,0,0,0.05)',
            },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: 'rgba(215,240,74,0.3)',
              borderRadius: '4px',
            },
          }}
        >
          <Table
            size="small"
            sx={{
              width: '100%',
              tableLayout: 'fixed',
              '& .MuiTableCell-root': {
                color: (theme) =>
                  theme.palette.mode === 'dark'
                    ? 'rgba(255,255,255,0.92)'
                    : 'inherit',
                fontWeight: (theme) => (theme.palette.mode === 'dark' ? 500 : 'inherit'),
                WebkitFontSmoothing: 'antialiased',
                MozOsxFontSmoothing: 'grayscale',
                px: { xs: 1, sm: 2 },
                py: { xs: 0.6, sm: 1 },
                fontSize: { xs: '0.75rem', sm: '0.875rem' },
                overflow: 'hidden',
                textOverflow: 'ellipsis',
                whiteSpace: 'nowrap',
              },
              '& .MuiTableHead-root .MuiTableCell-root': {
                color: (theme) =>
                  theme.palette.mode === 'dark' ? '#ffffff' : 'inherit',
                fontWeight: 600,
                fontSize: { xs: '0.75rem', sm: '0.9rem' },
              },
            }}
          >
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox
                    size="small"
                    indeterminate={Boolean(selectedIds && selectedIds.length && selectedIds.length < rows.length)}
                    checked={Boolean(rows.length && selectedIds && selectedIds.length === rows.length)}
                    onChange={(e) => {
                      if (onSelectAll) onSelectAll(e.target.checked);
                    }}
                    inputProps={{ 'aria-label': 'select all users' }}
                    sx={(theme) => ({
                      ml: 0.5,
                      '& .MuiSvgIcon-root': { fontSize: '1.15rem' },
                      color: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.85)' : 'rgba(0,0,0,0.65)',
                      '&.Mui-checked': { color: theme.palette.primary.main },
                      '&:hover': { backgroundColor: 'transparent' },
                    })}
                  />
                </TableCell>
                <TableCell sx={{ width: '18%' }}>Name</TableCell>
                <TableCell sx={{ width: '30%' }}>Email</TableCell>
                <TableCell sx={{ width: '14%' }}>Username</TableCell>
                <TableCell sx={{ width: '12%' }}>Phone</TableCell>
                <TableCell sx={{ width: '10%' }}>Admin</TableCell>
                <TableCell sx={{ width: '10%' }}>Status</TableCell>
                <TableCell align="right" sx={{ width: '6%' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row) => (
                <TableRow
                  key={row.userId}
                  hover
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell padding="checkbox">
                    <Checkbox
                      size="small"
                      checked={Boolean(selectedIds && selectedIds.includes(row.userId))}
                      onChange={() => onToggleSelect && onToggleSelect(row.userId)}
                      inputProps={{ 'aria-label': `select user ${row.userId}` }}
                      sx={(theme) => ({
                        '& .MuiSvgIcon-root': { fontSize: '1.15rem' },
                        color: theme.palette.mode === 'dark' ? 'rgba(255,255,255,0.85)' : 'rgba(0,0,0,0.65)',
                        '&.Mui-checked': { color: theme.palette.primary.main },
                      })}
                    />
                  </TableCell>
                  <TableCell>
                    {row.fName} {row.lName}
                  </TableCell>
                  <TableCell sx={{ overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {row.email}
                  </TableCell>
                  <TableCell>{row.username}</TableCell>
                  <TableCell>{row.phone || '-'}</TableCell>
                  <TableCell>
                    <Chip size="small" color={row.admin ? 'primary' : 'default'} label={row.admin ? 'Admin' : 'User'} />
                  </TableCell>
                  <TableCell>
                    <Chip
                      size="small"
                      color={row.suspended ? 'warning' : 'success'}
                      label={row.suspended ? 'Suspended' : 'Active'}
                      onClick={(e) => {
                        e.stopPropagation();
                        if (onStatusFilter) {
                          onStatusFilter(row.suspended ? 'true' : 'false');
                        }
                      }}
                      sx={{
                        cursor: onStatusFilter ? 'pointer' : 'default',
                        '&:hover': onStatusFilter
                          ? {
                              opacity: 0.8,
                              transform: 'scale(1.05)',
                            }
                          : {},
                      }}
                      role={onStatusFilter ? 'button' : undefined}
                      tabIndex={onStatusFilter ? 0 : undefined}
                      aria-label={onStatusFilter ? `Filter by ${row.suspended ? 'Suspended' : 'Active'} status` : undefined}
                      onKeyDown={
                        onStatusFilter
                          ? (e) => {
                              if (e.key === 'Enter' || e.key === ' ') {
                                e.preventDefault();
                                e.stopPropagation();
                                onStatusFilter(row.suspended ? 'true' : 'false');
                              }
                            }
                          : undefined
                      }
                    />
                  </TableCell>
                  <TableCell align="right">
                    <Stack direction="row" spacing={1} justifyContent="flex-end">
                      {onPromote && onDemote && (
                        <Tooltip title={row.admin ? 'Demote from admin' : 'Promote to admin'}>
                          <IconButton
                            onClick={(e) => {
                              e.stopPropagation();
                              if (row.admin) onDemote(row);
                              else onPromote(row);
                            }}
                            size="small"
                            type="button"
                            aria-label={row.admin ? 'Demote user' : 'Promote user'}
                          >
                            {row.admin ? (
                              <ArrowDownwardIcon fontSize="small" />
                            ) : (
                              <ArrowUpwardIcon fontSize="small" />
                            )}
                          </IconButton>
                        </Tooltip>
                      )}
                      <Tooltip title={row.suspended ? 'Unsuspend' : 'Suspend'}>
                        <IconButton
                          onClick={(e) => {
                            e.stopPropagation();
                            onSuspend(row);
                          }}
                          size="small"
                          type="button"
                          aria-label={row.suspended ? 'Unsuspend user' : 'Suspend user'}
                        >
                          {row.suspended ? (
                            <CheckCircleIcon fontSize="small" color="success" />
                          ) : (
                            <BlockIcon fontSize="small" color="warning" />
                          )}
                        </IconButton>
                      </Tooltip>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
              {!rows.length && !loading && (
                <TableRow>
                  <TableCell colSpan={8} align="center">
                    No users found.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </div>
      <TablePagination
        component="div"
        rowsPerPageOptions={[5, 10, 25]}
        count={pagination.totalItems ?? 0}
        rowsPerPage={pagination.size}
        page={pagination.page}
        onPageChange={(_, newPage) => onPageChange(newPage)}
        onRowsPerPageChange={(event) => onRowsPerPageChange(parseInt(event.target.value, 10))}
        sx={{
          overflowX: 'auto',
          '& .MuiTablePagination-toolbar': {
            flexWrap: 'wrap',
            gap: 1,
          },
        }}
      />
    </CardContent>
  </Card>
);

export default UserTable;

