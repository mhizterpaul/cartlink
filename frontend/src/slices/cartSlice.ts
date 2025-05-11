import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import axios from 'axios';

interface CartState {
    cart: any;
    loading: boolean;
    error: any;
}

export const getCart = createAsyncThunk<any>(
    'cart/getCart',
    async (_, { rejectWithValue }) => {
        try {
            const response = await axios.get('/api/customers/cart');
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const addToCart = createAsyncThunk<any, any>(
    'cart/addToCart',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await axios.post('/api/customers/cart/items', data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const removeFromCart = createAsyncThunk<any, { itemId: string }>(
    'cart/removeFromCart',
    async ({ itemId }, { rejectWithValue }) => {
        try {
            const response = await axios.delete(`/api/customers/cart/items/${itemId}`);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const updateCartItemQuantity = createAsyncThunk<any, { itemId: string, quantity: number }>(
    'cart/updateCartItemQuantity',
    async ({ itemId, quantity }, { rejectWithValue }) => {
        try {
            const response = await axios.put(`/api/customers/cart/items/${itemId}`, { quantity });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: CartState = {
    cart: null,
    loading: false,
    error: null,
};

const cartSlice = createSlice({
    name: 'cart',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(getCart.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getCart.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.cart = action.payload; })
            .addCase(getCart.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(addToCart.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(addToCart.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(addToCart.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(removeFromCart.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(removeFromCart.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(removeFromCart.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(updateCartItemQuantity.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(updateCartItemQuantity.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(updateCartItemQuantity.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; });
    },
});

export default cartSlice.reducer; 