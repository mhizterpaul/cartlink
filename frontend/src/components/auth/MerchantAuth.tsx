import { Navigate, Outlet } from 'react-router';
import { useSelector } from 'react-redux';
import { type RootState } from '../../store';

const MerchantAuth = () => {
    const { merchant, token } = useSelector((state: RootState) => state.merchant);

    // Check if user is authenticated as a merchant
    if (!merchant || !token) {
        // Redirect to login page if not authenticated
        return <Navigate to="/login" replace />;
    }

    // If authenticated, render the child routes
    return <Outlet />;
};

export default MerchantAuth; 