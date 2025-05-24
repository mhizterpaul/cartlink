import React from 'react';
import {
    Box,
    Typography,
    Button,
    Card,
    CardContent,
    Grid,
    Chip,
    Table,
    TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    Paper,
    TextField,
    MenuItem,
    Select,
    FormControl,
    InputLabel,
    Link,
} from '@mui/material';
import DescriptionOutlinedIcon from '@mui/icons-material/DescriptionOutlined';
import FileDownloadOutlinedIcon from '@mui/icons-material/FileDownloadOutlined';

const summary = [
    {
        label: 'Previous payout',
        date: 'Apr 2, 2021',
        amount: '$4,962.34',
        status: 'Paid',
        statusColor: 'success',
        link: 'View transactions',
    },
    {
        label: 'Next payout',
        date: 'Apr 3, 2021',
        amount: '$2,804.80',
        status: 'Pending',
        statusColor: 'warning',
        link: 'View transactions',
    },
    {
        label: 'Balance',
        date: 'Est. by Apr 15, 2021',
        amount: '$5,467.70',
        status: '',
        statusColor: 'default',
        link: 'View transactions',
    },
];

const payouts = [
    {
        date: 'Apr 3, 2021',
        status: 'Pending',
        charges: '$2,851.99',
        refunds: '$0.00',
        fees: '-$145.156',
        total: '$2,804.80',
    },
    {
        date: 'Apr 2, 2021',
        status: 'Paid',
        charges: '$5,872.87',
        refunds: '-$734.35',
        fees: '$0.00',
        total: '$4,962.34',
    },
    {
        date: 'Apr 1, 2021',
        status: 'Paid',
        charges: '$9,225.35',
        refunds: '-$199.20',
        fees: '$0.00',
        total: '$8,749.39',
    },
    {
        date: 'Mar 31, 2021',
        status: 'Paid',
        charges: '$3,644.86',
        refunds: '-$460.89',
        fees: '$0.00',
        total: '$3,074.62',
    },
];

const statusColor = (status: string) => {
    switch (status) {
        case 'Paid': return 'success';
        case 'Pending': return 'warning';
        default: return 'default';
    }
};

export default function Wallet() {
    return (
        <Box sx={{ p: { xs: 0, md: 2 } }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5" fontWeight={700}>Payouts</Typography>
                <Box sx={{ display: 'flex', gap: 1 }}>
                    <Button variant="outlined" startIcon={<DescriptionOutlinedIcon />} sx={{ borderRadius: 2, fontWeight: 600 }}>Documents</Button>
                    <Button variant="outlined" startIcon={<FileDownloadOutlinedIcon />} sx={{ borderRadius: 2, fontWeight: 600 }}>Export</Button>
                    <Button variant="contained" color="primary" sx={{ borderRadius: 2, fontWeight: 600, ml: 2 }}>Transactions</Button>
                </Box>
            </Box>
            <Grid container spacing={2} sx={{ mb: 2 }}>
                {summary.map((item, idx) => (
                    <Grid item xs={12} md={4} key={item.label}>
                        <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200', height: '100%' }}>
                            <CardContent>
                                <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
                                    <Typography color="text.secondary" fontWeight={500}>{item.label}</Typography>
                                    <Typography color="text.secondary" fontSize={13}>{item.date}</Typography>
                                </Box>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                    <Typography variant="h5" fontWeight={700}>{item.amount}</Typography>
                                    {item.status && (
                                        <Chip label={item.status} color={item.statusColor as any} size="small" sx={{ fontWeight: 500 }} />
                                    )}
                                </Box>
                                <Link href="#" underline="hover" fontWeight={600} fontSize={14}>{item.link}</Link>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200' }}>
                <CardContent sx={{ pb: 0 }}>
                    <Typography variant="subtitle1" fontWeight={600} sx={{ mb: 2 }}>All Payouts</Typography>
                    <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
                        <Grid item xs={12} md={3}>
                            <FormControl size="small" fullWidth>
                                <InputLabel>Filter payouts</InputLabel>
                                <Select label="Filter payouts" defaultValue="All">
                                    <MenuItem value="All">All</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={9}>
                            <TextField fullWidth size="small" placeholder="Search" />
                        </Grid>
                    </Grid>
                    <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: 'none', border: '1px solid', borderColor: 'grey.200' }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Date</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell>Charges</TableCell>
                                    <TableCell>Refunds</TableCell>
                                    <TableCell>Fees</TableCell>
                                    <TableCell>Total</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {payouts.map((row, idx) => (
                                    <TableRow key={idx}>
                                        <TableCell>
                                            <Link href="#" underline="hover" fontWeight={600} color="primary.main">{row.date}</Link>
                                        </TableCell>
                                        <TableCell>
                                            <Chip label={row.status} color={statusColor(row.status)} size="small" />
                                        </TableCell>
                                        <TableCell>{row.charges}</TableCell>
                                        <TableCell sx={{ color: row.refunds.startsWith('-') ? 'error.main' : undefined }}>{row.refunds}</TableCell>
                                        <TableCell sx={{ color: row.fees.startsWith('-') ? 'error.main' : undefined }}>{row.fees}</TableCell>
                                        <TableCell>{row.total}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </CardContent>
            </Card>
        </Box>
    );
} 