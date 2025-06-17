import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { refund } from '../api';

interface RefundState {
    refunds: any;
    loading: boolean;
    error: string | null;
}

export const requestRefund = createAsyncThunk(
    'refund/requestRefund',
    async ({ orderId, ...data }: { orderId: number;[key: string]: any }, { rejectWithValue }) => {
        try {
            const response = await refund.request({ orderId, ...data });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getCustomerRefunds = createAsyncThunk(
    'refund/getCustomerRefunds',
    async (_, { rejectWithValue }) => {
        try {
            const response = await refund.getCustomerRefunds();
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getOrderRefunds = createAsyncThunk(
    'refund/getOrderRefunds',
    async (orderId: number, { rejectWithValue }) => {
        try {
            const response = await refund.getOrderRefunds(orderId);
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
    reducers: {
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(requestRefund.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(requestRefund.fulfilled, (state) => {
                state.loading = false;
            })
            .addCase(requestRefund.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(getCustomerRefunds.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getCustomerRefunds.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.refunds = action.payload;
            })
            .addCase(getCustomerRefunds.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(getOrderRefunds.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(getOrderRefunds.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.refunds = action.payload;
            })
            .addCase(getOrderRefunds.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            });
    },
});

export const { clearError } = refundSlice.actions;
export default refundSlice.reducer; 