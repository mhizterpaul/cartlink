import React from 'react';
import {
    Box,
    Typography,
    Button,
    Card,
    CardContent,
    CardHeader,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    Avatar,
    Chip,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    TextField,
    Grid,
    IconButton,
    Pagination,
} from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import PrintIcon from '@mui/icons-material/Print';
import DownloadIcon from '@mui/icons-material/Download';

const purchases = [
    {
        product: { name: 'Nike Downshifter 12', price: 'Rp 648.000', image: '/images/shoe1.png' },
        customer: 'Justin ElNino',
        location: 'San Francisco, USA',
        amount: 'Rp 1,336,000',
        purchaseDate: '12 Sep 2023, 18:47',
        status: 'Completed',
    },
    {
        product: { name: 'Compass Retrogade High', price: 'Rp 468.000', image: '/images/shoe2.png' },
        customer: 'Jonathan Blsep',
        location: 'New York, USA',
        amount: 'Rp 668.000',
        purchaseDate: '12 Sep 2023, 16:41',
        status: 'In Progress',
    },
    {
        product: { name: 'Adidas Superstar XLG Green', price: 'Rp 2.000.000', image: '/images/shoe3.png' },
        customer: 'Ron Sebastian',
        location: 'London, UK',
        amount: 'Rp 4,000,000',
        purchaseDate: '12 Sep 2023, 13:18',
        status: 'Completed',
    },
    {
        product: { name: 'Vans Old Skool Shoe', price: 'Rp 1.100.000', image: '/images/shoe4.png' },
        customer: 'Natashia Fransisca',
        location: 'Amsterdam, Netherland',
        amount: 'Rp 1,100,000',
        purchaseDate: '11 Sep 2023, 11:09',
        status: 'Cancelled',
    },
    {
        product: { name: 'Nike Air Max 90', price: 'Rp 1.799.000', image: '/images/shoe5.png' },
        customer: 'Dionata Levano',
        location: 'Jakarta, Indonesia',
        amount: 'Rp 3,598,000',
        purchaseDate: '12 Sep 2023, 15:35',
        status: 'Completed',
    },
    {
        product: { name: 'Nike Air Max Pulse', price: 'Rp 2.379.000', image: '/images/shoe6.png' },
        customer: 'Hinata Shoyo',
        location: 'Tokyo, Japan',
        amount: 'Rp 7,137,000',
        purchaseDate: '12 Sep 2023, 16:41',
        status: 'Completed',
    },
];

const statusColor = (status: string) => {
    switch (status) {
        case 'Completed': return 'success';
        case 'In Progress': return 'warning';
        case 'Cancelled': return 'error';
        default: return 'default';
    }
};

export default function Transactions() {
    return (
        <Box sx={{ p: { xs: 0, md: 2 } }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5" fontWeight={700}>Purchases</Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button variant="outlined" startIcon={<PrintIcon />} sx={{ borderRadius: 2, fontWeight: 600 }}>Print</Button>
                    <Button variant="outlined" startIcon={<DownloadIcon />} sx={{ borderRadius: 2, fontWeight: 600 }}>Download Report</Button>
                </Box>
            </Box>
            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200', mb: 2 }}>
                <CardContent sx={{ pb: 0 }}>
                    <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
                        <Grid item xs={12} md={2}>
                            <FormControl size="small" fullWidth>
                                <InputLabel>Week</InputLabel>
                                <Select label="Week" defaultValue="Week">
                                    <MenuItem value="Week">Week</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <FormControl size="small" fullWidth>
                                <InputLabel>Date</InputLabel>
                                <Select label="Date" defaultValue="2023-42nd">
                                    <MenuItem value="2023-42nd">2023-42nd</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <FormControl size="small" fullWidth>
                                <InputLabel>Status</InputLabel>
                                <Select label="Status" defaultValue="Status">
                                    <MenuItem value="Status">Status</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <TextField fullWidth size="small" placeholder="Search..." />
                        </Grid>
                    </Grid>
                    <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: 'none', border: '1px solid', borderColor: 'grey.200' }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Product</TableCell>
                                    <TableCell>Customer</TableCell>
                                    <TableCell>Location</TableCell>
                                    <TableCell>Payment Amount</TableCell>
                                    <TableCell>Purchase Date</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell align="center">Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {purchases.map((row, idx) => (
                                    <TableRow key={idx}>
                                        <TableCell>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                                <Avatar src={row.product.image} alt={row.product.name} sx={{ width: 40, height: 40 }} />
                                                <Box>
                                                    <Typography fontWeight={600}>{row.product.name}</Typography>
                                                    <Typography variant="caption" color="text.secondary">{row.product.price}</Typography>
                                                </Box>
                                            </Box>
                                        </TableCell>
                                        <TableCell>{row.customer}</TableCell>
                                        <TableCell>{row.location}</TableCell>
                                        <TableCell>{row.amount}</TableCell>
                                        <TableCell>{row.purchaseDate}</TableCell>
                                        <TableCell>
                                            <Chip label={row.status} color={statusColor(row.status)} size="small" />
                                        </TableCell>
                                        <TableCell align="center">
                                            <IconButton size="small">
                                                <MoreVertIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2 }}>
                        <Typography variant="body2" color="text.secondary">Showing 1 to 10 of 20 results</Typography>
                        <Pagination count={2} page={1} color="primary" />
                    </Box>
                </CardContent>
            </Card>
        </Box>
    );
} 