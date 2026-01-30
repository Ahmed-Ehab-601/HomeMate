import { Routes, Route } from 'react-router-dom';
import { ThemeProvider, CssBaseline } from '@mui/material';
import AdminLayout from '../components/layout/AdminLayout';
import UsersPage from '../pages/UsersPage';
import TaskersPage from '../pages/TaskersPage';
import ServicesPage from '../pages/ServicesPage';
import ReportsPage from '../pages/ReportsPage';
import ReportDetailsPage from '../pages/ReportDetailsPage';
import AnalysisPage from '../pages/AnalysisPage';
import ThemeProviderSwitcher from '../theme/ThemeProviderSwitcher';

const AdminRoutes = () => (
  <ThemeProviderSwitcher>
    <CssBaseline />
    <Routes>
      <Route path="" element={<AdminLayout />}>
        <Route index element={<UsersPage />} />
        <Route path="users" element={<UsersPage />} />
        <Route path="taskers" element={<TaskersPage />} />
        <Route path="services" element={<ServicesPage />} />
        <Route path="reports" element={<ReportsPage />} />
        <Route path="reports/:id" element={<ReportDetailsPage />} />
        <Route path="analysis" element={<AnalysisPage />} />
      </Route>
    </Routes>
  </ThemeProviderSwitcher>
);

export default AdminRoutes;

