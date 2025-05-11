import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import axios from 'axios';

interface OrderState {
    orders: any;
    loading: boolean;
    error: any;
}

export const getOrders = createAsyncThunk<any, any>(
    'order/getOrders',
    async (params, { rejectWithValue }) => {
        try {
            const response = await axios.get('/api/merchants/orders', { params });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const updateOrderStatus = createAsyncThunk<any, { orderId: string; status: string }>(
    'order/updateOrderStatus',
    async ({ orderId, status }, { rejectWithValue }) => {
        try {
            const response = await axios.put(`/api/merchants/orders/${orderId}/status`, { status });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const updateOrderTracking = createAsyncThunk<any, { orderId: string; trackingId: string }>(
    'order/updateOrderTracking',
    async ({ orderId, trackingId }, { rejectWithValue }) => {
        try {
            const response = await axios.put(`/api/merchants/orders/${orderId}/tracking`, { trackingId });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getOrdersByLink = createAsyncThunk<any, string>(
    'order/getOrdersByLink',
    async (linkId, { rejectWithValue }) => {
        try {
            const response = await axios.get(`/api/merchants/orders/link/${linkId}`);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: OrderState = {
    orders: null,
    loading: false,
    error: null,
};

const orderSlice = createSlice({
    name: 'order',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getOrders.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getOrders.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.orders = action.payload; })
            .addCase(getOrders.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(updateOrderStatus.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(updateOrderStatus.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(updateOrderStatus.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(updateOrderTracking.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(updateOrderTracking.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(updateOrderTracking.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getOrdersByLink.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getOrdersByLink.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.orders = action.payload; })
            .addCase(getOrdersByLink.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; });
    },
});

export default orderSlice.reducer; 