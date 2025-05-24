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

// MERCHANT API functions
export const merchantSignUpApi = (data: {
    email: string;
    password: string;
    firstName: string;
    lastName: string;
    middleName?: string;
    phoneNumber?: string;
}) => api.post('/merchants/signup', data);

export const merchantLoginApi = async (data: { email: string; password: string }) => {
    try {
        console.log('Attempting login with:', { email: data.email });
        const response = await api.post('/merchants/login', {
            email: data.email,
            password: data.password
        });
        console.log('Login response:', response.data);
        return response.data;
    } catch (error: any) {
        console.error('Login error:', error.response?.data || error.message);
        throw error;
    }
};

export const merchantPasswordResetRequestApi = (data: { email: string }) => api.post('/merchants/password-reset-request', data);
export const merchantPasswordResetApi = (data: { email: string; resetToken: string; newPassword: string }) => api.post('/merchants/password-reset', data);
export const merchantRefreshTokenApi = (token: string) => api.post('/merchants/refresh-token', token);

// CUSTOMER API functions
export const customerSignUpApi = (data: any) => api.post('/customer/signup', data);



// CART API functions
export const getCartApi = () => api.get('/customers/cart').then(res => res.data);
export const addToCartApi = (data: any) => api.post('/customers/cart/items', data);
export const removeFromCartApi = ({ itemId }: any) => api.delete(`/customers/cart/items/${itemId}`);
export const updateCartItemQuantityApi = ({ itemId, quantity }: any) => api.put(`/customers/cart/items/${itemId}`, { quantity });

// REFUND API functions
export const requestRefundApi = ({ orderId, ...data }: any) => api.post(`/customers/orders/${orderId}/refund`, data);
export const getCustomerRefundsApi = () => api.get('/customers/orders/refunds').then(res => res.data);
export const getOrderRefundsApi = (orderId: any) => api.get(`/customers/orders/${orderId}/refunds`).then(res => res.data);

// COMPLAINT API functions
export const submitComplaintApi = ({ orderId, ...data }: any) => api.post(`/customers/orders/${orderId}/complaint`, data);
export const getCustomerComplaintsApi = () => api.get('/customers/orders/complaints').then(res => res.data);
export const getOrderComplaintsApi = (orderId: any) => api.get(`/customers/orders/${orderId}/complaints`).then(res => res.data);

// ORDER (MERCHANT) API functions
export const getOrdersApi = (params: any) => api.get('/merchants/orders', { params }).then(res => res.data);
export const updateOrderStatusApi = ({ orderId, status }: any) => api.put(`/merchants/orders/${orderId}/status`, { status });
export const updateOrderTrackingApi = ({ orderId, trackingId }: any) => api.put(`/merchants/orders/${orderId}/tracking`, { trackingId });
export const getOrdersByLinkApi = (linkId: any) => api.get(`/merchants/orders/link/${linkId}`).then(res => res.data);

// PRODUCT LINK API functions
export const generateProductLinkApi = ({ productId }: any) => api.post(`/merchants/products/${productId}/generate-link`);
export const getProductLinksApi = () => api.get('/merchants/products/links').then(res => res.data);
export const getLinkAnalyticsApi = ({ linkId, startDate, endDate }: any) => api.get(`/merchants/products/links/${linkId}/analytics`, { params: { startDate, endDate } }).then(res => res.data);
export const getTrafficSourcesApi = (linkId: any) => api.get(`/merchants/products/links/${linkId}/traffic`).then(res => res.data);

// PRODUCT API functions
export const addProductApi = (data: any) => api.post('/merchants/products', data);
export const updateProductApi = ({ productId, ...data }: any) => api.put(`/merchants/products/${productId}`, data);
export const deleteProductApi = (productId: any) => api.delete(`/merchants/products/${productId}`);
export const getProductsApi = () => api.get('/merchants/products').then(res => res.data);
export const searchProductsApi = (query: any) => api.get('/merchants/products/search', { params: { query } }).then(res => res.data);
export const getInStockProductsApi = () => api.get('/merchants/products/in-stock').then(res => res.data);

