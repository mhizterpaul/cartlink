import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
});

// Add request interceptor to include auth token
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Add response interceptor to handle errors
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response) {
            // The request was made and the server responded with a status code
            // that falls out of the range of 2xx
            console.error('Response error:', error.response.data);
        } else if (error.request) {
            // The request was made but no response was received
            console.error('Request error:', error.request);
        } else {
            // Something happened in setting up the request that triggered an Error
            console.error('Error:', error.message);
        }
        return Promise.reject(error);
    }
);

// Auth API
export const auth = {
    merchant: {
        signUp: (data: {
            email: string;
            password: string;
            firstName: string;
            lastName: string;
            middleName?: string;
            phoneNumber?: string;
        }) => api.post('/merchant/signup', data),

        login: async (data: { email: string; password: string }) => {
            try {
                console.log('Attempting login with:', { email: data.email });
                const response = await api.post('/merchant/login', {
                    email: data.email,
                    password: data.password
                });
                console.log('Login response:', response.data);
                return response.data;
            } catch (error: any) {
                console.error('Login error:', error.response?.data || error.message);
                throw error;
            }
        },

        passwordResetRequest: (data: { email: string }) =>
            api.post('/merchant/password-reset-request', data),

        passwordReset: (data: { email: string; resetToken: string; newPassword: string }) =>
            api.post('/merchant/password-reset', data),

        refreshToken: (token: string) =>
            api.post('/merchant/refresh-token', token),

        getProfile: () => api.get('/merchant/profile').then(res => res.data),

        updateProfile: (data: {
            firstName: string;
            lastName: string;
            phoneNumber?: string;
            [key: string]: any;
        }) => api.put('/merchant/profile', data)
    },

    customer: {
        signUp: (data: any) => api.post('/customer/signup', data)
    }
};

// Order API
export const order = {
    merchant: {
        getAll: (params?: any) => api.get('/merchant/orders', { params }).then(res => res.data),
        updateStatus: ({ orderId, status }: { orderId: number; status: string }) =>
            api.put(`/merchant/orders/${orderId}/status`, { status }),
        updateTracking: ({ orderId, trackingId }: { orderId: number; trackingId: string }) =>
            api.put(`/merchant/orders/${orderId}/tracking`, { trackingId }),
        getByLink: (linkId: number) =>
            api.get(`/merchant/orders/link/${linkId}`).then(res => res.data)
    },
    customer: {
        getCart: () => api.get('/customers/cart').then(res => res.data),
        addToCart: (data: any) => api.post('/customers/cart/items', data),
        removeFromCart: ({ itemId }: { itemId: number }) =>
            api.delete(`/customers/cart/items/${itemId}`),
        updateQuantity: ({ itemId, quantity }: { itemId: number; quantity: number }) =>
            api.put(`/customers/cart/items/${itemId}`, { quantity })
    }
};

// Product API
export const product = {
    merchant: {
        add: (data: any) => api.post('/merchant/products', data),
        update: ({ productId, ...data }: { productId: number;[key: string]: any }) =>
            api.put(`/merchant/products/${productId}`, data),
        delete: (productId: number) => api.delete(`/merchant/products/${productId}`),
        getAll: () => api.get('/merchant/products').then(res => res.data),
        search: (query: string) => api.get('/merchant/products/search', { params: { query } }).then(res => res.data),
        getInStock: () => api.get('/merchant/products/in-stock').then(res => res.data)
    }
};

// Product Link API
export const productLink = {
    generate: ({ productId }: { productId: number }) =>
        api.post(`/merchant/products/${productId}/generate-link`),
    getAll: () => api.get('/merchant/products/links').then(res => res.data),
    getAnalytics: ({ linkId, startDate, endDate }: { linkId: number; startDate: string; endDate: string }) =>
        api.get(`/merchant/products/links/${linkId}/analytics`, { params: { startDate, endDate } }).then(res => res.data),
    getTrafficSources: (linkId: number) =>
        api.get(`/merchant/products/links/${linkId}/traffic`).then(res => res.data)
};

// Complaint API
export const complaint = {
    submit: ({ orderId, ...data }: { orderId: number;[key: string]: any }) =>
        api.post(`/customers/orders/${orderId}/complaint`, data),
    getCustomerComplaints: () => api.get('/customers/orders/complaints').then(res => res.data),
    getOrderComplaints: (orderId: number) =>
        api.get(`/customers/orders/${orderId}/complaints`).then(res => res.data)
};

// Refund API
export const refund = {
    request: ({ orderId, ...data }: { orderId: number;[key: string]: any }) =>
        api.post(`/customers/orders/${orderId}/refund`, data),
    getCustomerRefunds: () => api.get('/customers/orders/refunds').then(res => res.data),
    getOrderRefunds: (orderId: number) =>
        api.get(`/customers/orders/${orderId}/refunds`).then(res => res.data)
};

// Dashboard API
export const dashboard = {
    getStats: () => api.get('/merchant/dashboard/stats').then(res => res.data),
    getSalesData: () => api.get('/merchant/dashboard/sales-data').then(res => res.data),
    getTrafficData: () => api.get('/merchant/dashboard/traffic-data').then(res => res.data)
};





