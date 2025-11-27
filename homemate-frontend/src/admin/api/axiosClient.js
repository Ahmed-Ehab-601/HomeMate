import axios from 'axios';

// Use relative path for dev (goes through Vite proxy), or env variable for production
const API_BASE_URL = import.meta?.env?.VITE_API_BASE_URL?.replace(/\/$/, '') || '';

const axiosClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('homemate_token');
  console.log('🔵 Admin API Request:', {
    url: config.url,
    method: config.method?.toUpperCase(),
    baseURL: config.baseURL,
    fullURL: `${config.baseURL || ''}${config.url}`,
    headers: config.headers,
    token: token ? `${token.substring(0, 20)}...` : 'NO TOKEN',
    data: config.data,
  });
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

axiosClient.interceptors.response.use(
  (response) => {
    console.log('✅ Admin API Response:', {
      url: response.config.url,
      status: response.status,
      statusText: response.statusText,
      data: response.data,
    });
    return response;
  },
  (error) => {
    console.error('❌ Admin API Error:', {
      url: error?.config?.url,
      method: error?.config?.method?.toUpperCase(),
      status: error?.response?.status,
      statusText: error?.response?.statusText,
      responseData: error?.response?.data,
      headers: error?.config?.headers,
      message: error?.message,
    });

    // Handle 401 Unauthorized - redirect to signin
    if (error?.response?.status === 401) {
      localStorage.removeItem('homemate_user');
      localStorage.removeItem('homemate_token');
      if (window.location.pathname !== '/signin' && window.location.pathname !== '/signup') {
        window.location.href = '/signin';
      }
    }

    // Handle 403 Forbidden
    if (error?.response?.status === 403) {
      console.error('🚫 403 Forbidden - Check:', {
        tokenExists: !!localStorage.getItem('homemate_token'),
        userRole: JSON.parse(localStorage.getItem('homemate_user') || '{}')?.role,
        endpoint: error?.config?.url,
      });
    }

    const message =
      error?.response?.data?.message ||
      error?.response?.data?.error ||
      error.message ||
      'Unexpected error';
    return Promise.reject(new Error(message));
  },
);

export default axiosClient;

