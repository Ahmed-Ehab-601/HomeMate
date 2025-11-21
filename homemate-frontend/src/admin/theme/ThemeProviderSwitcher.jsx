import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { ThemeProvider } from '@mui/material/styles';
import { lightTheme, darkTheme } from '../theme';

const ThemeModeContext = createContext({
  mode: 'light',
  toggleMode: () => {},
});

export const useAdminThemeMode = () => useContext(ThemeModeContext);

const ThemeProviderSwitcher = ({ children }) => {
  const [mode, setMode] = useState(() => localStorage.getItem('adminThemeMode') || 'light');

  useEffect(() => {
    localStorage.setItem('adminThemeMode', mode);
  }, [mode]);

  const toggleMode = () => {
    setMode((prev) => (prev === 'light' ? 'dark' : 'light'));
  };

  const value = useMemo(() => ({ mode, toggleMode }), [mode]);
  const theme = useMemo(() => (mode === 'dark' ? darkTheme : lightTheme), [mode]);

  return (
    <ThemeModeContext.Provider value={value}>
      <ThemeProvider theme={theme}>{children}</ThemeProvider>
    </ThemeModeContext.Provider>
  );
};

export default ThemeProviderSwitcher;

