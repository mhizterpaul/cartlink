import { createSlice, createAsyncThunk, PayloadAction, AnyAction, AsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

interface AuthState {
    user: any;
    token: string | null;
    loading: boolean;
    error: any;
}

export const signUp = createAsyncThunk<any, any>(
    'auth/signUp',
    async (data: any, { rejectWithValue }: { rejectWithValue: (value: any) => any }) => {
        try {
            const response = await axios.post('/api/auth/signup', data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const login = createAsyncThunk<any, any>(
    'auth/login',
    async (data: any, { rejectWithValue }: { rejectWithValue: (value: any) => any }) => {
        try {
            const response = await axios.post('/api/auth/login', data);
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

export const refreshToken = createAsyncThunk<any, string>(
    'auth/refreshToken',
    async (token: string, { rejectWithValue }: { rejectWithValue: (value: any) => any }) => {
        try {
            const response = await axios.post('/api/auth/refresh', {}, { headers: { Authorization: token } });
            return response.data;
        } catch (err: any) {
            return rejectWithValue(err.response?.data || err.message);
        }
    }
);

const initialState: AuthState = {
    user: null,
    token: null,
    loading: false,
    error: null,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        logout(state: AuthState) {
            state.user = null;
            state.token = null;
        },
    },
    extraReducers: (builder: any) => {
        builder
            .addCase(signUp.pending, (state: AuthState) => { state.loading = true; state.error = null; })
            .addCase(signUp.fulfilled, (state: AuthState, action: PayloadAction<any>) => { state.loading = false; state.user = action.payload; state.token = action.payload.token; })
            .addCase(signUp.rejected, (state: AuthState, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(login.pending, (state: AuthState) => { state.loading = true; state.error = null; })
            .addCase(login.fulfilled, (state: AuthState, action: PayloadAction<any>) => { state.loading = false; state.user = action.payload; state.token = action.payload.token; })
            .addCase(login.rejected, (state: AuthState, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; })
            .addCase(refreshToken.pending, (state: AuthState) => { state.loading = true; state.error = null; })
            .addCase(refreshToken.fulfilled, (state: AuthState, action: PayloadAction<any>) => { state.loading = false; state.token = action.payload.token; })
            .addCase(refreshToken.rejected, (state: AuthState, action: PayloadAction<any>) => { state.loading = false; state.error = action.payload; });
    },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer; 