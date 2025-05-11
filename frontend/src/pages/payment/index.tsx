import React, { useState } from 'react';
import {
    Box,
    Button,
    Typography,
    TextField,
    Paper,
    Checkbox,
    FormControlLabel,
    InputAdornment,
    Divider,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';

const orderItems = [
    { name: 'Random T-Shirt', price: 30 },
    { name: 'Some boots', price: 20 },
    { name: 'Other overpriced something', price: 10 },
];
const tax = 5.28;
const total = 65.28;

const PaymentPage = () => {
    const [form, setForm] = useState({
        cardNumber: '7589 8439 0329 9821',
        cvc: '478',
        name: 'ONDREJ VANCO',
        exp: '',
        remember: false,
    });

    const handleFormChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    return (
        <Box sx={{ minHeight: '100vh', background: 'linear-gradient(135deg, #7b2ff2 0%, #f357a8 100%)', display: 'flex', alignItems: 'center', justifyContent: 'center', py: 6 }}>
            <Paper sx={{ display: 'flex', borderRadius: 4, overflow: 'hidden', minWidth: 600, maxWidth: 700, width: '100%', boxShadow: 6 }}>
                {/* Left: Order Summary */}
                <Box sx={{ background: '#2d0a4b', color: '#fff', p: 4, width: 260, display: 'flex', flexDirection: 'column', alignItems: 'flex-start', minHeight: 420 }}>
                    <ShoppingCartIcon sx={{ fontSize: 32, mb: 2 }} />
                    <Typography sx={{ fontWeight: 700, mb: 2, letterSpacing: 1 }}>YOUR ORDER:</Typography>
                    {orderItems.map((item) => (
                        <Box key={item.name} sx={{ display: 'flex', justifyContent: 'space-between', width: '100%', mb: 1 }}>
                            <Typography>{item.name}</Typography>
                            <Typography>${item.price}</Typography>
                        </Box>
                    ))}
                    <Divider sx={{ background: 'rgba(255,255,255,0.2)', width: '100%', my: 2 }} />
                    <Typography sx={{ color: '#e0c3fc', fontSize: 15, mb: 0.5 }}>TAX: ${tax.toFixed(2)}</Typography>
                    <Typography sx={{ fontWeight: 700, fontSize: 17 }}>TOTAL: ${total.toFixed(2)}</Typography>
                </Box>
                {/* Right: Payment Form */}
                <Box sx={{ flex: 1, background: '#fff', p: 4, display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={8}>
                            <Typography sx={{ fontWeight: 700, mb: 1 }}>CARD NUMBER</Typography>
                            <TextField
                                fullWidth
                                name="cardNumber"
                                value={form.cardNumber}
                                onChange={handleFormChange}
                                sx={{ mb: 2 }}
                                inputProps={{ maxLength: 19, style: { fontWeight: 600, fontSize: 18, letterSpacing: 2 } }}
                            />
                        </Grid>
                        <Grid item xs={12} sm={4}>
                            <Typography sx={{ fontWeight: 700, mb: 1, display: 'flex', alignItems: 'center' }}>
                                CVC
                                <HelpOutlineIcon sx={{ fontSize: 16, color: '#bdbdbd', ml: 0.5 }} />
                            </Typography>
                            <TextField
                                fullWidth
                                name="cvc"
                                value={form.cvc}
                                onChange={handleFormChange}
                                sx={{ mb: 2 }}
                                inputProps={{ maxLength: 4, style: { fontWeight: 600, fontSize: 18, letterSpacing: 2 } }}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Typography sx={{ fontWeight: 700, mb: 1 }}>NAME AS STATED ON THE CARD</Typography>
                            <TextField
                                fullWidth
                                name="name"
                                value={form.name}
                                onChange={handleFormChange}
                                sx={{ mb: 2 }}
                                inputProps={{ style: { fontWeight: 600, fontSize: 16, letterSpacing: 1 } }}
                            />
                        </Grid>
                        <Grid item xs={12} sm={6}>
                            <Typography sx={{ fontWeight: 700, mb: 1 }}>EXPIRATION DATE</Typography>
                            <TextField
                                fullWidth
                                name="exp"
                                value={form.exp}
                                onChange={handleFormChange}
                                placeholder="00 / 00"
                                sx={{ mb: 2 }}
                                inputProps={{ maxLength: 7, style: { fontWeight: 600, fontSize: 16, letterSpacing: 2 } }}
                            />
                        </Grid>
                    </Grid>
                    <FormControlLabel
                        control={<Checkbox name="remember" checked={form.remember} onChange={handleFormChange} />}
                        label={<Typography>Remember thic card</Typography>}
                        sx={{ mt: 1, mb: 3 }}
                    />
                    <Button
                        fullWidth
                        variant="contained"
                        sx={{
                            background: '#2ecc71',
                            color: '#fff',
                            fontWeight: 700,
                            fontSize: 18,
                            borderRadius: 2,
                            py: 1.5,
                            textTransform: 'none',
                            boxShadow: 0,
                            '&:hover': { background: '#27ae60' },
                        }}
                    >
                        PAY ${total.toFixed(2)}
                    </Button>
                </Box>
            </Paper>
        </Box>
    );
};

import { Grid } from '@mui/material';
export default PaymentPage; 