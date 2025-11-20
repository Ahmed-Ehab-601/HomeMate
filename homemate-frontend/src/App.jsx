
import { Navigate } from 'react-router-dom';
import { useState } from 'react';
import AdminServiceList from './components/AdminServiceList';
import AdminServiceDetails from './components/AdminServiceDetails';
import ServiceForm from './components/ServiceForm';
import ServiceReview from './components/ServiceReview';
import { serviceAPI } from './services/api';
import './App.css';

function App() {
  return <Navigate to="/admin/users" replace />;
}


function App() {
  const [currentView, setCurrentView] = useState('list'); // 'list', 'details', 'create', 'edit', 'review'
  const [selectedServiceID, setSelectedServiceID] = useState(null);
  const [message, setMessage] = useState(null);
  const [reviewData, setReviewData] = useState(null);
  const [isReviewEdit, setIsReviewEdit] = useState(false);

  const handleServiceClick = (serviceID) => {
    setSelectedServiceID(serviceID);
    setCurrentView('details');
  };

  const handleCreateNew = () => {
    setSelectedServiceID(null);
    setCurrentView('create');
  };

  const handleBackToList = () => {
    setCurrentView('list');
    setSelectedServiceID(null);
    setMessage(null);
  };

  const handleServiceUpdated = () => {
    // Refresh the list if needed
    setMessage({ type: 'success', text: 'Service updated successfully!' });
  };

  const handleCreateReview = (formData) => {
    setReviewData(formData);
    setIsReviewEdit(false);
    setCurrentView('review');
  };

  const handleCreateSubmit = async () => {
    try {
      setMessage(null);
      await serviceAPI.createService(reviewData);
      setMessage({ type: 'success', text: 'Service created successfully!' });
      setTimeout(() => {
        handleBackToList();
      }, 1500);
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
      setCurrentView('create'); // Go back to form on error
    }
  };

  const handleCreateCancel = () => {
    handleBackToList();
  };

  const handleReviewBack = () => {
    if (isReviewEdit) {
      setCurrentView('details');
    } else {
      setCurrentView('create');
    }
  };

  return (
    <div className="app-container">
      <div className="app-content">
        {currentView === 'list' && (
          <AdminServiceList
            onServiceClick={handleServiceClick}
            onCreateNew={handleCreateNew}
          />
        )}

        {currentView === 'details' && selectedServiceID && (
          <AdminServiceDetails
            serviceID={selectedServiceID}
            onBack={handleBackToList}
            onServiceUpdated={handleServiceUpdated}
          />
        )}

        {currentView === 'create' && (
          <div className="service-list-container">
            <div className="service-form-wrapper">
              <div className="form-header">
                <button className="btn btn-back" onClick={handleCreateCancel}>
                  ← Back to Services
                </button>
                <h2>Create New Service</h2>
              </div>
              {message && (
                <div className={`message ${message.type}`}>{message.text}</div>
              )}
              <ServiceForm
                service={null}
                onSubmit={handleCreateSubmit}
                onCancel={handleCreateCancel}
                onReview={handleCreateReview}
              />
            </div>
          </div>
        )}

        {currentView === 'review' && reviewData && !isReviewEdit && (
          <div className="service-list-container">
            <div className="service-form-wrapper">
              {message && (
                <div className={`message ${message.type}`}>{message.text}</div>
              )}
              <ServiceReview
                formData={reviewData}
                onConfirm={handleCreateSubmit}
                onBack={handleReviewBack}
                isEdit={false}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
