import { Navigate, Outlet } from 'react-router';
import { useSelector } from 'react-redux';
import type { RootState } from '../../store';

const CustomerAuth = () => {
    const { merchant, token } = useSelector((state: RootState) => state.merchant);

    // If user is logged in as a merchant, redirect to dashboard
    if (merchant && token) {
        return <Navigate to="/dashboard" replace />;
    }

    // If not logged in as merchant, allow access to customer routes
    return <Outlet />;
};

export default CustomerAuth; 