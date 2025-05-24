import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { addProductApi, updateProductApi, deleteProductApi, getProductsApi, searchProductsApi, getInStockProductsApi } from '../api';

interface ProductState {
    products: any;
    inStockProducts: any;
    loading: boolean;
    error: any;
}

export const addProduct = createAsyncThunk<any, any>(
    'product/addProduct',
    async (data, { rejectWithValue }) => {
        try {
            const response = await addProductApi(data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const updateProduct = createAsyncThunk<any, { productId: string; data: any }>(
    'product/updateProduct',
    async ({ productId, data }, { rejectWithValue }) => {
        try {
            const response = await updateProductApi({ productId, ...data });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const deleteProduct = createAsyncThunk<any, string>(
    'product/deleteProduct',
    async (productId, { rejectWithValue }) => {
        try {
            const response = await deleteProductApi(productId);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getProducts = createAsyncThunk<any>(
    'product/getProducts',
    async (_, { rejectWithValue }) => {
        try {
            const response = await getProductsApi();
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const searchProducts = createAsyncThunk<any, string>(
    'product/searchProducts',
    async (query, { rejectWithValue }) => {
        try {
            const response = await searchProductsApi(query);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const getInStockProducts = createAsyncThunk<any>(
    'product/getInStockProducts',
    async (_, { rejectWithValue }) => {
        try {
            const response = await getInStockProductsApi();
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: ProductState = {
    products: null,
    inStockProducts: null,
    loading: false,
    error: null,
};

const productSlice = createSlice({
    name: 'product',
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder
            .addCase(addProduct.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(addProduct.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(addProduct.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(updateProduct.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(updateProduct.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(updateProduct.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(deleteProduct.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(deleteProduct.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(deleteProduct.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getProducts.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getProducts.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.products = action.payload; })
            .addCase(getProducts.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(searchProducts.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(searchProducts.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.products = action.payload; })
            .addCase(searchProducts.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(getInStockProducts.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(getInStockProducts.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; state.inStockProducts = action.payload; })
            .addCase(getInStockProducts.rejected, (state, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; });
    },
});

export default productSlice.reducer; 