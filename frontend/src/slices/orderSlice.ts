import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { order } from '../api';

interface OrderState {
    orders: any;
    loading: boolean;
    error: string | null;
}

export const getOrders = createAsyncThunk(
    'order/getOrders',
    async (params: any, { rejectWithValue }) => {
        try {
            const response = await order.merchant.getAll(params);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const updateOrderStatus = createAsyncThunk(
    'order/updateOrderStatus',
    async ({ orderId, status }: { orderId: number; status: string }, { rejectWithValue }) => {
        try {
            const response = await order.merchant.updateStatus({ orderId, status });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const updateOrderTracking = createAsyncThunk(
    'order/updateOrderTracking',
    async ({ orderId, trackingId }: { orderId: number; trackingId: string }, { rejectWithValue }) => {
        try {
            const response = await order.merchant.updateTracking({ orderId, trackingId });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getOrdersByLink = createAsyncThunk(
    'order/getOrdersByLink',
    async (linkId: number, { rejectWithValue }) => {
        try {
            const response = await order.merchant.getByLink(linkId);
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
    reducers: {
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(getOrders.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getOrders.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.orders = action.payload;
            })
            .addCase(getOrders.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(updateOrderStatus.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(updateOrderStatus.fulfilled, (state) => {
                state.loading = false;
            })
            .addCase(updateOrderStatus.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(updateOrderTracking.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(updateOrderTracking.fulfilled, (state) => {
                state.loading = false;
            })
            .addCase(updateOrderTracking.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(getOrdersByLink.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getOrdersByLink.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.orders = action.payload;
            })
            .addCase(getOrdersByLink.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            });
    },
});

export const { clearError } = orderSlice.actions;
export default orderSlice.reducer; 