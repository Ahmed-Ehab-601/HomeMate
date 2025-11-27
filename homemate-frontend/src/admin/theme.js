import { createTheme } from '@mui/material/styles';

const baseOptions = {
  typography: {
    fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
    h4: {
      fontWeight: 700,
      letterSpacing: -0.5,
    },
    h6: {
      fontWeight: 600,
    },
    button: {
      textTransform: 'none',
      fontWeight: 600,
    },
  },
  shape: {
    borderRadius: 12,
  },
  components: {
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 16,
          border: '1px solid rgba(215,240,74,0.08)',
          boxShadow: '0 10px 30px rgba(15, 15, 15, 0.04)',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 999,
          paddingInline: 20,
        },
      },
    },
    MuiTableContainer: {
      styleOverrides: {
        root: {
          width: '100%',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            fontWeight: 600,
            textTransform: 'uppercase',
            fontSize: 12,
            letterSpacing: 0.5,
          },
        },
      },
    },
  },
};

export const lightTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#d7f04a',
      light: '#eaf78a',
      dark: '#9fbf3a',
    },
    secondary: {
      main: '#98c86a',
      light: '#bfe867',
      dark: '#6f973d',
    },
    background: {
      default: '#f5f7fb',
      paper: '#ffffff',
    },
  },
  ...baseOptions,
});

export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#5ef0d0',
    },
    secondary: {
      main: '#5ef0d0',
    },
    background: {
      default: '#0f172a',
      paper: '#1e293b',
    },
    text: {
      primary: 'rgba(255,255,255,0.92)',
      secondary: 'rgba(255,255,255,0.75)',
    },
  },
  components: {
    ...baseOptions.components,
    MuiCard: {
      styleOverrides: {
        root: {
          border: '1px solid rgba(255,255,255,0.08)',
          boxShadow: '0 10px 30px rgba(0,0,0,0.6)',
        },
      },
    },
    MuiTableCell: {
      styleOverrides: {
        root: {
          color: 'rgba(255,255,255,0.92)',
          fontWeight: 500,
          WebkitFontSmoothing: 'antialiased',
          MozOsxFontSmoothing: 'grayscale',
        },
      },
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          '& .MuiTableCell-root': {
            color: '#ffffff',
            fontWeight: 600,
            WebkitFontSmoothing: 'antialiased',
            MozOsxFontSmoothing: 'grayscale',
          },
        },
      },
    },
  },
  typography: {
    ...baseOptions.typography,
    body1: {
      ...baseOptions.typography.body1,
      WebkitFontSmoothing: 'antialiased',
      MozOsxFontSmoothing: 'grayscale',
    },
    body2: {
      ...baseOptions.typography.body2,
      WebkitFontSmoothing: 'antialiased',
      MozOsxFontSmoothing: 'grayscale',
    },
  },
  shape: baseOptions.shape,
});

