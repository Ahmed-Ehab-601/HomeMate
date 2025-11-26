import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import AdminRoutes from './admin/routing/AdminRoutes';
import './index.css';

function App() {
  console.log('App rendering');
  
  return (
    <BrowserRouter>
      <Routes>
        {/* Redirect root to admin */}
        <Route path="/" element={<Navigate to="/admin" replace />} />
        
        {/* Admin routes */}
        <Route path="/admin/*" element={<AdminRoutes />} />
        
        {/* Catch all */}
        <Route path="*" element={<Navigate to="/admin" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('Failed to find root element. Make sure index.html has <div id="root"></div>');
}

console.log('Rendering React app...');

const root = ReactDOM.createRoot(rootElement);
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

console.log('React app rendered successfully');