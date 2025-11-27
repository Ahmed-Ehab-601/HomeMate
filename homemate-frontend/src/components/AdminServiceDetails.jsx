import { useState, useEffect } from 'react';
import { serviceAPI } from '../services/api';
import ServiceForm from './ServiceForm';
import ServiceReview from './ServiceReview';
import './ServiceList.css';

const AdminServiceDetails = ({ serviceID, onBack, onServiceUpdated }) => {
  const [serviceDetails, setServiceDetails] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isReviewing, setIsReviewing] = useState(false);
  const [reviewData, setReviewData] = useState(null);
  const [message, setMessage] = useState(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  useEffect(() => {
    if (serviceID) {
      loadServiceDetails();
    }
  }, [serviceID]);

  const loadServiceDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await serviceAPI.getServiceDetails(serviceID);
      console.log('📥 Service Details Response:', data);
      console.log('📦 Service Object:', data?.service);
      if (data?.service) {
        console.log('🖼️ Image Data Debug:', {
          hasImageData: !!data.service.imageData,
          imageDataType: typeof data.service.imageData,
          imageDataIsNull: data.service.imageData === null,
          imageDataIsUndefined: data.service.imageData === undefined,
          imageDataLength: data.service.imageData?.length || 0,
          imageType: data.service.imageType || 'NOT SET',
          imageName: data.service.imageName || 'NOT SET',
          imageDataPreview: data.service.imageData 
            ? (typeof data.service.imageData === 'string' 
                ? data.service.imageData.substring(0, 100) + '...' 
                : 'NOT A STRING')
            : 'NULL/UNDEFINED',
          allServiceKeys: Object.keys(data.service)
        });
      }
      setServiceDetails(data);
    } catch (err) {
      console.error('❌ Error loading service details:', err);
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = () => {
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
    setMessage(null);
  };

  const handleReview = (formData) => {
    setReviewData(formData);
    setIsEditing(false);
    setIsReviewing(true);
  };

  const handleReviewConfirm = async () => {
    try {
      setMessage(null);
      await serviceAPI.editService(serviceID, reviewData);
      setMessage({ type: 'success', text: 'Service updated successfully!' });
      setIsReviewing(false);
      setReviewData(null);
      await loadServiceDetails();
      if (onServiceUpdated) {
        onServiceUpdated();
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
      setIsReviewing(false);
      setIsEditing(true);
    }
  };

  const handleReviewBack = () => {
    setIsReviewing(false);
    setIsEditing(true);
  };

  const handleSubmit = async (formData) => {
    // This shouldn't be called if onReview is provided, but keep for fallback
    try {
      setMessage(null);
      await serviceAPI.editService(serviceID, formData);
      setMessage({ type: 'success', text: 'Service updated successfully!' });
      setIsEditing(false);
      await loadServiceDetails();
      if (onServiceUpdated) {
        onServiceUpdated();
      }
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
    }
  };

  const handleDelete = async () => {
    try {
      setMessage(null);
      await serviceAPI.deleteService(serviceID);
      setMessage({ type: 'success', text: 'Service deleted successfully!' });
      setTimeout(() => {
        onBack();
      }, 1500);
    } catch (err) {
      setMessage({ type: 'error', text: err.message });
      setShowDeleteConfirm(false);
    }
  };

  const getImageSrc = () => {
    const service = serviceDetails?.service;
    
    if (!service) {
      console.warn('⚠️ No service object found');
      return 'https://via.placeholder.com/400x300?text=No+Service';
    }
    
    const imageData = service.imageData;
    const imageType = service.imageType || 'image/jpeg';
    
    if (!imageData) {
      console.warn(`⚠️ No imageData for service: "${service.name}"`, {
        hasImageName: !!service.imageName,
        imageName: service.imageName,
        allKeys: Object.keys(service)
      });
      return 'https://via.placeholder.com/400x300?text=No+Image+Data';
    }
    
    if (typeof imageData !== 'string') {
      console.warn(`⚠️ imageData is not a string for service: "${service.name}"`, {
        type: typeof imageData,
        value: imageData
      });
      return 'https://via.placeholder.com/400x300?text=Invalid+Image+Data';
    }
    
    // If it's already a data URL
    if (imageData.startsWith('data:')) {
      console.log(`✅ Using existing data URL for: "${service.name}"`);
      return imageData;
    }
    
    // If it's a base64 string, add the data URL prefix
    if (imageData.length > 0) {
      const dataUrl = `data:${imageType};base64,${imageData}`;
      console.log(`✅ Created data URL for: "${service.name}"`, {
        imageType,
        dataLength: imageData.length,
        preview: imageData.substring(0, 50) + '...'
      });
      return dataUrl;
    }
    
    console.warn(`⚠️ Empty imageData string for service: "${service.name}"`);
    return 'https://via.placeholder.com/400x300?text=Empty+Image';
  };

  if (loading) {
    return (
      <div className="service-list-container">
        <div className="service-details-wrapper">
          <div className="loading">Loading service details...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="service-list-container">
        <div className="service-details-wrapper">
          <div className="error">Error: {error}</div>
          <button className="btn btn-secondary" onClick={onBack} style={{ marginTop: '1rem' }}>
            ← Back to Services
          </button>
        </div>
      </div>
    );
  }

  if (isReviewing && reviewData) {
    return (
      <div className="service-list-container">
        <div className="service-form-wrapper">
          <div className="form-header">
            <button className="btn btn-back" onClick={handleReviewBack}>
              ← Back to Edit
            </button>
            <h2>Review Service Update</h2>
          </div>
          {message && (
            <div className={`message ${message.type}`}>{message.text}</div>
          )}
          <ServiceReview
            formData={reviewData}
            onConfirm={handleReviewConfirm}
            onBack={handleReviewBack}
            isEdit={true}
          />
        </div>
      </div>
    );
  }

  if (isEditing) {
    return (
      <div className="service-list-container">
        <div className="service-form-wrapper">
          <div className="form-header">
            <button className="btn btn-back" onClick={handleCancel}>
              ← Back
            </button>
            <h2>Edit Service</h2>
          </div>
          {message && (
            <div className={`message ${message.type}`}>{message.text}</div>
          )}
          <ServiceForm
            service={serviceDetails?.service}
            onSubmit={handleSubmit}
            onCancel={handleCancel}
            onReview={handleReview}
          />
        </div>
      </div>
    );
  }

  const service = serviceDetails?.service;

  return (
    <div className="service-list-container">
      <div className="service-details-wrapper">
        <div className="details-header">
          <button className="btn btn-back" onClick={onBack}>
            ← Back to Services
          </button>
          <h1>Service Details</h1>
        </div>

        {message && (
          <div className={`message ${message.type}`}>{message.text}</div>
        )}

        {showDeleteConfirm ? (
          <div className="delete-confirm">
            <h3>Are you sure you want to delete this service?</h3>
            <p>This action cannot be undone.</p>
            <div className="delete-actions">
              <button className="btn btn-danger" onClick={handleDelete}>
                Yes, Delete
              </button>
              <button className="btn btn-secondary" onClick={() => setShowDeleteConfirm(false)}>
                Cancel
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="service-details-content">
              <div className="service-details-image">
                <img 
                  src={getImageSrc()} 
                  alt={service?.name}
                  onError={(e) => {
                    console.error(`🖼️ Image failed to load for: "${service?.name}"`, {
                      attemptedSrc: e.target.src.substring(0, 100) + '...',
                      serviceId: serviceID,
                      hasImageData: !!service?.imageData,
                      imageType: service?.imageType,
                      imageName: service?.imageName
                    });
                    if (!e.target.src.includes('placeholder') && !e.target.src.includes('Image+Error')) {
                      e.target.src = 'https://via.placeholder.com/400x300?text=Image+Error';
                    }
                  }}
                  onLoad={() => {
                    console.log(`✅ Image loaded successfully for: "${service?.name}"`);
                  }}
                />
              </div>

              <div className="service-details-info">
                <h2>{service?.name}</h2>
                <p className="service-description">{service?.description}</p>

                {serviceDetails && (
                  <div className="service-stats">
                    {serviceDetails.completedTasks !== undefined && (
                      <div className="stat-card">
                        <h3>{serviceDetails.completedTasks || 0}</h3>
                        <p>Completed Tasks</p>
                      </div>
                    )}
                    {serviceDetails.taskers !== undefined && (
                      <div className="stat-card">
                        <h3>{serviceDetails.taskers || 0}</h3>
                        <p>Taskers</p>
                      </div>
                    )}
                  </div>
                )}

                <div className="service-meta">
                  {service?.imageName && (
                    <p><strong>Image:</strong> {service.imageName}</p>
                  )}
                </div>

                <div className="details-actions">
                  <button className="btn btn-primary" onClick={handleEdit}>
                    Edit Service
                  </button>
                  <button className="btn btn-danger" onClick={() => setShowDeleteConfirm(true)}>
                    Delete Service
                  </button>
                </div>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default AdminServiceDetails;

