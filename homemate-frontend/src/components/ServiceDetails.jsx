import './ServiceDetails.css';

const ServiceDetails = ({ serviceDetails, onBack }) => {
  const service = serviceDetails.service;

  const getImageSrc = () => {
    if (!service?.imageData) {
      return 'https://via.placeholder.com/300x200?text=No+Image';
    }

    // Image data is coming as a string from backend
    // It should be base64 encoded already, just add the data URL prefix
    const imageData = service.imageData;
    const imageType = service.imageType || 'image/jpeg';
    
    // If it already has data: prefix, return as-is
    if (imageData.startsWith('data:')) {
      return imageData;
    }
    
    // Otherwise, construct the data URL with base64 string
    return `data:${imageType};base64,${imageData}`;
  };

  return (
    <div className="service-details-container">
      <button onClick={onBack} className="back-button">
        ← Back to Services
      </button>
      
      <div className="service-details-card">
        <h2>Service Details</h2>
        
        <div className="service-image-container">
          <img 
            src={getImageSrc()} 
            alt={service?.name || 'Service'} 
            className="service-image"
          />
        </div>
        
        <h3>{service?.name}</h3>
        <p className="service-description">{service?.description}</p>
        
        <div className="service-stats">
          <div className="stat-item">
            <span className="stat-value">{serviceDetails.completedTasks || 0}</span>
            <span className="stat-label">Completed Tasks</span>
          </div>
          <div className="stat-item">
            <span className="stat-value">{serviceDetails.taskers || 0}</span>
            <span className="stat-label">Taskers</span>
          </div>
        </div>
        
        <div className="service-meta">
          {service?.imageName && (
            <p><strong>Image:</strong> {service.imageName}</p>
          )}
          {service?.imageType && (
            <p><strong>Image Type:</strong> {service.imageType}</p>
          )}
        </div>
        
        <div className="service-actions">
          <button className="edit-button">Edit Service</button>
          <button className="delete-button">Delete Service</button>
        </div>
      </div>
    </div>
  );
};

export default ServiceDetails;