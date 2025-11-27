import './ServiceList.css';

const ServiceReview = ({ formData, onConfirm, onBack, isEdit = false }) => {
  const getImageSrc = () => {
    if (formData.imageData) {
      if (typeof formData.imageData === 'string') {
        if (formData.imageData.startsWith('data:')) {
          return formData.imageData;
        }
        return `data:${formData.imageType || 'image/jpeg'};base64,${formData.imageData}`;
      }
    }
    return null;
  };

  const imageSrc = getImageSrc();

  return (
    <div className="service-review-container">
      <div className="service-review-wrapper">
        <div className="review-header-section">
          <h2>Review {isEdit ? 'Service Update' : 'New Service'}</h2>
          <p className="review-subtitle">Please review all details before submitting</p>
        </div>

        <div className="review-content">
          <div className="review-section">
            <h3>Service Information</h3>
            <div className="review-item">
              <strong>Service Name:</strong>
              <span>{formData.name || 'Not provided'}</span>
            </div>
            <div className="review-item">
              <strong>Description:</strong>
              <span className="review-description">{formData.description || 'Not provided'}</span>
            </div>
          </div>

          <div className="review-section">
            <h3>Image</h3>
            {imageSrc ? (
              <div className="review-image-container">
                <img src={imageSrc} alt="Service preview" className="review-image" />
                {formData.imageName && (
                  <p className="review-image-name">{formData.imageName}</p>
                )}
              </div>
            ) : (
              <p className="review-no-image">No image provided</p>
            )}
          </div>

          <div className="review-section">
            <h3>Technical Details</h3>
            <div className="review-item">
              <strong>Image Type:</strong>
              <span>{formData.imageType || 'N/A'}</span>
            </div>
            {formData.imageName && (
              <div className="review-item">
                <strong>Image File Name:</strong>
                <span>{formData.imageName}</span>
              </div>
            )}
            <div className="review-item">
              <strong>Image Data Size:</strong>
              <span>{formData.imageData ? `${Math.round(formData.imageData.length / 1024)} KB` : 'N/A'}</span>
            </div>
          </div>
        </div>

        <div className="review-actions">
          <button type="button" className="btn btn-secondary" onClick={onBack}>
            ← Back to Edit
          </button>
          <button type="button" className="btn btn-primary" onClick={onConfirm}>
            {isEdit ? 'Confirm Update' : 'Confirm Create'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ServiceReview;

