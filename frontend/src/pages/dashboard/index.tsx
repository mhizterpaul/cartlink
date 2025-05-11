import * as React from 'react';
import {
    Box,
    Typography,
    AppBar,
    Toolbar,
    IconButton,
    useTheme,
    useMediaQuery,
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
    InputBase,
    Grid,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import PeopleIcon from '@mui/icons-material/People';
import SettingsIcon from '@mui/icons-material/Settings';
import NotificationsIcon from '@mui/icons-material/Notifications';
import ListAltIcon from '@mui/icons-material/ListAlt';
import InventoryIcon from '@mui/icons-material/Inventory';
import NavigationDrawer from '../../components/drawer';
import SearchIcon from '@mui/icons-material/Search';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';

const drawerWidth = 240;

const navigationItems = [
    { text: 'Dashboard', icon: <DashboardIcon /> },
    { text: 'Customers', icon: <PeopleIcon /> },
    { text: 'Orders', icon: <ShoppingCartIcon /> },
    { text: 'Activity', icon: <ListAltIcon /> },
    { text: 'Inventory', icon: <InventoryIcon /> },
    { text: 'Settings', icon: <SettingsIcon /> },
];

const salesData = [
    { name: 'Feb', sales: 3000 },
    { name: 'Mar', sales: 4000 },
    { name: 'Apr', sales: 3500 },
    { name: 'May', sales: 6491 },
    { name: 'Jun', sales: 4200 },
    { name: 'Jul', sales: 3900 },
    { name: 'Aug', sales: 4100 },
    { name: 'Sep', sales: 3700 },
    { name: 'Oct', sales: 4300 },
    { name: 'Nov', sales: 3900 },
    { name: 'Dec', sales: 4500 },
];

const trafficData = [
    { name: 'Direct', value: 4000 },
    { name: 'Referral', value: 3000 },
    { name: 'Social', value: 2000 },
    { name: 'Organic', value: 2780 },
];
const COLORS = ['#3366CC', '#00C49F', '#FFBB28', '#FF8042'];

const transactions = [
    { status: 'Completed', card: 'Visa Card **** 4321', type: 'Card payment', amount: '$45.91', user: 'Louis Watson' },
    { status: 'Completed', card: 'Mastercard **** 8891', type: 'Card payment', amount: '$99.00', user: 'Nintendo' },
    { status: 'Pending', card: 'Paypal', type: 'Online', amount: '$120.00', user: 'Apple' },
    { status: 'Canceled', card: 'Amex Card **** 4444', type: 'Card payment', amount: '$19.22', user: 'Pizza Hut' },
];

const customers = [
    { name: 'Theresa Webb', email: 'theresa.webb@gmail.com', amount: '$123.45' },
    { name: 'Kristin Watson', email: 'kristin.watson@gmail.com', amount: '$98.00' },
    { name: 'Kathryn Murphy', email: 'kathryn.murphy@gmail.com', amount: '$75.50' },
    { name: 'Cody Fisher', email: 'cody.fisher@gmail.com', amount: '$60.00' },
];

export default function DashboardPage() {
    const [mobileOpen, setMobileOpen] = React.useState(false);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));

    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };

    return (
        <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'grey.50' }}>
            <AppBar
                position="fixed"
                sx={{
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` },
                    backgroundColor: theme.palette.background.paper,
                    color: theme.palette.text.primary,
                    boxShadow: 'none',
                    borderBottom: `1px solid ${theme.palette.divider}`,
                }}
            >
                <Toolbar>
                    {isMobile && (
                        <IconButton
                            color="inherit"
                            aria-label="open drawer"
                            edge="start"
                            onClick={handleDrawerToggle}
                            sx={{ mr: 2 }}
                        >
                            <MenuIcon />
                        </IconButton>
                    )}
                    <Typography variant="h6" sx={{ fontWeight: 700, mr: 3, color: 'primary.main', display: { xs: 'none', md: 'block' } }}>
                        Sasrio.
                    </Typography>
                    <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center' }}>
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                backgroundColor: theme.palette.grey[100],
                                borderRadius: 1,
                                px: 2,
                                py: 0.5,
                                width: { xs: '100%', sm: 350 },
                            }}
                        >
                            <SearchIcon sx={{ color: theme.palette.grey[500], mr: 1 }} />
                            <InputBase
                                placeholder="Search Customers, Orders, Products"
                                sx={{ flex: 1, fontSize: '0.95rem' }}
                            />
                        </Box>
                    </Box>
                    <IconButton color="inherit">
                        <NotificationsIcon />
                    </IconButton>
                    <Avatar sx={{ ml: 2, bgcolor: 'primary.main', width: 36, height: 36 }}>A</Avatar>
                </Toolbar>
            </AppBar>

            <NavigationDrawer
                items={navigationItems}
                open={isMobile ? mobileOpen : true}
                onClose={handleDrawerToggle}
            >
                <Box sx={{ px: 2, py: 3, display: { xs: 'block', md: 'none' } }}>
                    <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>Sasrio.</Typography>
                </Box>
            </NavigationDrawer>

            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: { xs: 1, sm: 2, md: 3 },
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` },
                }}
            >
                <Toolbar />
                <Container maxWidth="xl" disableGutters sx={{ mt: 2 }}>
                    {/* Level 1: Stat Cards */}
                    <Grid container spacing={2} sx={{ mb: 2 }}>
                        {[
                            { label: "Today's Sale", value: '$12,426' },
                            { label: 'Total Sales', value: '$2,38,485' },
                            { label: 'Total Orders', value: '84,382' },
                            { label: 'Total Customers', value: '33,493' },
                        ].map((stat, i) => (
                            <Grid item xs={12} sm={6} md={3} key={stat.label}>
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

                    {/* Level 3: Transactions & Recent Customers */}
                    <Grid container spacing={2}>
                        <Grid item xs={12} md={8}>
                            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200' }}>
                                <CardHeader
                                    title={<Typography variant="subtitle1" fontWeight={600}>Transactions</Typography>}
                                    action={
                                        <Button size="small" variant="text">See All Transactions</Button>
                                    }
                                />
                                <List sx={{ p: 0 }}>
                                    {transactions.map((tx, idx) => (
                                        <React.Fragment key={idx}>
                                            <ListItem sx={{ px: 3, py: 2 }}>
                                                <Chip
                                                    label={tx.status}
                                                    color={tx.status === 'Completed' ? 'success' : tx.status === 'Pending' ? 'warning' : 'error'}
                                                    size="small"
                                                    sx={{ mr: 2, minWidth: 80, borderRadius: 1, fontWeight: 500 }}
                                                />
                                                <ListItemText
                                                    primary={<Typography variant="subtitle2">{tx.card}</Typography>}
                                                    secondary={<Typography variant="body2" color="text.secondary">{tx.type}</Typography>}
                                                />
                                                <Typography variant="body2" sx={{ minWidth: 80, textAlign: 'right' }}>{tx.amount}</Typography>
                                                <Typography variant="body2" sx={{ minWidth: 120, textAlign: 'right', color: 'text.secondary' }}>{tx.user}</Typography>
                                            </ListItem>
                                            {idx !== transactions.length - 1 && <Divider />}
                                        </React.Fragment>
                                    ))}
                                </List>
                            </Card>
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200' }}>
                                <CardHeader
                                    title={<Typography variant="subtitle1" fontWeight={600}>Recent Customers</Typography>}
                                    action={
                                        <Button size="small" variant="text">See All Customers</Button>
                                    }
                                />
                                <List sx={{ p: 0 }}>
                                    {customers.map((customer, idx) => (
                                        <React.Fragment key={idx}>
                                            <ListItem sx={{ px: 3, py: 2 }}>
                                                <ListItemAvatar>
                                                    <Avatar sx={{ bgcolor: theme.palette.primary.main }}>
                                                        {customer.name[0]}
                                                    </Avatar>
                                                </ListItemAvatar>
                                                <ListItemText
                                                    primary={<Typography variant="subtitle2">{customer.name}</Typography>}
                                                    secondary={<Typography variant="body2" color="text.secondary">{customer.email}</Typography>}
                                                />
                                                <Typography variant="body2" sx={{ minWidth: 60, textAlign: 'right' }}>{customer.amount}</Typography>
                                            </ListItem>
                                            {idx !== customers.length - 1 && <Divider />}
                                        </React.Fragment>
                                    ))}
                                </List>
                            </Card>
                        </Grid>
                    </Grid>
                </Container>
            </Box>
        </Box>
    );
}