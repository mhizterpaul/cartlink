import * as React from 'react';
import {
    Box,
    Typography,
    Container,
    Card,
    CardContent,
    CardHeader,
    Avatar,
    List,
    ListItem,
    ListItemText,
    ListItemAvatar,
    Divider,
    Button,
    Chip,
    Grid,
    useTheme,
} from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import DashboardLayout from './components/DashboardLayout';
import Inventory from './components/Inventory';
import Orders from "./components/Orders"
import Transactions from "./components/Transactions";
import Wallet from "./components/Wallet";
import Customers from "./components/Customers";
import Coupons from "./components/Coupons";
import { getDashboardStatsApi, getSalesDataApi, getTrafficDataApi } from '../../api';

const COLORS = ['#3366CC', '#00C49F', '#FFBB28', '#FF8042', '#8884d8', '#82ca9d'];

function renderDashboardContent(selected: string) {
    switch (selected) {
        case 'dashboard':
            return <DashboardContent />;
        case 'customers':
            return <Customers />;
        case 'orders':
            return <Orders />;
        case 'wallet':
            return <Wallet />;
        case 'coupons':
            return <Coupons />;
        case 'inventory':
            return <Inventory />;
        case 'transactions':
            return <Transactions />;
        default:
            return null;
    }
}

function DashboardContent() {
    const [stats, setStats] = React.useState<any>(null);
    const [salesData, setSalesData] = React.useState<any[]>([]);
    const [trafficData, setTrafficData] = React.useState<any[]>([]);
    const theme = useTheme();

    React.useEffect(() => {
        const fetchData = async () => {
            try {
                const [statsRes, salesRes, trafficRes] = await Promise.all([
                    getDashboardStatsApi(),
                    getSalesDataApi(),
                    getTrafficDataApi(),
                ]);
                setStats(statsRes.data);
                setSalesData(salesRes.data);
                setTrafficData(trafficRes.data);
            } catch (error) {
                console.error('Error fetching dashboard data:', error);
            }
        };
        fetchData();
    }, []);

    if (!stats) return null;

    return (
        <Container maxWidth="xl" disableGutters sx={{ mt: 2 }}>
            {/* Level 1: Stat Cards */}
            <Grid container spacing={2} sx={{ mb: 2 }}>
                {[
                    { label: "Today's Sale", value: `$${stats.todaySales.toFixed(2)}` },
                    { label: 'Total Sales', value: `$${stats.totalSales.toFixed(2)}` },
                    { label: 'Total Orders', value: stats.totalOrders },
                    { label: 'Total Customers', value: stats.totalCustomers },
                ].map((stat, i) => (
                    <Grid size={{ xs: 12, sm: 6, md: 3 }} key={stat.label}>
                        <Card elevation={0} sx={{ height: '100%', p: 2, border: '1px solid', borderColor: 'grey.200' }}>
                            <CardContent sx={{ p: 0 }}>
                                <Typography color="textSecondary" variant="subtitle2" sx={{ mb: 1 }}>
                                    {stat.label}
                                </Typography>
                                <Typography variant="h5" sx={{ fontWeight: 700 }}>
                                    {stat.value}
                                </Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>

            {/* Level 2: Sales Report & Traffic Sources */}
            <Grid container spacing={2} sx={{ mb: 2 }}>
                <Grid item xs={12} md={8}>
                    <Card elevation={0} sx={{ height: '100%', border: '1px solid', borderColor: 'grey.200' }}>
                        <CardHeader
                            title={<Typography variant="subtitle1" fontWeight={600}>Sales Report</Typography>}
                            action={
                                <Box sx={{ display: 'flex', gap: 1 }}>
                                    <Button size="small" variant="outlined">9 Months</Button>
                                    <Button size="small" variant="text">7 Days</Button>
                                    <Button size="small" variant="text">Export</Button>
                                </Box>
                            }
                        />
                        <Box sx={{ p: 2, minHeight: 240, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            <ResponsiveContainer width="100%" height={180}>
                                <BarChart data={salesData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                                    <XAxis dataKey="name" axisLine={false} tickLine={false} />
                                    <YAxis hide />
                                    <Tooltip />
                                    <Bar dataKey="sales" fill={theme.palette.primary.main} radius={[6, 6, 0, 0]} barSize={28} />
                                </BarChart>
                            </ResponsiveContainer>
                        </Box>
                    </Card>
                </Grid>
                <Grid item xs={12} md={4}>
                    <Card elevation={0} sx={{ height: '100%', border: '1px solid', borderColor: 'grey.200' }}>
                        <CardHeader
                            title={<Typography variant="subtitle1" fontWeight={600}>Traffic Sources</Typography>}
                            action={
                                <Button size="small" variant="text">Last 7 Days</Button>
                            }
                        />
                        <Box sx={{ p: 2, minHeight: 240, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                            <ResponsiveContainer width="100%" height={180}>
                                <PieChart>
                                    <Pie data={trafficData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={60} label>
                                        {trafficData.map((entry, index) => (
                                            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                        ))}
                                    </Pie>
                                    <Legend verticalAlign="bottom" height={36} />
                                </PieChart>
                            </ResponsiveContainer>
                        </Box>
                    </Card>
                </Grid>
            </Grid>
        </Container>
    );
}

export default function DashboardPage() {
    const [selected, setSelected] = React.useState('dashboard');
    return (
        <DashboardLayout onSelect={(name) => setSelected(name)} selected={selected}>
            {renderDashboardContent(selected)}
        </DashboardLayout>
    );
}