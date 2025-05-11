import React, { useState } from 'react';
import {
    Box,
    Button,
    Typography,
    IconButton,
    TextField,
    Paper,
    Divider,
    InputAdornment,
    Grid,
} from '@mui/material';
import RemoveIcon from '@mui/icons-material/Remove';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';

const initialCart = [
    {
        id: 1,
        name: 'Apple AirPods Pro',
        color: 'White',
        price: 249.99,
        count: 1,
        image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MWP22?wid=200&hei=200&fmt=jpeg&qlt=95&.v=1591634795000',
    },
    {
        id: 2,
        name: 'Apple AirPods Max',
        color: 'Silver',
        price: 549.99,
        count: 1,
        image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/airpods-max-select-silver-202011?wid=200&hei=200&fmt=jpeg&qlt=95&.v=1604022365000',
    },
    {
        id: 3,
        name: 'Apple HomePod mini',
        color: 'Silver',
        price: 99.99,
        count: 1,
        image: 'https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/homepod-mini-select-202110?wid=200&hei=200&fmt=jpeg&qlt=95&.v=1632925511000',
    },
];

const CartPage = () => {
    const [cart, setCart] = useState(initialCart);
    const [promo, setPromo] = useState('');

    const handleCount = (id: number, delta: number) => {
        setCart((prev) =>
            prev.map((item) =>
                item.id === id
                    ? { ...item, count: Math.max(1, item.count + delta) }
                    : item
            )
        );
    };

    const handleRemove = (id: number) => {
        setCart((prev) => prev.filter((item) => item.id !== id));
    };

    const handleClear = () => setCart([]);

    const subtotal = cart.reduce((sum, item) => sum + item.price * item.count, 0);
    const discount = 0;
    const total = subtotal - discount;

    return (
        <Box sx={{ minHeight: '100vh', background: '#f4f6fa', py: 4 }}>
            {/* Browser header mock */}
            <Box sx={{ maxWidth: 1100, mx: 'auto', mb: 2 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', py: 1, px: 2, borderRadius: 2, background: '#fff', boxShadow: 1 }}>
                    <Box sx={{ width: 12, height: 12, borderRadius: '50%', background: '#f66', mr: 1 }} />
                    <Box sx={{ width: 12, height: 12, borderRadius: '50%', background: '#fc6', mr: 1 }} />
                    <Box sx={{ width: 12, height: 12, borderRadius: '50%', background: '#6c6', mr: 2 }} />
                    <Typography sx={{ fontWeight: 500, color: '#888', flex: 1 }}>eevui.com</Typography>
                </Box>
            </Box>
            {/* Main content */}
            <Box sx={{ maxWidth: 1100, mx: 'auto', display: 'flex', gap: 3 }}>
                {/* Cart section */}
                <Box sx={{ flex: 2 }}>
                    <Paper sx={{ p: 3, borderRadius: 3, mb: 3 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                            <Typography variant="h6" sx={{ fontWeight: 700, flex: 1 }}>
                                Cart <Typography component="span" variant="body2" sx={{ color: '#aaa', fontWeight: 400 }}>
                                    ({cart.length} products)
                                </Typography>
                            </Typography>
                            <Button color="error" sx={{ textTransform: 'none', fontWeight: 500 }} onClick={handleClear} disabled={cart.length === 0}>
                                Clear cart
                            </Button>
                        </Box>
                        <Divider sx={{ mb: 2 }} />
                        <Grid container spacing={2} sx={{ fontWeight: 600, color: '#888', mb: 1, px: 1 }}>
                            <Grid item xs={6}>Product</Grid>
                            <Grid item xs={3}>Count</Grid>
                            <Grid item xs={3}>Price</Grid>
                        </Grid>
                        {cart.map((item) => (
                            <Paper key={item.id} sx={{ display: 'flex', alignItems: 'center', mb: 2, p: 2, borderRadius: 2, boxShadow: 0, background: '#fafbfc' }}>
                                <Box sx={{ width: 60, height: 60, borderRadius: 2, overflow: 'hidden', mr: 2, background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    <img src={item.image} alt={item.name} style={{ width: 48, height: 48, objectFit: 'contain' }} />
                                </Box>
                                <Box sx={{ flex: 1 }}>
                                    <Typography sx={{ fontWeight: 600 }}>{item.name}</Typography>
                                    <Typography variant="body2" sx={{ color: '#aaa' }}>{item.color}</Typography>
                                </Box>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, width: 120, justifyContent: 'center' }}>
                                    <IconButton onClick={() => handleCount(item.id, -1)} size="small" sx={{ border: '1px solid #eee', background: '#fff' }}>
                                        <RemoveIcon fontSize="small" />
                                    </IconButton>
                                    <Typography sx={{ mx: 1, minWidth: 24, textAlign: 'center' }}>{item.count}</Typography>
                                    <IconButton onClick={() => handleCount(item.id, 1)} size="small" sx={{ border: '1px solid #eee', background: '#fff' }}>
                                        <AddIcon fontSize="small" />
                                    </IconButton>
                                </Box>
                                <Box sx={{ width: 100, textAlign: 'right', fontWeight: 600, color: '#222' }}>
                                    ${(item.price * item.count).toFixed(2)}
                                </Box>
                                <IconButton onClick={() => handleRemove(item.id)} sx={{ ml: 2 }}>
                                    <CloseIcon fontSize="small" />
                                </IconButton>
                            </Paper>
                        ))}
                    </Paper>
                    {/* Bottom banner */}
                    <Paper sx={{ p: 3, borderRadius: 3, background: '#111', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 2 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <img src="https://store.storeimages.cdn-apple.com/4982/as-images.apple.com/is/MYAV2?wid=80&hei=80&fmt=jpeg&qlt=95&.v=1591634795000" alt="Apple Watch" style={{ width: 60, height: 60, borderRadius: 8 }} />
                            <Typography sx={{ fontWeight: 500, fontSize: 18 }}>
                                Check the newest Apple products<br />
                                <Typography component="span" variant="body2" sx={{ color: '#aaa' }}>Official Apple retailer</Typography>
                            </Typography>
                        </Box>
                        <Button variant="contained" sx={{ background: '#fff', color: '#111', borderRadius: 2, fontWeight: 600, px: 4, py: 1, boxShadow: 0, textTransform: 'none', '&:hover': { background: '#eee' } }}>
                            Shop now
                        </Button>
                    </Paper>
                </Box>
                {/* Summary section */}
                <Box sx={{ flex: 1, minWidth: 320 }}>
                    <Paper sx={{ p: 3, borderRadius: 3, mb: 3 }}>
                        <Typography sx={{ fontWeight: 600, mb: 2 }}>Promo code</Typography>
                        <TextField
                            fullWidth
                            placeholder="Type here..."
                            value={promo}
                            onChange={e => setPromo(e.target.value)}
                            InputProps={{
                                endAdornment: (
                                    <InputAdornment position="end">
                                        <Button variant="contained" sx={{ borderRadius: 2, boxShadow: 0, textTransform: 'none', fontWeight: 600, background: '#222', color: '#fff', px: 3, '&:hover': { background: '#111' } }}>
                                            Apply
                                        </Button>
                                    </InputAdornment>
                                ),
                                sx: { borderRadius: 2, background: '#f5f6fa' },
                            }}
                            sx={{ mb: 3 }}
                        />
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                            <Typography color="text.secondary">Subtotal</Typography>
                            <Typography>${subtotal.toFixed(2)}</Typography>
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
                        <Button fullWidth variant="contained" sx={{ background: '#111', color: '#fff', borderRadius: 2, fontWeight: 600, fontSize: 16, py: 1.5, textTransform: 'none', boxShadow: 0, '&:hover': { background: '#222' } }}>
                            Continue to checkout
                        </Button>
                    </Paper>
                </Box>
            </Box>
        </Box>
    );
};

export default CartPage;
