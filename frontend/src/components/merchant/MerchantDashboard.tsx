import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchDashboardStats, fetchSalesData, fetchTrafficData } from '../../slices/dashboardSlice';
import type { RootState, AppDispatch } from '../../store';

const MerchantDashboard: React.FC = () => {
    const dispatch = useDispatch<AppDispatch>();
    const { stats, salesData, trafficData, loading, error } = useSelector((state: RootState) => state.dashboard);

    useEffect(() => {
        dispatch(fetchDashboardStats());
        dispatch(fetchSalesData());
        dispatch(fetchTrafficData());
    }, [dispatch]);

    if (loading) return <div>Loading...</div>;
    if (error) return <div>Error: {error}</div>;

    return (
        <div>
            <h1>Merchant Dashboard</h1>
            <div>
                <h2>Stats</h2>
                <pre>{JSON.stringify(stats, null, 2)}</pre>
            </div>
            <div>
                <h2>Sales Data</h2>
                <pre>{JSON.stringify(salesData, null, 2)}</pre>
            </div>
            <div>
                <h2>Traffic Data</h2>
                <pre>{JSON.stringify(trafficData, null, 2)}</pre>
            </div>
        </div>
    );
};

export default MerchantDashboard; 