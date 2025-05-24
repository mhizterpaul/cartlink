import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add request interceptor to add auth token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const merchantApi = {
    getProfile: () => api.get('/merchant/profile'),
    updateProfile: (data: any) => api.put('/merchant/profile', data),
    getReviews: () => api.get('/merchant/reviews'),
    getComplaints: () => api.get('/merchant/complaints'),
    getOrders: () => api.get('/merchant/orders'),
    getDashboardStats: () => api.get('/merchant/dashboard/stats'),
};

export default api; 