import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { productLink } from '../api';

interface ProductLinkState {
    productLinks: any;
    analytics: any;
    trafficSources: any;
    loading: boolean;
    error: any;
}

export const generateProductLink = createAsyncThunk<any, { productId: number }>(
    'productLink/generateProductLink',
    async ({ productId }, { rejectWithValue }) => {
        try {
            const response = await productLink.generate({ productId });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getProductLinks = createAsyncThunk<any>(
    'productLink/getProductLinks',
    async (_, { rejectWithValue }) => {
        try {
            const response = await productLink.getAll();
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getLinkAnalytics = createAsyncThunk<any, { linkId: number; startDate?: string; endDate?: string }>(
    'productLink/getLinkAnalytics',
    async ({ linkId, startDate, endDate }, { rejectWithValue }) => {
        try {
            const response = await productLink.getAnalytics({ linkId, startDate, endDate });
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getTrafficSources = createAsyncThunk<any, number>(
    'productLink/getTrafficSources',
    async (linkId, { rejectWithValue }) => {
        try {
            const response = await productLink.getTrafficSources(linkId);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: ProductLinkState = {
    productLinks: null,
    analytics: null,
    trafficSources: null,
    loading: false,
    error: null,
};

const productLinkSlice = createSlice({
    name: 'productLink',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(generateProductLink.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(generateProductLink.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(generateProductLink.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getProductLinks.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getProductLinks.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.productLinks = action.payload; })
            .addCase(getProductLinks.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getLinkAnalytics.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getLinkAnalytics.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.analytics = action.payload; })
            .addCase(getLinkAnalytics.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getTrafficSources.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getTrafficSources.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.trafficSources = action.payload; })
            .addCase(getTrafficSources.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; });
    },
});

export default productLinkSlice.reducer; 