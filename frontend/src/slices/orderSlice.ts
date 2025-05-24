import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { getOrdersApi, updateOrderStatusApi, updateOrderTrackingApi, getOrdersByLinkApi } from '../api';

interface OrderState {
    orders: any;
    loading: boolean;
    error: any;
}

export const getOrders = createAsyncThunk<any, any>(
    'order/getOrders',
    async (params, { rejectWithValue }) => {
        try {
            const response = await getOrdersApi(params);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const updateOrderStatus = createAsyncThunk<any, { orderId: string; status: string }>(
    'order/updateOrderStatus',
    async ({ orderId, status }, { rejectWithValue }) => {
        try {
            const response = await updateOrderStatusApi({ orderId, status });
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
            const response = await updateOrderTrackingApi({ orderId, trackingId });
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
            const response = await getOrdersByLinkApi(linkId);
            return response;
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