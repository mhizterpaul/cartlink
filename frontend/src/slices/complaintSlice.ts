import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import axios from 'axios';

interface ComplaintState {
    complaints: any;
    loading: boolean;
    error: any;
}

export const submitComplaint = createAsyncThunk<any, { orderId: string; data: any }>(
    'complaint/submitComplaint',
    async ({ orderId, data }, { rejectWithValue }) => {
        try {
            const response = await axios.post(`/api/customers/orders/${orderId}/complaint`, data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getCustomerComplaints = createAsyncThunk<any>(
    'complaint/getCustomerComplaints',
    async (_, { rejectWithValue }) => {
        try {
            const response = await axios.get('/api/customers/orders/complaints');
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getOrderComplaints = createAsyncThunk<any, string>(
    'complaint/getOrderComplaints',
    async (orderId, { rejectWithValue }) => {
        try {
            const response = await axios.get(`/api/customers/orders/${orderId}/complaints`);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: ComplaintState = {
    complaints: null,
    loading: false,
    error: null,
};

const complaintSlice = createSlice({
    name: 'complaint',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(submitComplaint.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(submitComplaint.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(submitComplaint.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getCustomerComplaints.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getCustomerComplaints.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.complaints = action.payload; })
            .addCase(getCustomerComplaints.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getOrderComplaints.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getOrderComplaints.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.complaints = action.payload; })
            .addCase(getOrderComplaints.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; });
    },
});

export default complaintSlice.reducer; 