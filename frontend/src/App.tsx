import { BrowserRouter, Navigate, Routes, Route } from "react-router";
import './App.css';
import Homepage from './pages/index';
import NotFound from './pages/NotFound';
import Signup from "./pages/signup"
import Signin from "./pages/signin"
import Dashboard from "./pages/dashboard";
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import { Provider as ReduxProvider } from "react-redux"
import Products from "./pages/products"
import Product from "./pages/product"
import Cart from "./pages/cart"
import Checkout from "./pages/checkout"
import Reviews from "./pages/reviews"
import Payment from "./pages/payment"
import Settings from "./pages/settings"
import { StyledEngineProvider, ThemeProvider, createTheme } from '@mui/material/styles';
import { green, purple } from '@mui/material/colors';
import store from "./store";
import MerchantAuth from './components/auth/MerchantAuth';
import CustomerAuth from './components/auth/CustomerAuth';

const theme = createTheme({
  palette: {
    primary: {
      main: purple[500],
    },
    secondary: {
      main: green[500],
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <StyledEngineProvider injectFirst>
        <BrowserRouter>
          <ReduxProvider store={store}>
            <Routes>
              <Route path="/" element={<Homepage />} />
              <Route path="/404" element={<NotFound />} />
              <Route element={<MerchantAuth />}>
                <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/settings" element={<Settings />} />
              </Route>
              <Route path="/products?merchantId=default" element={<Products />} />
              <Route path="/product?productId=defualt" element={<Product />} />
              <Route path="/reviews?productId=default" element={<Reviews />} />
              <Route element={<CustomerAuth />}>
                <Route path="/cart" element={<Cart />} />
                <Route path="/checkout" element={<Checkout />} />
                <Route path="/payment" element={<Payment />} />
              </Route>
              <Route path="/signup" element={<Signup />} />
              <Route path="/login" element={<Signin />} />
              <Route path="*" element={<Navigate to="/404" />} />
            </Routes>
          </ReduxProvider>
        </BrowserRouter>
      </StyledEngineProvider>
    </ThemeProvider>
  );
}

export default App;
