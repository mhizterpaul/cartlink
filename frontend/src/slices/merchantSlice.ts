import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import { auth } from '../api';

interface MerchantState {
    merchant: any | null;
    token: string | null;
    loading: boolean;
    error: string | null;
}

const initialState: MerchantState = {
    merchant: null,
    token: localStorage.getItem('token'),
    loading: false,
    error: null
};

export const loginMerchant = createAsyncThunk(
    'merchant/login',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await auth.merchant.login(data);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const signUpMerchant = createAsyncThunk(
    'merchant/signup',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await auth.merchant.signUp(data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const requestPasswordReset = createAsyncThunk(
    'merchant/requestPasswordReset',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await auth.merchant.passwordResetRequest(data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const resetPassword = createAsyncThunk(
    'merchant/resetPassword',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await auth.merchant.passwordReset(data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const refreshToken = createAsyncThunk(
    'merchant/refreshToken',
    async (token: string, { rejectWithValue }) => {
        try {
            const response = await auth.merchant.refreshToken(token);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const merchantSlice = createSlice({
    name: 'merchant',
    initialState,
    reducers: {
        logout: (state) => {
            state.merchant = null;
            state.token = null;
            localStorage.removeItem('token');
        },
        clearError: (state) => {
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(loginMerchant.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(loginMerchant.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.merchant = action.payload.merchant;
                state.token = action.payload.token;
                localStorage.setItem('token', action.payload.token);
            })
            .addCase(loginMerchant.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(signUpMerchant.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(signUpMerchant.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.merchant = action.payload.merchant;
                state.token = action.payload.token;
                localStorage.setItem('token', action.payload.token);
            })
            .addCase(signUpMerchant.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(requestPasswordReset.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(requestPasswordReset.fulfilled, (state) => {
                state.loading = false;
            })
            .addCase(requestPasswordReset.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(resetPassword.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(resetPassword.fulfilled, (state) => {
                state.loading = false;
            })
            .addCase(resetPassword.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(refreshToken.fulfilled, (state, action: PayloadAction<any>) => {
                state.token = action.payload.token;
                localStorage.setItem('token', action.payload.token);
            });
    }
});

export const { logout, clearError } = merchantSlice.actions;
export default merchantSlice.reducer; 