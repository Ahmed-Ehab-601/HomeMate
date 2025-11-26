// Update this to match your backend URL and base path
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/service';

// Helper function to convert byte array to base64
const convertImageData = (service) => {
  if (service?.imageData && Array.isArray(service.imageData)) {
    try {
      const base64 = btoa(
        service.imageData.map(byte => String.fromCharCode(byte)).join('')
      );
      service.imageData = base64;
    } catch (error) {
      console.error('Error converting image data:', error);
    }
  }
  return service;
};

// Helper function to handle API errors
const handleApiError = async (response) => {
  const contentType = response.headers.get('content-type');
  
  if (contentType && contentType.includes('application/json')) {
    const errorData = await response.json();
    
    // Handle Spring Boot validation errors (from GlobalExceptionHandler)
    if (errorData.errors) {
      // Format: { status: "error", errors: { fieldName: "error message" } }
      const errorMessages = Object.entries(errorData.errors)
        .map(([field, message]) => `${field}: ${message}`)
        .join('\n');
      throw new Error(errorMessages);
    }
    
    // Handle other structured errors
    if (errorData.message) {
      throw new Error(errorData.message);
    }
    
    throw new Error(JSON.stringify(errorData));
  } else {
    const errorText = await response.text();
    throw new Error(errorText || `Request failed with status ${response.status}`);
  }
};

export const serviceAPI = {
  // Get all services
  getAllServices: async () => {
    try {
      console.log('Fetching services from:', `${API_BASE_URL}/getallservices`);
      const response = await fetch(`${API_BASE_URL}/getallservices`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        await handleApiError(response);
      }
      
      const data = await response.json();
      
      if (Array.isArray(data)) {
        return data.map(service => convertImageData(service));
      }
      
      return data;
    } catch (err) {
      console.error('Fetch error:', err);
      throw err;
    }
  },

  // Get service details by ID
  getServiceDetails: async (serviceID) => {
    try {
      console.log('Fetching service details for ID:', serviceID);
      const response = await fetch(`${API_BASE_URL}/getservicedetails/${serviceID}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        await handleApiError(response);
      }
      
      const data = await response.json();
      
      if (data?.service) {
        data.service = convertImageData(data.service);
      }
      
      return data;
    } catch (err) {
      console.error('Fetch error:', err);
      throw err;
    }
  },

  // Create a new service
  createService: async (serviceDto) => {
    try {
      console.log('Creating service:', serviceDto);
      const response = await fetch(`${API_BASE_URL}/create`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(serviceDto),
      });
      
      if (!response.ok) {
        await handleApiError(response);
      }
      
      return response.text();
    } catch (err) {
      console.error('Create error:', err);
      throw err;
    }
  },

  // Edit/Update a service
  editService: async (id, serviceDto) => {
    try {
      console.log('Editing service ID:', id, serviceDto);
      const response = await fetch(`${API_BASE_URL}/edit/${id}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(serviceDto),
      });
      
      if (!response.ok) {
        await handleApiError(response);
      }
      
      return response.text();
    } catch (err) {
      console.error('Edit error:', err);
      throw err;
    }
  },

  // Delete a service
  deleteService: async (id) => {
    try {
      console.log('Deleting service ID:', id);
      const response = await fetch(`${API_BASE_URL}/delete/${id}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
        },
      });
      
      if (!response.ok) {
        await handleApiError(response);
      }
      
      return response.text(); 
    } catch (err) {
      console.error('Delete error:', err);
      throw err;
    }
  },
};