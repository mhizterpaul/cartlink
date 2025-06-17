import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { dashboard } from '../api';

interface DashboardState {
    stats: any | null;
    salesData: any | null;
    trafficData: any | null;
    loading: boolean;
    error: string | null;
}

const initialState: DashboardState = {
    stats: null,
    salesData: null,
    trafficData: null,
    loading: false,
    error: null
};

export const fetchDashboardStats = createAsyncThunk(
    'dashboard/fetchStats',
    async (_, { rejectWithValue }) => {
        try {
            const response = await dashboard.getStats();
            return response;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || 'Failed to fetch dashboard stats');
        }
    }
);

export const fetchSalesData = createAsyncThunk(
    'dashboard/fetchSalesData',
    async (_, { rejectWithValue }) => {
        try {
            const response = await dashboard.getSalesData();
            return response;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || 'Failed to fetch sales data');
        }
    }
);

export const fetchTrafficData = createAsyncThunk(
    'dashboard/fetchTrafficData',
    async (_, { rejectWithValue }) => {
        try {
            const response = await dashboard.getTrafficData();
            return response;
        } catch (error: any) {
            return rejectWithValue(error.response?.data?.message || 'Failed to fetch traffic data');
        }
    }
);

const dashboardSlice = createSlice({
    name: 'dashboard',
    initialState,
    reducers: {
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            // Stats
            .addCase(fetchDashboardStats.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchDashboardStats.fulfilled, (state, action) => {
                state.loading = false;
                state.stats = action.payload;
            })
            .addCase(fetchDashboardStats.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            // Sales Data
            .addCase(fetchSalesData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchSalesData.fulfilled, (state, action) => {
                state.loading = false;
                state.salesData = action.payload;
            })
            .addCase(fetchSalesData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            })
            // Traffic Data
            .addCase(fetchTrafficData.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchTrafficData.fulfilled, (state, action) => {
                state.loading = false;
                state.trafficData = action.payload;
            })
            .addCase(fetchTrafficData.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload as string;
            });
    }
});

export const { clearError } = dashboardSlice.actions;
export default dashboardSlice.reducer; 