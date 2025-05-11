import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';

const api = axios.create({
    baseURL: '/api',
});

// AUTH
export const useSignUp = () => useMutation((data) => api.post('/auth/signup', data));
export const useLogin = () => useMutation((data) => api.post('/auth/login', data));
export const useRefreshToken = () => useMutation((token) => api.post('/auth/refresh', {}, { headers: { Authorization: token } }));


// MERCHANT
export const useMerchantSignUp = () => useMutation((data) => api.post('/merchants/signup', data));
export const useMerchantLogin = () => useMutation((data) => api.post('/merchants/login', data));
export const useMerchantPasswordResetRequest = () => useMutation((data) => api.post('/merchants/password-reset-request', data));
export const useMerchantPasswordReset = () => useMutation((data) => api.post('/merchants/password-reset', data));

// CART
export const useGetCart = () => useQuery(['cart'], () => api.get('/customers/cart').then(res => res.data));
export const useAddToCart = () => useMutation((data) => api.post('/customers/cart/items', data));
export const useRemoveFromCart = () => useMutation(({ itemId }) => api.delete(`/customers/cart/items/${itemId}`));
export const useUpdateCartItemQuantity = () => useMutation(({ itemId, quantity }) => api.put(`/customers/cart/items/${itemId}`, { quantity }));

// REFUND
export const useRequestRefund = () => useMutation(({ orderId, ...data }) => api.post(`/customers/orders/${orderId}/refund`, data));
export const useGetCustomerRefunds = () => useQuery(['refunds'], () => api.get('/customers/orders/refunds').then(res => res.data));
export const useGetOrderRefunds = (orderId) => useQuery(['orderRefunds', orderId], () => api.get(`/customers/orders/${orderId}/refunds`).then(res => res.data));

// COMPLAINT
export const useSubmitComplaint = () => useMutation(({ orderId, ...data }) => api.post(`/customers/orders/${orderId}/complaint`, data));
export const useGetCustomerComplaints = () => useQuery(['complaints'], () => api.get('/customers/orders/complaints').then(res => res.data));
export const useGetOrderComplaints = (orderId) => useQuery(['orderComplaints', orderId], () => api.get(`/customers/orders/${orderId}/complaints`).then(res => res.data));

// ORDER (MERCHANT)
export const useGetOrders = (params) => useQuery(['orders', params], () => api.get('/merchants/orders', { params }).then(res => res.data));
export const useUpdateOrderStatus = () => useMutation(({ orderId, status }) => api.put(`/merchants/orders/${orderId}/status`, { status }));
export const useUpdateOrderTracking = () => useMutation(({ orderId, trackingId }) => api.put(`/merchants/orders/${orderId}/tracking`, { trackingId }));
export const useGetOrdersByLink = (linkId) => useQuery(['ordersByLink', linkId], () => api.get(`/merchants/orders/link/${linkId}`).then(res => res.data));

// PRODUCT LINK
export const useGenerateProductLink = () => useMutation(({ productId }) => api.post(`/merchants/products/${productId}/generate-link`));
export const useGetProductLinks = () => useQuery(['productLinks'], () => api.get('/merchants/products/links').then(res => res.data));
export const useGetLinkAnalytics = ({ linkId, startDate, endDate }) => useQuery(['linkAnalytics', linkId, startDate, endDate], () => api.get(`/merchants/products/links/${linkId}/analytics`, { params: { startDate, endDate } }).then(res => res.data));
export const useGetTrafficSources = (linkId) => useQuery(['trafficSources', linkId], () => api.get(`/merchants/products/links/${linkId}/traffic`).then(res => res.data));

// PRODUCT
export const useAddProduct = () => useMutation((data) => api.post('/merchants/products', data));
export const useUpdateProduct = () => useMutation(({ productId, ...data }) => api.put(`/merchants/products/${productId}`, data));
export const useDeleteProduct = () => useMutation((productId) => api.delete(`/merchants/products/${productId}`));
export const useGetProducts = () => useQuery(['products'], () => api.get('/merchants/products').then(res => res.data));
export const useSearchProducts = (query) => useQuery(['searchProducts', query], () => api.get('/merchants/products/search', { params: { query } }).then(res => res.data));
export const useGetInStockProducts = () => useQuery(['inStockProducts'], () => api.get('/merchants/products/in-stock').then(res => res.data)); 