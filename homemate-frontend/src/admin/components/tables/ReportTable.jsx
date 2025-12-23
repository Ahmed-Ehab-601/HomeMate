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
  Chip,
  LinearProgress,
  IconButton,
  Tooltip,
} from '@mui/material';
import { Email } from '@mui/icons-material';

const ReportTable = ({
  rows = [],
  loading,
  pagination,
  onPageChange,
  onRowsPerPageChange,
  onViewDetails,
  onRespond,
}) => {
  const getStatusStyle = (status) => {
    switch (status?.toLowerCase()) {
      case 'done':
        return {
          backgroundColor: '#4caf50',
          color: '#fff',
        };
      case 'pending':
        return {
          backgroundColor: '#f44336',
          color: '#fff',
        };
      default:
        return {
          backgroundColor: '#9e9e9e',
          color: '#fff',
        };
    }
  };

  const getReporterLabel = (reporter) => {
    return reporter ? 'User' : 'Tasker';
  };

  const getReporterStyle = (reporter) => {
    if (reporter) {
      return {
        backgroundColor: '#2196f3',
        color: '#fff',
      };
    } else {
      return {
        backgroundColor: '#ff9800',
        color: '#fff',
      };
    }
  };

  return (
    <Card
      elevation={0}
      sx={{
        borderRadius: { xs: 0, sm: 1.5 },
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
                minWidth: 800,
                tableLayout: 'auto',
                '& .MuiTableCell-root': {
                  textAlign: 'center',
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
                  <TableCell sx={{ width: '10%' }}>Report ID</TableCell>
                  <TableCell sx={{ width: '30%' }}>Header</TableCell>
                  <TableCell sx={{ width: '10%' }}>Task ID</TableCell>
                  <TableCell sx={{ width: '15%' }}>Reporter</TableCell>
                  <TableCell sx={{ width: '15%' }}>Status</TableCell>
                  <TableCell sx={{ width: '20%' }}>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {rows.length === 0 && !loading ? (
                  <TableRow>
                    <TableCell colSpan={6} align="center" sx={{ py: 4 }}>
                      No reports found
                    </TableCell>
                  </TableRow>
                ) : (
                  rows.map((row) => (
                    <TableRow
                      key={row.reportID}
                      hover
                      sx={{
                        '&:last-child td, &:last-child th': { border: 0 },
                        cursor: 'pointer'
                      }}
                      onClick={() => onViewDetails && onViewDetails(row.reportID)}
                    >
                      <TableCell>{row.reportID}</TableCell>
                      <TableCell
                        sx={{
                          maxWidth: '100%',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                        }}
                      >
                        {row.header}
                      </TableCell>
                      <TableCell>{row.taskID}</TableCell>
                      <TableCell>
                        <Chip
                          label={getReporterLabel(row.reporter)}
                          size="small"
                          sx={getReporterStyle(row.reporter)}
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={row.adminStatus || 'Pending'}
                          size="small"
                          sx={getStatusStyle(row.adminStatus)}
                        />
                      </TableCell>
                      <TableCell onClick={(e) => e.stopPropagation()}>
                        {row.adminStatus?.toLowerCase() === 'done' && (
                          <Tooltip title="Respond via Email">
                            <IconButton
                              size="small"
                              onClick={() => onRespond && onRespond(row.reportID)}
                              color="primary"
                            >
                              <Email />
                            </IconButton>
                          </Tooltip>
                        )}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </div>
        <TablePagination
          component="div"
          count={pagination.totalItems || 0}
          page={pagination.page || 0}
          onPageChange={(_, newPage) => onPageChange && onPageChange(newPage)}
          rowsPerPage={pagination.size || 20}
          onRowsPerPageChange={(e) =>
            onRowsPerPageChange && onRowsPerPageChange(parseInt(e.target.value, 10))
          }
          rowsPerPageOptions={[10, 20, 50, 100]}
          sx={{
            borderTop: '1px solid',
            borderColor: 'divider',
            '& .MuiTablePagination-toolbar': {
              justifyContent: 'center',
            },
          }}
        />
      </CardContent>
    </Card>
  );
};

export default ReportTable;
