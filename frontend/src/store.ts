import { configureStore } from '@reduxjs/toolkit';
import merchantReducer from './slices/merchantSlice';
import cartReducer from './slices/cartSlice';
import refundReducer from './slices/refundSlice';
import complaintReducer from './slices/complaintSlice';
import orderReducer from './slices/orderSlice';
import productLinkReducer from './slices/productLinkSlice';
import productReducer from './slices/productSlice';
import dashboardReducer from './slices/dashboardSlice';

const store = configureStore({
    reducer: {
        merchant: merchantReducer,
        cart: cartReducer,
        refund: refundReducer,
        complaint: complaintReducer,
        order: orderReducer,
        productLink: productLinkReducer,
        product: productReducer,
        dashboard: dashboardReducer,
    },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
export default store; 