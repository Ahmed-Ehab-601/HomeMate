import { BrowserRouter, Route, Routes } from "react-router-dom";
import "./App.css";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import HomePage from "./pages/HomePage";
import ServicesCatalog from "./pages/ServicesCatalog";
import TaskerDiscoveryPage from "./pages/TaskerDiscoveryPage";
import TaskerProfilePage from "./pages/TaskerProfilePage";
import RequestTaskPage from "./pages/RequestTaskPage";
import UserTasksPage from "./pages/UserTasksPage";
import TaskerTasksPage from "./pages/TaskerTasksPage";

function App() {
  return (
    <BrowserRouter>
      <div className="app-shell">
        <Header />
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/services" element={<ServicesCatalog />} />
          <Route
            path="/services/:slug/taskers"
            element={<TaskerDiscoveryPage />}
          />
          <Route path="/taskers/:taskerId" element={<TaskerProfilePage />} />
          <Route
            path="/taskers/:taskerId/request"
            element={<RequestTaskPage />}
          />
          <Route path="/my-tasks" element={<UserTasksPage />} />
          <Route path="/tasker/my-tasks" element={<TaskerTasksPage />} />
        </Routes>
        <Footer />
      </div>
    </BrowserRouter>
  );
}

export default App;
