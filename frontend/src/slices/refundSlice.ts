import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { requestRefundApi, getCustomerRefundsApi, getOrderRefundsApi } from '../api';

interface RefundState {
    refunds: any;
    loading: boolean;
    error: any;
}

export const requestRefund = createAsyncThunk<any, { orderId: string; data: any }>(
    'refund/requestRefund',
    async ({ orderId, data }, { rejectWithValue }) => {
        try {
            const response = await requestRefundApi({ orderId, ...data });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getCustomerRefunds = createAsyncThunk<any>(
    'refund/getCustomerRefunds',
    async (_, { rejectWithValue }) => {
        try {
            const response = await getCustomerRefundsApi();
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getOrderRefunds = createAsyncThunk<any, string>(
    'refund/getOrderRefunds',
    async (orderId, { rejectWithValue }) => {
        try {
            const response = await getOrderRefundsApi(orderId);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: RefundState = {
    refunds: null,
    loading: false,
    error: null,
};

const refundSlice = createSlice({
    name: 'refund',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(requestRefund.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(requestRefund.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(requestRefund.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getCustomerRefunds.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getCustomerRefunds.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.refunds = action.payload; })
            .addCase(getCustomerRefunds.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getOrderRefunds.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getOrderRefunds.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.refunds = action.payload; })
            .addCase(getOrderRefunds.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; });
    },
});

export default refundSlice.reducer; 