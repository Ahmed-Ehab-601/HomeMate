import { useState, useEffect } from 'react';
import './ServiceForm.css';

const ServiceForm = ({ service, onSubmit, onCancel, onReview }) => {
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    imageData: '',
    imageName: '',
    imageType: '',
  });
  const [imageFile, setImageFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [errors, setErrors] = useState({});
  const [generalError, setGeneralError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (service) {
      setFormData({
        name: service.name || '',
        description: service.description || '',
        imageData: service.imageData || '',
        imageName: service.imageName || '',
        imageType: service.imageType || '',
      });
      if (service.imageData) {
        setPreview(
          typeof service.imageData === 'string' && service.imageData.startsWith('data:')
            ? service.imageData
            : `data:${service.imageType || 'image/jpeg'};base64,${service.imageData}`
        );
      }
    }
  }, [service]);

  // Client-side validation
  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.name || formData.name.trim() === '') {
      newErrors.name = 'Service name is required';
    } else if (formData.name.length > 100) {
      newErrors.name = 'Name must not exceed 100 characters';
    }
    
    if (!formData.description || formData.description.trim() === '') {
      newErrors.description = 'Description is required';
    } else if (formData.description.length > 100) {
      newErrors.description = 'Description must not exceed 100 characters';
    }
    
    if (formData.imageData && formData.imageData.length > 22369621) {
      newErrors.imageData = 'Image data must not exceed 16MB';
    }
    
    if (formData.imageName && formData.imageName.length > 200) {
      newErrors.imageName = 'Image name must not exceed 200 characters';
    }
    
    if (formData.imageType && formData.imageType.length > 200) {
      newErrors.imageType = 'Image type must not exceed 200 characters';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
    if (generalError) {
      setGeneralError('');
    }
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate file size (16MB = 16 * 1024 * 1024 bytes)
    const maxSize = 16 * 1024 * 1024;
    if (file.size > maxSize) {
      setErrors(prev => ({ 
        ...prev, 
        imageData: 'Image file must not exceed 16MB' 
      }));
      return;
    }

    setImageFile(file);
    setFormData((prev) => ({
      ...prev,
      imageName: file.name,
      imageType: file.type,
    }));

    const reader = new FileReader();
    reader.onloadend = () => {
      const base64String = reader.result.split(',')[1];
      setFormData((prev) => ({ ...prev, imageData: base64String }));
      setPreview(reader.result);
      
      // Clear any previous image errors
      if (errors.imageData) {
        setErrors(prev => ({ ...prev, imageData: '' }));
      }
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Clear previous errors
    setErrors({});
    setGeneralError('');
    
    // Client-side validation
    if (!validateForm()) {
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      // If onReview is provided, show review step first
      if (onReview) {
        onReview(formData);
      } else {
        // Otherwise submit directly
        await onSubmit(formData);
      }
    } catch (err) {
      console.error('Form submission error:', err);
      
      // Parse backend validation errors
      const errorMessage = err.message || err.toString();
      
      if (errorMessage.includes(':')) {
        // Parse field-specific errors (format: "fieldName: error message")
        const fieldErrors = {};
        errorMessage.split('\n').forEach(line => {
          const [field, message] = line.split(':').map(s => s.trim());
          if (field && message) {
            fieldErrors[field] = message;
          }
        });
        
        if (Object.keys(fieldErrors).length > 0) {
          setErrors(fieldErrors);
        } else {
          setGeneralError(errorMessage);
        }
      } else {
        // General error
        setGeneralError(errorMessage);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="service-form-container">
      {generalError && (
        <div className="error-banner">
          {generalError}
        </div>
      )}

      <form onSubmit={handleSubmit} className="service-form">
        <div className="form-group">
          <label htmlFor="name">
            Service Name * 
            <span className="char-count">{formData.name.length}/100</span>
          </label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            className={errors.name ? 'error' : ''}
            maxLength={100}
            required
          />
          {errors.name && <span className="error-message">{errors.name}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="description">
            Description * 
            <span className="char-count">{formData.description.length}/100</span>
          </label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            className={errors.description ? 'error' : ''}
            rows="4"
            maxLength={100}
            required
          />
          {errors.description && <span className="error-message">{errors.description}</span>}
        </div>

        <div className="form-group">
          <label htmlFor="image">Service Image (Max 16MB)</label>
          <input
            type="file"
            id="image"
            accept="image/*"
            onChange={handleImageChange}
            className={errors.imageData ? 'error' : ''}
          />
          {errors.imageData && <span className="error-message">{errors.imageData}</span>}
          {formData.imageName && !errors.imageData && (
            <span className="file-info">Selected: {formData.imageName}</span>
          )}
          {preview && (
            <div className="image-preview">
              <img src={preview} alt="Preview" />
            </div>
          )}
        </div>

        <div className="form-actions">
          <button 
            type="submit" 
            className="btn btn-primary"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Saving...' : (service ? 'Update Service' : 'Create Service')}
          </button>
          <button 
            type="button" 
            className="btn btn-secondary" 
            onClick={onCancel}
            disabled={isSubmitting}
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default ServiceForm;