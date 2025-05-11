import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import merchantReducer from './slices/merchantSlice';
import cartReducer from './slices/cartSlice';
import refundReducer from './slices/refundSlice';
import complaintReducer from './slices/complaintSlice';
import orderReducer from './slices/orderSlice';
import productLinkReducer from './slices/productLinkSlice';
import productReducer from './slices/productSlice';

const store = configureStore({
    reducer: {
        auth: authReducer,
        merchant: merchantReducer,
        cart: cartReducer,
        refund: refundReducer,
        complaint: complaintReducer,
        order: orderReducer,
        productLink: productLinkReducer,
        product: productReducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export default store; 