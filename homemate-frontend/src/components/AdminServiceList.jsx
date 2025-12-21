import { useState, useEffect } from 'react';
import { serviceAPI } from '../services/api';
import './ServiceList.css';

const AdminServiceList = ({ onServiceClick, onCreateNew }) => {
  const [services, setServices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadServices();
  }, []);

  const loadServices = async () => {
    try {
      setLoading(true);
      setError(null);
      console.log('🔵 Loading services from API...');
      const data = await serviceAPI.getAllServices();
      console.log('✅ Loaded services raw data:', data);
      
      // Handle both array and object responses
      let servicesArray = [];
      if (Array.isArray(data)) {
        servicesArray = data;
      } else if (data && Array.isArray(data.services)) {
        servicesArray = data.services;
      } else if (data && Array.isArray(data.data)) {
        servicesArray = data.data;
      } else {
        servicesArray = [];
      }
      
      // Detailed logging for debugging image issues
      if (servicesArray.length > 0) {
        console.log(`✅ Loaded ${servicesArray.length} services`);
        servicesArray.forEach((service, index) => {
          console.log(`\n📦 Service ${index + 1}: "${service.name}"`, {
            id: service.id || service.serviceID,
            hasImageData: !!service.imageData,
            imageDataType: typeof service.imageData,
            imageDataIsNull: service.imageData === null,
            imageDataIsUndefined: service.imageData === undefined,
            imageDataLength: service.imageData?.length || 0,
            imageType: service.imageType || 'NOT SET',
            imageName: service.imageName || 'NOT SET',
            allKeys: Object.keys(service),
            imageDataPreview: service.imageData 
              ? (typeof service.imageData === 'string' 
                  ? service.imageData.substring(0, 100) + '...' 
                  : 'NOT A STRING')
              : 'NULL/UNDEFINED'
          });
        });
      } else {
        console.warn('⚠️ No services loaded - array is empty');
      }
      
      setServices(servicesArray);
    } catch (err) {
      console.error('❌ Error loading services:', err);
      console.error('Error details:', {
        message: err.message,
        stack: err.stack,
        fullError: err
      });
      setError(err.message || 'Failed to load services');
    } finally {
      setLoading(false);
    }
  };

  const getImageSrc = (service) => {
    const imageData = service?.imageData;
    
    if (!imageData) {
      return 'https://via.placeholder.com/400x250?text=No+Image';
    }
    
    return imageData;
  };

  const filteredServices = services.filter((service) =>
    service.name?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="service-list-container">
        <div className="service-list-wrapper">
          <div className="loading">Loading services...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="service-list-container">
        <div className="service-list-wrapper">
          <div className="error">
            <h3>Error Loading Services</h3>
            <p>{error}</p>
            <button className="btn btn-primary" onClick={loadServices} style={{ marginTop: '1rem' }}>
              Retry
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="service-list-container">
      <div className="service-list-wrapper">
        <div className="service-list-header">
          <div>
            <h1>Manage Services</h1>
          </div>
          <button className="btn btn-primary" onClick={onCreateNew}>
            + Create New Service
          </button>
        </div>

        <div className="search-container">
          <input
            type="text"
            className="search-input"
            placeholder="Search by service name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        {filteredServices.length === 0 ? (
          <div className="no-services">
            {searchTerm ? 'No services found matching your search.' : 'No services available. Create your first service!'}
          </div>
        ) : (
          <div className="services-grid">
            {filteredServices.map((service) => (
              <div
                key={service.id || service.serviceID}
                className="service-card"
                onClick={() => onServiceClick(service.id || service.serviceID)}
              >
                <div className="service-card-image">
                  <img 
                    src={getImageSrc(service)} 
                    alt={service.name || 'Service'}
                    onError={(e) => {
                      console.error(`🖼️ Image failed to load for: "${service.name}"`, {
                        attemptedSrc: e.target.src.substring(0, 100) + '...',
                        serviceId: service.id || service.serviceID,
                        hasImageData: !!service.imageData,
                        imageType: service.imageType
                      });
                      // Only show error placeholder if it's not already a placeholder
                      if (!e.target.src.includes('placeholder') && !e.target.src.includes('Image+Error')) {
                        e.target.src = 'https://via.placeholder.com/400x250?text=Image+Error';
                      }
                    }}
                    onLoad={() => {
                      console.log(`✅ Image loaded successfully for: "${service.name}"`);
                    }}
                    loading="lazy"
                  />
                  {service.completedTasks !== undefined && (
                    <div className="task-count-overlay">
                      {service.completedTasks || 0} tasks completed
                    </div>
                  )}
                </div>
                <div className="service-card-content">
                  <h3>{service.name}</h3>
                  <p className="service-description">
                    {service.description || 'No description available.'}
                  </p>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminServiceList;

