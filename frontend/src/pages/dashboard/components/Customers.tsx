import React from 'react';
import {
    Box,
    Typography,
    Button,
    Card,
    CardContent,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    TextField,
    Grid,
    Pagination,
    Avatar,
    Tabs,
    Tab,
    Checkbox,
    IconButton,
} from '@mui/material';
import FilterListIcon from '@mui/icons-material/FilterList';
import FileDownloadOutlinedIcon from '@mui/icons-material/FileDownloadOutlined';
import KeyboardArrowDownIcon from '@mui/icons-material/KeyboardArrowDown';

interface Customer {
    id: number;
    name: string;
    email: string;
    location: string;
    orders: number;
    spent: string;
    avatar: string;
}

const customers: Customer[] = [
    { id: 1, name: 'Ramisa Sanjana', email: 'ramisa@gmail.com', location: '14 Clifton Down Road, UK', orders: 7, spent: '$3331.00', avatar: '/images/avatar1.png' },
    { id: 2, name: 'Mohua Amin', email: 'mohua@gmail.com', location: '405 Kings Road, Chelsea, London', orders: 44, spent: '$74,331.00', avatar: '/images/avatar2.png' },
    { id: 3, name: 'Estiaq Noor', email: 'estiaqnoor@gmail.com', location: '176 Finchley Road, London', orders: 4, spent: '$2331.00', avatar: '/images/avatar3.png' },
    { id: 4, name: 'Reaz Nahid', email: 'reaz@hotmail.com', location: '12 South Bridge, Edinburgh, UK', orders: 27, spent: '$44,131.89', avatar: '/images/avatar4.png' },
    { id: 5, name: 'Rabbi Amin', email: 'amin@yourmail.com', location: '176 Finchley Road, London', orders: 16, spent: '$7331.00', avatar: '/images/avatar5.png' },
    { id: 6, name: 'Sakib Al Baky', email: 'sakib@yahoo.com', location: '405 Kings Road, Chelsea, London', orders: 47, spent: '$6231.00', avatar: '/images/avatar6.png' },
    { id: 7, name: 'Maria Nur', email: 'maria@gmail.com', location: '80 High Street, Winchester', orders: 12, spent: '$9631.00', avatar: '/images/avatar7.png' },
    { id: 8, name: 'Ahmed Baky', email: 'maria@gmail.com', location: '80 High Street, Winchester', orders: 12, spent: '$9631.00', avatar: '/images/avatar8.png' },
];

export default function Customers() {
    const [selectedTab, setSelectedTab] = React.useState(0);

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setSelectedTab(newValue);
    };

    return (
        <Box sx={{ p: { xs: 0, md: 2 } }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5" fontWeight={700}>Customers</Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button variant="outlined" startIcon={<FileDownloadOutlinedIcon />} sx={{ borderRadius: 2, fontWeight: 600 }}>Export</Button>
                    <Button variant="contained" color="primary" sx={{ borderRadius: 2, fontWeight: 600 }}>+ Add Customers</Button>
                </Box>
            </Box>
            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200', mb: 2 }}>
                <CardContent sx={{ pb: 0 }}>
                    <Tabs value={selectedTab} onChange={handleTabChange} sx={{ mb: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
                        <Tab label="All Customers" />
                        <Tab label="New Customers" />
                        <Tab label="From Europe" />
                        <Tab label="Asia" />
                        <Tab label="Others" />
                    </Tabs>

                    <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
                        <Grid item xs={12} md={3}>
                            <Button variant="outlined" startIcon={<FilterListIcon />} sx={{ borderRadius: 2, fontWeight: 600, height: '100%' }}>Filter</Button>
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <TextField fullWidth size="small" placeholder="Search customer..." />
                        </Grid>
                        <Grid item xs={12} md={3}>
                            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
                                <IconButton size="small">
                                    <KeyboardArrowDownIcon />
                                </IconButton>
                                <IconButton size="small">
                                    <KeyboardArrowDownIcon />
                                </IconButton>
                                <IconButton size="small">
                                    <KeyboardArrowDownIcon />
                                </IconButton>
                            </Box>
                        </Grid>
                    </Grid>

                    <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: 'none', border: '1px solid', borderColor: 'grey.200' }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell padding="checkbox">
                                        <Checkbox size="small" />
                                    </TableCell>
                                    <TableCell>Customer</TableCell>
                                    <TableCell>Email</TableCell>
                                    <TableCell>Location</TableCell>
                                    <TableCell>Orders</TableCell>
                                    <TableCell>Spent</TableCell>
                                    <TableCell>Action</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {customers.map((customer) => (
                                    <TableRow key={customer.id}>
                                        <TableCell padding="checkbox">
                                            <Checkbox size="small" />
                                        </TableCell>
                                        <TableCell>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                                <Avatar src={customer.avatar} alt={customer.name} sx={{ width: 40, height: 40 }} />
                                                <Typography fontWeight={600}>{customer.name}</Typography>
                                            </Box>
                                        </TableCell>
                                        <TableCell>{customer.email}</TableCell>
                                        <TableCell>{customer.location}</TableCell>
                                        <TableCell>{customer.orders}</TableCell>
                                        <TableCell>{customer.spent}</TableCell>
                                        <TableCell>
                                            <IconButton size="small">
                                                <KeyboardArrowDownIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', mt: 2 }}>
                        <Typography variant="body2" color="text.secondary" sx={{ mr: 2 }}>Showing 1 to 10 of 24 results</Typography>
                        <Pagination count={3} page={1} color="primary" />
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
} 