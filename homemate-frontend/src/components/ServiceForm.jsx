import { useState, useEffect } from 'react';
import { uploadToCloudinary } from '../utils/cloudinary';
import { useAuth } from '../contexts/AuthContext';
import './ServiceForm.css';

const ServiceForm = ({ service, onSubmit, onCancel, onReview }) => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    imageData: '',
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
      });
      if (service.imageData) {
        setPreview(service.imageData);
      }
    }
  }, [service]);

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

    const maxSize = 16 * 1024 * 1024;
    if (file.size > maxSize) {
      setErrors(prev => ({ 
        ...prev, 
        imageData: 'Image file must not exceed 16MB' 
      }));
      return;
    }

    setImageFile(file);
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreview(reader.result);
      if (errors.imageData) {
        setErrors(prev => ({ ...prev, imageData: '' }));
      }
    };
    reader.readAsDataURL(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    setErrors({});
    setGeneralError('');
    
    if (!validateForm()) {
      return;
    }
    
    setIsSubmitting(true);
    
    try {
      let imageUrl = formData.imageData;
      
      if (imageFile) {
        imageUrl = await uploadToCloudinary(imageFile, user?.username || user?.id || 'admin');
      }
      
      const submitData = {
        name: formData.name,
        description: formData.description,
        imageData: imageUrl,
      };
      
      if (onReview) {
        onReview(submitData);
      } else {
        await onSubmit(submitData);
      }
    } catch (err) {
      console.error('Form submission error:', err);
      const errorMessage = err.message || err.toString();
      setGeneralError(errorMessage);
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
          {imageFile && !errors.imageData && (
            <span className="file-info">Selected: {imageFile.name}</span>
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