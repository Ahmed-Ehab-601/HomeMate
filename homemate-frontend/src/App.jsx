import {
  BrowserRouter,
  Route,
  Routes,
  Navigate,
  useLocation,
} from "react-router-dom";
import "./App.css";
import { AuthProvider, useAuth } from "./contexts/AuthContext";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import HomePage from "./pages/HomePage";
import ServicesCatalog from "./pages/ServicesCatalog";
import TaskerDiscoveryPage from "./pages/TaskerDiscoveryPage";
import TaskerProfilePage from "./pages/TaskerProfilePage";
import RequestTaskPage from "./pages/RequestTaskPage";
import UserProfilePage from "./pages/UserProfilePage";
import SignInPage from "./pages/SignInPage";
import TaskerDashboardPage from "./pages/TaskerDashboardPage";
import SignUpPage from "./pages/SignUpPage";
import UserTasksPage from "./pages/UserTasksPage";
import TaskerTasksPage from "./pages/TaskerTasksPage";
import SubmitReviewPage from "./pages/SubmitReviewPage";
import AdminRoutes from "./admin/routing/AdminRoutes";
import SubmitReportPage from "./pages/SubmitReportPage";
import ChatPage from "./pages/ChatPage";
// Protected route component - redirects to signin on 401
function ProtectedRoute({ children, requiredRole }) {
  const { isAuthenticated, isLoading, getUserRole } = useAuth();

  if (isLoading) {
    return <div className="page">Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/signin" replace />;
  }

  if (requiredRole && getUserRole() !== requiredRole) {
    return <Navigate to="/" replace />;
  }

  return children;
}

// User profile route that redirects taskers to tasker hub and admins to admin panel
function UserProfileRoute() {
  const { user } = useAuth();

  if (user?.role === "ROLE_TASKER") {
    return <Navigate to="/tasker/profile" replace />;
  }

  if (user?.role === "ROLE_ADMIN") {
    return <Navigate to="/admin/users" replace />;
  }

  return <UserProfilePage />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/services" element={<ServicesCatalog />} />
      <Route path="/services/:slug/taskers" element={<TaskerDiscoveryPage />} />
      <Route path="/taskers/:taskerId" element={<TaskerProfilePage />} />
      <Route path="/taskers/:taskerId/request" element={<RequestTaskPage />} />
      <Route
        path="/submit-review/:taskId"
        element={
          <ProtectedRoute requiredRole="ROLE_USER">
            <SubmitReviewPage />
            </ProtectedRoute>
            }
            />
      <Route      
        path="/report/submit/:taskId"
        element={
          <ProtectedRoute>
            <SubmitReportPage />
          </ProtectedRoute>
        }
      />
      <Route path="/signin" element={<SignInPage />} />
      <Route path="/signup" element={<SignUpPage />} />
      <Route path="/chat/:chatId" element={<ChatPage />} />

      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <UserProfileRoute />
          </ProtectedRoute>
        }
      />
      <Route
        path="/tasker/profile"
        element={
          <ProtectedRoute requiredRole="ROLE_TASKER">
            <TaskerDashboardPage />
          </ProtectedRoute>
        }
      />
      <Route path="/my-tasks" element={<UserTasksPage />} />
      <Route path="/tasker/my-tasks" element={<TaskerTasksPage />} />
      <Route
        path="/admin/*"
        element={
          <ProtectedRoute requiredRole="ROLE_ADMIN">
            <AdminRoutes />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

function App() {
  const location = useLocation();
  const isAdminRoute = location.pathname.startsWith("/admin");

  return (
    <div className="app-shell">
      {!isAdminRoute && <Header />}
      <AppRoutes />
      <Footer />
    </div>
  );
}

function AppWrapper() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <App />
      </AuthProvider>
    </BrowserRouter>
  );
}

export default AppWrapper;
