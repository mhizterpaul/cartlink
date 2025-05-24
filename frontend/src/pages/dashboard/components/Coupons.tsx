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
    Chip,
    IconButton,
    TextField,
    Grid,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
} from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import SearchIcon from '@mui/icons-material/Search';

const coupons = [
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
    {
        name: 'Test Free delivery',
        code: 'DEYKXJX5',
        price: '5 EUR',
        activeFrom: '2023-06-12',
        activeTo: '2023-06-19',
        limit: 40,
        used: 0,
    },
];

export default function Coupons() {
    return (
        <Box sx={{ p: { xs: 0, md: 2 } }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                <Typography variant="h5" fontWeight={700}>Coupon Management</Typography>
                <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
                    <Button variant="outlined">Export CSV</Button>
                    <Button variant="outlined">Import CSV</Button>
                    <Chip label="44/50" color="secondary" size="small" sx={{ fontWeight: 600 }} />
                    <Typography variant="body2" color="text.secondary">Coupons Remaining (updated 3m ago)</Typography>
                    <SearchIcon color="action" />
                    <TextField size="small" placeholder="Search by name" sx={{ minWidth: 200 }} />
                </Box>
            </Box>
            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'success.main', mb: 2 }}>
                <CardContent sx={{ pb: 0 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                        <Button variant="outlined">Set Default</Button>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                            <Button variant="outlined">Undo</Button>
                            <Button variant="contained" color="success" sx={{ fontWeight: 600 }}>Create Coupons</Button>
                        </Box>
                    </Box>
                    <Grid container spacing={2} alignItems="center">
                        <Grid item xs={12} md={2}>
                            <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>Limits</Typography>
                            <TextField fullWidth size="small" placeholder="##" />
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>End time</Typography>
                            <TextField fullWidth size="small" placeholder="01/03/2018 📅" />
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>Start time</Typography>
                            <TextField fullWidth size="small" placeholder="01/03/2018 📅" />
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>Discount precent</Typography>
                            <FormControl fullWidth size="small">
                                <InputLabel>Discount precent</InputLabel>
                                <Select label="Discount precent" defaultValue="Percent">
                                    <MenuItem value="Percent">Percent</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>Deal Type</Typography>
                            <FormControl fullWidth size="small">
                                <InputLabel>Deal Type</InputLabel>
                                <Select label="Deal Type" defaultValue="Discount">
                                    <MenuItem value="Discount">Discount</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={2}>
                            <Typography variant="subtitle2" color="text.secondary" fontWeight={600}>Coupon Type</Typography>
                            <FormControl fullWidth size="small">
                                <InputLabel>Coupon Type</InputLabel>
                                <Select label="Coupon Type" defaultValue="string">
                                    <MenuItem value="string">string</MenuItem>
                                </Select>
                            </FormControl>
                        </Grid>
                    </Grid>
                </CardContent>
            </Card>

            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200' }}>
                <CardContent sx={{ pb: 0 }}>
                    <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: 'none', border: '1px solid', borderColor: 'grey.200' }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Name</TableCell>
                                    <TableCell>Code</TableCell>
                                    <TableCell>Price</TableCell>
                                    <TableCell>Active from</TableCell>
                                    <TableCell>Active to</TableCell>
                                    <TableCell>Limit number</TableCell>
                                    <TableCell>Used from</TableCell>
                                    <TableCell align="center">Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {coupons.map((coupon, idx) => (
                                    <TableRow key={idx}>
                                        <TableCell>{coupon.name}</TableCell>
                                        <TableCell>
                                            <Chip label={coupon.code} size="small" color="success" sx={{ fontWeight: 600 }} />
                                        </TableCell>
                                        <TableCell>{coupon.price}</TableCell>
                                        <TableCell>{coupon.activeFrom}</TableCell>
                                        <TableCell>{coupon.activeTo}</TableCell>
                                        <TableCell>{coupon.limit}</TableCell>
                                        <TableCell>{coupon.used}</TableCell>
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
                </CardContent>
            </Card>
        </Box>
    );
} 