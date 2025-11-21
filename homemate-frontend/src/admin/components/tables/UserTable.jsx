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
  Chip,
  Tooltip,
  LinearProgress,
  Stack,
} from '@mui/material';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

const UserTable = ({
  rows = [],
  loading,
  pagination,
  onPageChange,
  onRowsPerPageChange,
  onSuspend,
  onStatusFilter,
}) => (
  <Card
    elevation={0}
    sx={{
      borderRadius: { xs: 0, sm: 4 },
      border: (theme) =>
        theme.palette.mode === 'dark'
          ? '1px solid rgba(255,255,255,0.08)'
          : '1px solid rgba(103,80,164,0.1)',
      boxShadow: (theme) =>
        theme.palette.mode === 'dark'
          ? '0 20px 35px rgba(0,0,0,0.6)'
          : '0 20px 35px rgba(15,15,15,0.05)',
      width: '100vw',
      maxWidth: '100vw',
      margin: 0,
      boxSizing: 'border-box',
      overflow: 'hidden',
      display: 'flex',
      flexDirection: 'column',
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
              backgroundColor: 'rgba(103,80,164,0.3)',
              borderRadius: '4px',
            },
          }}
        >
          <Table
            size="small"
            sx={{
              width: '100%',
              tableLayout: 'auto',
              minWidth: '600px', // Minimum width to ensure readability on mobile
              '& .MuiTableCell-root': {
                color: (theme) =>
                  theme.palette.mode === 'dark'
                    ? 'rgba(255,255,255,0.92)'
                    : 'inherit',
                fontWeight: (theme) => (theme.palette.mode === 'dark' ? 500 : 'inherit'),
                WebkitFontSmoothing: 'antialiased',
                MozOsxFontSmoothing: 'grayscale',
              },
              '& .MuiTableHead-root .MuiTableCell-root': {
                color: (theme) =>
                  theme.palette.mode === 'dark' ? '#ffffff' : 'inherit',
                fontWeight: 600,
              },
            }}
          >
            <TableHead>
              <TableRow>
                <TableCell sx={{ whiteSpace: 'nowrap' }}>Name</TableCell>
                <TableCell sx={{ whiteSpace: 'nowrap' }}>Email</TableCell>
                <TableCell sx={{ whiteSpace: 'nowrap' }}>Username</TableCell>
                <TableCell sx={{ whiteSpace: 'nowrap' }}>Phone</TableCell>
                <TableCell sx={{ whiteSpace: 'nowrap' }}>Admin</TableCell>
                <TableCell sx={{ whiteSpace: 'nowrap' }}>Status</TableCell>
                <TableCell align="right" sx={{ whiteSpace: 'nowrap' }}>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row) => (
                <TableRow
                  key={row.userId}
                  hover
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>
                    {row.fName} {row.lName}
                  </TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap', maxWidth: { xs: '150px', sm: 'none' }, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {row.email}
                  </TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{row.username}</TableCell>
                  <TableCell sx={{ whiteSpace: 'nowrap' }}>{row.phone || '-'}</TableCell>
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
                  <TableCell colSpan={7} align="center">
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

