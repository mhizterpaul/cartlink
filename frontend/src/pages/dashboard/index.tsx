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
} from '@mui/material';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import DashboardLayout from './components/DashboardLayout';
import Inventory from './components/Inventory';
import Orders from "./components/Orders"
import Transactions from "./components/Transactions";
import Wallet from "./components/Wallet";
import Customers from "./components/Customers";
import Coupons from "./components/Coupons";
import { merchantApi } from '../../services/api';

const COLORS = ['#3366CC', '#00C49F', '#FFBB28', '#FF8042'];

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
    const [reviews, setReviews] = React.useState<any[]>([]);
    const [complaints, setComplaints] = React.useState<any[]>([]);

    React.useEffect(() => {
        const fetchData = async () => {
            try {
                const [statsRes, reviewsRes, complaintsRes] = await Promise.all([
                    merchantApi.getDashboardStats(),
                    merchantApi.getReviews(),
                    merchantApi.getComplaints()
                ]);
                setStats(statsRes.data);
                setReviews(reviewsRes.data);
                setComplaints(complaintsRes.data);
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

            {/* Level 2: Reviews & Complaints */}
            <Grid container spacing={2} sx={{ mb: 2 }}>
                <Grid size={{ xs: 12, md: 8 }}>
                    <Card elevation={0} sx={{ height: '100%', border: '1px solid', borderColor: 'grey.200' }}>
                        <CardHeader
                            title={<Typography variant="subtitle1" fontWeight={600}>Recent Reviews</Typography>}
                            action={
                                <Button size="small" variant="text">See All Reviews</Button>
                            }
                        />
                        <List sx={{ p: 0 }}>
                            {reviews.slice(0, 4).map((review, idx) => (
                                <React.Fragment key={idx}>
                                    <ListItem sx={{ px: 3, py: 2 }}>
                                        <ListItemAvatar>
                                            <Avatar sx={{ bgcolor: 'primary.main' }}>
                                                {review.customer.firstName[0]}
                                            </Avatar>
                                        </ListItemAvatar>
                                        <ListItemText
                                            primary={<Typography variant="subtitle2">{review.customer.firstName} {review.customer.lastName}</Typography>}
                                            secondary={<Typography variant="body2" color="text.secondary">{review.comment}</Typography>}
                                        />
                                        <Chip
                                            label={`${review.rating} Stars`}
                                            color="primary"
                                            size="small"
                                            sx={{ minWidth: 80, borderRadius: 1, fontWeight: 500 }}
                                        />
                                    </ListItem>
                                    {idx !== reviews.length - 1 && <Divider />}
                                </React.Fragment>
                            ))}
                        </List>
                    </Card>
                </Grid>
                <Grid size={{ xs: 12, md: 4 }}>
                    <Card elevation={0} sx={{ height: '100%', border: '1px solid', borderColor: 'grey.200' }}>
                        <CardHeader
                            title={<Typography variant="subtitle1" fontWeight={600}>Recent Complaints</Typography>}
                            action={
                                <Button size="small" variant="text">See All Complaints</Button>
                            }
                        />
                        <List sx={{ p: 0 }}>
                            {complaints.slice(0, 4).map((complaint, idx) => (
                                <React.Fragment key={idx}>
                                    <ListItem sx={{ px: 3, py: 2 }}>
                                        <ListItemText
                                            primary={<Typography variant="subtitle2">{complaint.title}</Typography>}
                                            secondary={<Typography variant="body2" color="text.secondary">{complaint.description}</Typography>}
                                        />
                                        <Chip
                                            label={complaint.status}
                                            color={complaint.status === 'RESOLVED' ? 'success' : complaint.status === 'IN_PROGRESS' ? 'warning' : 'error'}
                                            size="small"
                                            sx={{ minWidth: 80, borderRadius: 1, fontWeight: 500 }}
                                        />
                                    </ListItem>
                                    {idx !== complaints.length - 1 && <Divider />}
                                </React.Fragment>
                            ))}
                        </List>
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