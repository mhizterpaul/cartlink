import React, { useState } from 'react';
import {
    Box,
    Button,
    Typography,
    TextField,
    Paper,
    Divider,
    Grid,
    ToggleButton,
    ToggleButtonGroup,
    Checkbox,
    FormControlLabel,
    InputAdornment,
    MenuItem,
} from '@mui/material';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

const cartItems = [
    {
        id: 1,
        name: 'DuoComfort Sofa Premium',
        price: 20.0,
        count: 1,
        image: 'https://images.unsplash.com/photo-1519710164239-da123dc03ef4?auto=format&fit=crop&w=80&q=80',
    },
    {
        id: 2,
        name: 'IronOne Desk',
        price: 25.0,
        count: 1,
        image: 'https://images.unsplash.com/photo-1515378791036-0648a3ef77b2?auto=format&fit=crop&w=80&q=80',
    },
];

const countries = [
    { code: 'US', label: 'United States' },
    { code: 'CA', label: 'Canada' },
    { code: 'GB', label: 'United Kingdom' },
    { code: 'NG', label: 'Nigeria' },
];

const states = [
    'Alabama', 'Alaska', 'Arizona', 'Arkansas', 'California', 'Colorado', 'Connecticut', 'Delaware', 'Florida', 'Georgia',
    'Hawaii', 'Idaho', 'Illinois', 'Indiana', 'Iowa', 'Kansas', 'Kentucky', 'Louisiana', 'Maine', 'Maryland', 'Massachusetts',
    'Michigan', 'Minnesota', 'Mississippi', 'Missouri', 'Montana', 'Nebraska', 'Nevada', 'New Hampshire', 'New Jersey',
    'New Mexico', 'New York', 'North Carolina', 'North Dakota', 'Ohio', 'Oklahoma', 'Oregon', 'Pennsylvania', 'Rhode Island',
    'South Carolina', 'South Dakota', 'Tennessee', 'Texas', 'Utah', 'Vermont', 'Virginia', 'Washington', 'West Virginia',
    'Wisconsin', 'Wyoming',
];

const subtotal = 45.0;
const shipping = 5.0;
const discount = 10.0;
const total = subtotal + shipping - discount;

const CheckoutPage = () => {
    const [shippingType, setShippingType] = useState<'delivery' | 'pickup'>('delivery');
    const [form, setForm] = useState({
        name: '',
        email: '',
        phone: '',
        country: '',
        city: '',
        state: '',
        zip: '',
        terms: false,
        discount: '',
    });

    const handleFormChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value, type, checked } = e.target;
        setForm((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
    };

    return (
        <Box sx={{ minHeight: '100vh', background: '#f5f7fa', py: 4 }}>
            <Box sx={{ maxWidth: 1100, mx: 'auto', background: '#fff', borderRadius: 3, boxShadow: 1, p: 4 }}>
                {/* Header */}
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 4 }}>
                    <Typography variant="h5" sx={{ color: '#2563eb', fontWeight: 800, letterSpacing: 1, mr: 4 }}>
                        FURNEST
                    </Typography>
                    <Box sx={{ flex: 1 }} />
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 3 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <CheckCircleIcon sx={{ color: '#2563eb', fontSize: 22 }} />
                            <Typography sx={{ fontWeight: 600, color: '#2563eb' }}>Cart</Typography>
                        </Box>
                        <Divider orientation="vertical" flexItem />
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <CheckCircleIcon sx={{ color: '#2563eb', fontSize: 22 }} />
                            <Typography sx={{ fontWeight: 600, color: '#2563eb' }}>Review</Typography>
                        </Box>
                        <Divider orientation="vertical" flexItem />
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Box sx={{ width: 22, height: 22, borderRadius: '50%', background: '#2563eb', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 700, fontSize: 14 }}>
                                3
                            </Box>
                            <Typography sx={{ fontWeight: 600 }}>Checkout</Typography>
                        </Box>
                    </Box>
                </Box>
                <Grid container spacing={4}>
                    {/* Left: Shipping Form */}
                    <Grid item xs={12} md={7}>
                        <Typography variant="h5" sx={{ fontWeight: 700, mb: 2 }}>Checkout</Typography>
                        <Typography sx={{ fontWeight: 600, mb: 2 }}>Shipping Information</Typography>
                        <Box sx={{ display: 'flex', gap: 2, mb: 3 }}>
                            <ToggleButtonGroup
                                value={shippingType}
                                exclusive
                                onChange={(_, val) => val && setShippingType(val)}
                                sx={{ background: '#f5f7fa', borderRadius: 2 }}
                            >
                                <ToggleButton value="delivery" sx={{ px: 4, fontWeight: 600, border: 0, borderRadius: 2 }}>
                                    Delivery
                                </ToggleButton>
                                <ToggleButton value="pickup" sx={{ px: 4, fontWeight: 600, border: 0, borderRadius: 2 }}>
                                    Pick up
                                </ToggleButton>
                            </ToggleButtonGroup>
                        </Box>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <TextField
                                    label="Full name"
                                    name="name"
                                    value={form.name}
                                    onChange={handleFormChange}
                                    required
                                    fullWidth
                                    sx={{ mb: 2 }}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    label="Email address"
                                    name="email"
                                    value={form.email}
                                    onChange={handleFormChange}
                                    required
                                    fullWidth
                                    sx={{ mb: 2 }}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    label="Phone number"
                                    name="phone"
                                    value={form.phone}
                                    onChange={handleFormChange}
                                    required
                                    fullWidth
                                    sx={{ mb: 2 }}
                                    InputProps={{
                                        startAdornment: <InputAdornment position="start">🇺🇸</InputAdornment>,
                                    }}
                                />
                            </Grid>
                            <Grid item xs={12}>
                                <TextField
                                    select
                                    label="Country"
                                    name="country"
                                    value={form.country}
                                    onChange={handleFormChange}
                                    required
                                    fullWidth
                                    sx={{ mb: 2 }}
                                >
                                    {countries.map((c) => (
                                        <MenuItem key={c.code} value={c.code}>{c.label}</MenuItem>
                                    ))}
                                </TextField>
                            </Grid>
                            <Grid item xs={12} sm={4}>
                                <TextField
                                    label="City"
                                    name="city"
                                    value={form.city}
                                    onChange={handleFormChange}
                                    required
                                    fullWidth
                                    sx={{ mb: 2 }}
                                />
                            </Grid>
                            <Grid item xs={12} sm={4}>
                                <TextField
                                    select
                                    label="State"
                                    name="state"
                                    value={form.state}
                                    onChange={handleFormChange}
                                    required
                                    fullWidth
                                    sx={{ mb: 2 }}
                                >
                                    {states.map((s) => (
                                        <MenuItem key={s} value={s}>{s}</MenuItem>
                                    ))}
                                </TextField>
                            </Grid>
                            <Grid item xs={12} sm={4}>
                                <TextField
                                    label="ZIP Code"
                                    name="zip"
                                    value={form.zip}
                                    onChange={handleFormChange}
                                    required
                                    fullWidth
                                    sx={{ mb: 2 }}
                                />
                            </Grid>
                        </Grid>
                        <FormControlLabel
                            control={<Checkbox name="terms" checked={form.terms} onChange={handleFormChange} />}
                            label={<Typography variant="body2">I have read and agree to the Terms and Conditions.</Typography>}
                            sx={{ mt: 1 }}
                        />
                    </Grid>
                    {/* Right: Cart Review */}
                    <Grid item xs={12} md={5}>
                        <Paper sx={{ p: 3, borderRadius: 3, mb: 2 }}>
                            <Typography sx={{ fontWeight: 600, mb: 2 }}>Review your cart</Typography>
                            {cartItems.map((item) => (
                                <Box key={item.id} sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <Box sx={{ width: 56, height: 56, borderRadius: 2, overflow: 'hidden', mr: 2, background: '#f5f7fa', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        <img src={item.image} alt={item.name} style={{ width: 48, height: 48, objectFit: 'contain' }} />
                                    </Box>
                                    <Box sx={{ flex: 1 }}>
                                        <Typography sx={{ fontWeight: 600 }}>{item.name}</Typography>
                                        <Typography variant="body2" sx={{ color: '#aaa' }}>{item.count}x</Typography>
                                    </Box>
                                    <Typography sx={{ fontWeight: 600 }}>${item.price.toFixed(2)}</Typography>
                                </Box>
                            ))}
                            <TextField
                                fullWidth
                                placeholder="Discount code"
                                name="discount"
                                value={form.discount}
                                onChange={handleFormChange}
                                InputProps={{
                                    endAdornment: (
                                        <InputAdornment position="end">
                                            <Button variant="contained" sx={{ borderRadius: 2, boxShadow: 0, textTransform: 'none', fontWeight: 600, background: '#e5e9f2', color: '#222', px: 3, '&:hover': { background: '#dbeafe' } }}>
                                                Apply
                                            </Button>
                                        </InputAdornment>
                                    ),
                                    sx: { borderRadius: 2, background: '#f5f6fa' },
                                }}
                                sx={{ mb: 3, mt: 1 }}
                            />
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                <Typography color="text.secondary">Subtotal</Typography>
                                <Typography>${subtotal.toFixed(2)}</Typography>
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                <Typography color="text.secondary">Shipping</Typography>
                                <Typography>${shipping.toFixed(2)}</Typography>
                            </Box>
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                                <Typography color="text.secondary">Discount</Typography>
                                <Typography color="text.secondary">-${discount.toFixed(2)}</Typography>
                            </Box>
                            <Divider sx={{ my: 2 }} />
                            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                                <Typography sx={{ fontWeight: 600 }}>Total</Typography>
                                <Typography sx={{ fontWeight: 700, fontSize: 20 }}>${total.toFixed(2)}</Typography>
                            </Box>
                            <Button fullWidth variant="contained" sx={{ background: '#2563eb', color: '#fff', borderRadius: 2, fontWeight: 600, fontSize: 16, py: 1.5, textTransform: 'none', boxShadow: 0, '&:hover': { background: '#1746a2' } }}>
                                Pay Now
                            </Button>
                        </Paper>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 2 }}>
                            <CheckCircleIcon sx={{ color: '#2563eb', fontSize: 22 }} />
                            <Typography sx={{ fontWeight: 600, color: '#2563eb' }}>Secure Checkout – SSL Encrypted</Typography>
                        </Box>
                        <Typography variant="body2" sx={{ color: '#888', mt: 1 }}>
                            Ensuring your financial and personal details are secure during every transaction.
                        </Typography>
                    </Grid>
                </Grid>
            </Box>
        </Box>
    );
};

export default CheckoutPage;
