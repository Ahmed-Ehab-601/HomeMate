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

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    if (file) {
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
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // If onReview is provided, show review step first
    if (onReview) {
      onReview(formData);
    } else {
      // Otherwise submit directly
      onSubmit(formData);
    }
  };

  return (
    <div className="service-form-container">
      <form onSubmit={handleSubmit} className="service-form">
        <div className="form-group">
          <label htmlFor="name">Service Name *</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="description">Description *</label>
          <textarea
            id="description"
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            rows="4"
            required
          />
        </div>

        <div className="form-group">
          <label htmlFor="image">Service Image</label>
          <input
            type="file"
            id="image"
            accept="image/*"
            onChange={handleImageChange}
          />
          {preview && (
            <div className="image-preview">
              <img src={preview} alt="Preview" />
            </div>
          )}
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            {service ? 'Update Service' : 'Create Service'}
          </button>
          <button type="button" className="btn btn-secondary" onClick={onCancel}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
};

export default ServiceForm;

