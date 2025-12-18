import { baseUrl } from '../utils/apiClient';

// Update this to match your backend URL and base path
const API_BASE_URL = `${baseUrl}/api/service`;

// Get authentication headers
const getAuthHeaders = () => {
  const token = localStorage.getItem('homemate_token');
  const headers = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
};

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

export const setUserOffline = async (userId) => {
  const token = localStorage.getItem("token");
  const response = await fetch(`/api/user/${userId}/offline`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
  });
  if (!response.ok) {
    throw new Error("Failed to set user offline");
  }
  return response.json();
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
      const token = localStorage.getItem('homemate_token');
      const user = JSON.parse(localStorage.getItem('homemate_user') || '{}');
      console.log('🔵 [SERVICE] API Request:', {
        url: `${API_BASE_URL}/getallservices`,
        method: 'GET',
        auth: {
          hasToken: !!token,
          token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
          userRole: user?.role || 'UNKNOWN',
          userId: user?.id || 'UNKNOWN',
        },
      });
      const response = await fetch(`${API_BASE_URL}/getallservices`, {
        method: 'GET',
        headers: getAuthHeaders(),
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
      const token = localStorage.getItem('homemate_token');
      const user = JSON.parse(localStorage.getItem('homemate_user') || '{}');
      console.log('🔵 [SERVICE] API Request:', {
        url: `${API_BASE_URL}/getservicedetails/${serviceID}`,
        method: 'GET',
        serviceID,
        auth: {
          hasToken: !!token,
          token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
          userRole: user?.role || 'UNKNOWN',
          userId: user?.id || 'UNKNOWN',
        },
      });
      const response = await fetch(`${API_BASE_URL}/getservicedetails/${serviceID}`, {
        method: 'GET',
        headers: getAuthHeaders(),
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
      const token = localStorage.getItem('homemate_token');
      const user = JSON.parse(localStorage.getItem('homemate_user') || '{}');
      console.log('🔵 [SERVICE] API Request:', {
        url: `${API_BASE_URL}/create`,
        method: 'POST',
        auth: {
          hasToken: !!token,
          token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
          userRole: user?.role || 'UNKNOWN',
          userId: user?.id || 'UNKNOWN',
        },
        body: serviceDto,
      });
      const response = await fetch(`${API_BASE_URL}/create`, {
        method: 'POST',
        headers: getAuthHeaders(),
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
      const token = localStorage.getItem('homemate_token');
      const user = JSON.parse(localStorage.getItem('homemate_user') || '{}');
      console.log('🔵 [SERVICE] API Request:', {
        url: `${API_BASE_URL}/edit/${id}`,
        method: 'POST',
        serviceId: id,
        auth: {
          hasToken: !!token,
          token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
          userRole: user?.role || 'UNKNOWN',
          userId: user?.id || 'UNKNOWN',
        },
        body: serviceDto,
      });
      const response = await fetch(`${API_BASE_URL}/edit/${id}`, {
        method: 'POST',
        headers: getAuthHeaders(),
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
      const token = localStorage.getItem('homemate_token');
      const user = JSON.parse(localStorage.getItem('homemate_user') || '{}');
      console.log('🔵 [SERVICE] API Request:', {
        url: `${API_BASE_URL}/delete/${id}`,
        method: 'DELETE',
        serviceId: id,
        auth: {
          hasToken: !!token,
          token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
          userRole: user?.role || 'UNKNOWN',
          userId: user?.id || 'UNKNOWN',
        },
      });
      const response = await fetch(`${API_BASE_URL}/delete/${id}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
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