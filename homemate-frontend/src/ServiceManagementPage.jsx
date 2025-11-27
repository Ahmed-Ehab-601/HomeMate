import { useState, useEffect } from 'react';
import { Box, Alert, Snackbar } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import AdminServiceList from './components/AdminServiceList';
import AdminServiceDetails from './components/AdminServiceDetails';
import ServiceForm from './components/ServiceForm';
import ServiceReview from './components/ServiceReview';
import { serviceAPI } from './services/api';
import './components/ServiceList.css';

function ServiceManagementPage() {
  const [currentView, setCurrentView] = useState('list');
  const [selectedServiceID, setSelectedServiceID] = useState(null);
  const [message, setMessage] = useState(null);
  const [reviewData, setReviewData] = useState(null);
  const [isReviewEdit, setIsReviewEdit] = useState(false);
  
  const theme = useTheme();
  
  // Sync MUI theme with CSS
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', theme.palette.mode);
  }, [theme.palette.mode]);

  const handleServiceClick = (serviceID) => {
    setSelectedServiceID(serviceID);
    setCurrentView('details');
    setMessage(null);
  };

  const handleCreateNew = () => {
    setSelectedServiceID(null);
    setReviewData(null);
    setMessage(null);
    setCurrentView('create');
  };

  const handleBackToList = () => {
    setCurrentView('list');
    setSelectedServiceID(null);
    setReviewData(null);
    setMessage(null);
  };

  const handleServiceUpdated = () => {
    setMessage({ type: 'success', text: 'Service updated successfully!' });
  };

  const handleCreateReview = (formData) => {
    setReviewData(formData);
    setIsReviewEdit(false);
    setMessage(null);
    setCurrentView('review');
  };

  const handleCreateSubmit = async () => {
    if (!reviewData) return;
    
    try {
      setMessage(null);
      await serviceAPI.createService(reviewData);
      setMessage({ type: 'success', text: 'Service created successfully!' });
      setTimeout(() => {
        handleBackToList();
      }, 1500);
    } catch (err) {
      console.error('Create error:', err);
      setMessage({ type: 'error', text: err.message });
      setCurrentView('create');
    }
  };

  const handleCreateCancel = () => {
    setReviewData(null);
    setMessage(null);
    handleBackToList();
  };

  const handleReviewBack = () => {
    setMessage(null);
    if (isReviewEdit) {
      setCurrentView('details');
    } else {
      setCurrentView('create');
    }
  };

  return (
    <>
      {/* LIST VIEW */}
      {currentView === 'list' && (
        <AdminServiceList
          onServiceClick={handleServiceClick}
          onCreateNew={handleCreateNew}
        />
      )}

      {/* DETAILS VIEW */}
      {currentView === 'details' && selectedServiceID && (
        <AdminServiceDetails
          serviceID={selectedServiceID}
          onBack={handleBackToList}
          onServiceUpdated={handleServiceUpdated}
        />
      )}

      {/* CREATE VIEW */}
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

      {/* REVIEW VIEW */}
      {currentView === 'review' && reviewData && !isReviewEdit && (
        <div className="service-list-container">
          <div className="service-form-wrapper">
            <div className="form-header">
              <button className="btn btn-back" onClick={handleReviewBack}>
                ← Back to Edit
              </button>
              <h2>Review New Service</h2>
            </div>
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

      {/* MUI Snackbar for notifications */}
      <Snackbar
        open={Boolean(message)}
        autoHideDuration={6000}
        onClose={() => setMessage(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert 
          severity={message?.type === 'error' ? 'error' : 'success'} 
          onClose={() => setMessage(null)}
          sx={{ width: '100%' }}
        >
          {message?.text}
        </Alert>
      </Snackbar>
    </>
  );
}

export default ServiceManagementPage;