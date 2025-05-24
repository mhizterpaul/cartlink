import { createSlice, createAsyncThunk, type PayloadAction } from '@reduxjs/toolkit';
import { merchantSignUpApi, merchantLoginApi, merchantPasswordResetRequestApi, merchantPasswordResetApi, merchantRefreshTokenApi } from '../api';

interface MerchantState {
    merchant: any;
    token: string | null;
    loading: boolean;
    error: any;
}

export const merchantSignUp = createAsyncThunk<any, any>(
    'merchant/signUp',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await merchantSignUpApi(data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const merchantLogin = createAsyncThunk<any, any>(
    'merchant/login',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await merchantLoginApi(data);
            return response;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const merchantRefreshToken = createAsyncThunk<any, void>(
    'merchant/refreshToken',
    async (_, { getState, rejectWithValue }) => {
        try {
            const state = getState() as { merchant: MerchantState };
            const token = state.merchant.token;
            if (!token) {
                return rejectWithValue('No token available');
            }
            const response = await merchantRefreshTokenApi(token);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const merchantPasswordResetRequest = createAsyncThunk<any, any>(
    'merchant/passwordResetRequest',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await merchantPasswordResetRequestApi(data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const merchantPasswordReset = createAsyncThunk<any, any>(
    'merchant/passwordReset',
    async (data: any, { rejectWithValue }) => {
        try {
            const response = await merchantPasswordResetApi(data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: MerchantState = {
    merchant: null,
    token: null,
    loading: false,
    error: null,
};

const merchantSlice = createSlice({
    name: 'merchant',
    initialState,
    reducers: {
        logout: (state) => {
            state.merchant = null;
            state.token = null;
            state.error = null;
        },
    },
    extraReducers: (builder) => {
        builder
            .addCase(merchantSignUp.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(merchantSignUp.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.merchant = action.payload;
                state.token = action.payload.token;
            })
            .addCase(merchantSignUp.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(merchantLogin.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(merchantLogin.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.merchant = action.payload;
                state.token = action.payload.token;
            })
            .addCase(merchantLogin.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(merchantRefreshToken.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(merchantRefreshToken.fulfilled, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.token = action.payload.token;
            })
            .addCase(merchantRefreshToken.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(merchantPasswordResetRequest.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(merchantPasswordResetRequest.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(merchantPasswordResetRequest.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            })
            .addCase(merchantPasswordReset.pending, (state) => { state.loading = true; state.error = null; })
            .addCase(merchantPasswordReset.fulfilled, (state, action: PayloadAction<any>) => { state.loading = false; })
            .addCase(merchantPasswordReset.rejected, (state, action: PayloadAction<any>) => {
                state.loading = false;
                state.error = action.payload;
            });
    },
});

export const { logout } = merchantSlice.actions;
export default merchantSlice.reducer; 