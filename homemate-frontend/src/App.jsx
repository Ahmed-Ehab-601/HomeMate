import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
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
import SignUpPage from "./pages/SignUpPage";
import UserTasksPage from "./pages/UserTasksPage";
import TaskerTasksPage from "./pages/TaskerTasksPage";

// Protected route component - redirects to signin on 401
function ProtectedRoute({ children }) {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return <div className="page">Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/signin" replace />;
  }

  return children;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/services" element={<ServicesCatalog />} />
      <Route path="/services/:slug/taskers" element={<TaskerDiscoveryPage />} />
      <Route path="/taskers/:taskerId" element={<TaskerProfilePage />} />
      <Route path="/taskers/:taskerId/request" element={<RequestTaskPage />} />
      <Route path="/signin" element={<SignInPage />} />
      <Route path="/signup" element={<SignUpPage />} />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <UserProfilePage />
          </ProtectedRoute>
        }
      />
      <Route path="/my-tasks" element={<UserTasksPage />} />
      <Route path="/tasker/my-tasks" element={<TaskerTasksPage />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="app-shell">
          <Header />
          <AppRoutes />
          <Footer />
        </div>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
