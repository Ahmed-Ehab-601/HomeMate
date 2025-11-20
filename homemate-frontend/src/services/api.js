// Update this to match your backend URL and base path
// Your controller has @RequestMapping("/service"), so all endpoints are under /service
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/service';

// If using Vite proxy, you might want to use relative URLs
// Uncomment the line below and comment the line above if using proxy
// const API_BASE_URL = '';

// Helper function to convert byte array to base64
const convertImageData = (service) => {
  if (service?.imageData && Array.isArray(service.imageData)) {
    try {
      // Convert byte array to base64 string
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
      
      console.log('Response status:', response.status);
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Failed to fetch services: ${response.status} ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Services data:', data);
      
      // Convert image data for each service
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
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Failed to fetch service details: ${response.status} ${response.statusText}`);
      }
      
      const data = await response.json();
      console.log('Service details received:', data);
      
      // Convert image data for the service within serviceDetails
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
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Failed to create service: ${response.status} ${response.statusText}`);
      }
      
      return response.text(); // Returns "Service created successfully!"
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
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Failed to edit service: ${response.status} ${response.statusText}`);
      }
      
      return response.text(); // Returns "Service edited successfully!"
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
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`Failed to delete service: ${response.status} ${response.statusText}`);
      }
      
      return response.text(); 
    } catch (err) {
      console.error('Delete error:', err);
      throw err;
    }
  },
};