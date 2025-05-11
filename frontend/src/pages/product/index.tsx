import React, { useState } from 'react';
import {
    Box,
    Typography,
    Button,
    Paper,
    IconButton,
    Divider,
    Grid,
    Avatar,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RemoveIcon from '@mui/icons-material/Remove';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import ArrowBackIosNewIcon from '@mui/icons-material/ArrowBackIosNew';
import ArrowForwardIosIcon from '@mui/icons-material/ArrowForwardIos';

const product = {
    name: 'Men Skin Care + Body Wash',
    price: 33.0,
    image: 'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    offer: 'GET 20% OFF! PLUS 2 POINTS PER $1 on this order when you open and use the MR Rewards® credit card.',
    offerImg: 'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    description:
        'Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s, when an unknown.',
};

const promoProduct = {
    name: 'Gentle conditioner',
    price: 19.99,
    desc: 'Condition + Moisturize',
    size: '150 ml',
    image: 'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
};

const alsoLike = [
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
];

const mensCollection = [
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
    'https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png',
];

const ProductPage = () => {
    const [qty, setQty] = useState(2);
    const [carouselIdx, setCarouselIdx] = useState(0);

    return (
        <Box sx={{ minHeight: '100vh', background: '#18191b', py: 4 }}>
            <Box sx={{ maxWidth: 1100, mx: 'auto', background: '#fff', borderRadius: 4, p: 3, boxShadow: 2 }}>
                {/* Header */}
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Typography sx={{ fontWeight: 900, fontSize: 24, color: '#232323', flex: 1 }}>
                        <span style={{ color: '#232323' }}>eCommerce</span>
                        <span style={{ color: '#2196f3' }}>Hero</span>
                    </Typography>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 3, color: '#232323', fontWeight: 600, fontSize: 16 }}>
                        <Typography>Collections</Typography>
                        <Typography>Brands</Typography>
                        <Typography>Sales</Typography>
                        <Typography>ENG</Typography>
                        <ShoppingCartIcon />
                        <Avatar sx={{ width: 28, height: 28, bgcolor: '#e3e3e3', color: '#232323', fontWeight: 700 }}>U</Avatar>
                    </Box>
                </Box>
                {/* Product Title */}
                <Typography align="center" sx={{ color: '#2196f3', fontWeight: 700, fontSize: 26, mb: 2 }}>
                    {product.name}
                </Typography>
                <Grid container spacing={3}>
                    {/* Left: Product Image and Offer */}
                    <Grid item xs={12} md={7}>
                        <Paper sx={{ p: 3, borderRadius: 3, mb: 2, display: 'flex', alignItems: 'center', gap: 3, background: '#f7fafd' }}>
                            <Box sx={{ flex: 1, display: 'flex', alignItems: 'center', gap: 2 }}>
                                <img src={product.image} alt={product.name} style={{ width: 120, height: 180, objectFit: 'contain' }} />
                                <img src={product.image} alt={product.name} style={{ width: 120, height: 180, objectFit: 'contain' }} />
                            </Box>
                            <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 2 }}>
                                <Paper sx={{ background: '#2ea7f7', color: '#fff', borderRadius: 2, p: 2, textAlign: 'center', fontWeight: 700, fontSize: 16, mb: 2 }}>
                                    GET 20% OFF!<br />PLUS 2 POINTS PER $1<br />
                                    <Typography sx={{ fontWeight: 400, fontSize: 13, mt: 1 }}>
                                        on this order when you open and use the MR Rewards® credit card.
                                    </Typography>
                                    <Box sx={{ mt: 1, display: 'flex', justifyContent: 'center' }}>
                                        <img src="https://i.ibb.co/6bQ6b7B/men-skin-care-body-wash.png" alt="card" style={{ width: 60, height: 40, objectFit: 'contain', borderRadius: 4 }} />
                                    </Box>
                                </Paper>
                            </Box>
                        </Paper>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                            <Typography sx={{ color: '#2196f3', fontWeight: 700, fontSize: 28 }}>${product.price.toFixed(2)}</Typography>
                            <Box sx={{ display: 'flex', alignItems: 'center', background: '#232323', borderRadius: 2, px: 1, py: 0.5, color: '#fff', fontWeight: 700, fontSize: 18 }}>
                                <IconButton size="small" onClick={() => setQty(qty > 1 ? qty - 1 : 1)} sx={{ color: '#fff' }}><RemoveIcon /></IconButton>
                                <Typography sx={{ mx: 1 }}>{qty}</Typography>
                                <IconButton size="small" onClick={() => setQty(qty + 1)} sx={{ color: '#fff' }}><AddIcon /></IconButton>
                            </Box>
                        </Box>
                        <Button fullWidth variant="contained" sx={{ background: '#2196f3', color: '#fff', borderRadius: 2, fontWeight: 700, fontSize: 18, py: 1.2, textTransform: 'none', mb: 2 }}>
                            ADD TO CARD
                        </Button>
                        <Typography sx={{ color: '#232323', fontSize: 15, mt: 1 }}>{product.description}</Typography>
                    </Grid>
                    {/* Right: Promo and Recommendations */}
                    <Grid item xs={12} md={5}>
                        {/* Promo Card */}
                        <Paper sx={{ background: '#d32f2f', color: '#fff', borderRadius: 3, p: 3, mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
                            <Box sx={{ flex: 1 }}>
                                <Typography sx={{ fontWeight: 700, fontSize: 20, mb: 1 }}>{promoProduct.name}</Typography>
                                <Typography sx={{ fontWeight: 400, fontSize: 15, mb: 1 }}>{promoProduct.desc}</Typography>
                                <Typography sx={{ fontWeight: 700, fontSize: 22, mb: 1 }}>${promoProduct.price.toFixed(2)}</Typography>
                                <Typography sx={{ fontWeight: 400, fontSize: 15, mb: 1 }}>{promoProduct.size}</Typography>
                                <Button variant="contained" sx={{ background: '#fff', color: '#d32f2f', borderRadius: 2, fontWeight: 700, px: 3, py: 1, textTransform: 'none', boxShadow: 0, '&:hover': { background: '#f5f5f5' } }}>
                                    ADD TO CARD
                                </Button>
                            </Box>
                            <Box>
                                <img src={promoProduct.image} alt={promoProduct.name} style={{ width: 80, height: 120, objectFit: 'contain' }} />
                            </Box>
                            <IconButton sx={{ color: '#fff', position: 'absolute', top: 12, right: 12, background: 'rgba(0,0,0,0.1)' }}>
                                <AddIcon />
                            </IconButton>
                        </Paper>
                        {/* You may also like carousel */}
                        <Paper sx={{ background: '#fafbfc', borderRadius: 3, p: 2, mb: 3 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                                <Typography sx={{ fontWeight: 700, fontSize: 18, flex: 1 }}>YOU MAY ALSO LIKE</Typography>
                                <IconButton onClick={() => setCarouselIdx((carouselIdx - 1 + alsoLike.length) % alsoLike.length)}>
                                    <ArrowBackIosNewIcon fontSize="small" />
                                </IconButton>
                                <IconButton onClick={() => setCarouselIdx((carouselIdx + 1) % alsoLike.length)}>
                                    <ArrowForwardIosIcon fontSize="small" />
                                </IconButton>
                            </Box>
                            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'center' }}>
                                {alsoLike.slice(carouselIdx, carouselIdx + 3).map((img, i) => (
                                    <Paper key={i} sx={{ p: 1, borderRadius: 2, background: '#fff' }}>
                                        <img src={img} alt="also like" style={{ width: 60, height: 60, objectFit: 'contain' }} />
                                    </Paper>
                                ))}
                            </Box>
                        </Paper>
                        {/* Men's Collection */}
                        <Box>
                            <Typography sx={{ fontWeight: 700, fontSize: 18, mb: 1 }}>Men's Collection</Typography>
                            <Box sx={{ display: 'flex', gap: 2 }}>
                                {mensCollection.map((img, i) => (
                                    <Paper key={i} sx={{ p: 1, borderRadius: 2, background: '#fff' }}>
                                        <img src={img} alt="men's collection" style={{ width: 48, height: 48, objectFit: 'contain' }} />
                                    </Paper>
                                ))}
                            </Box>
                        </Box>
                    </Grid>
                </Grid>
            </Box>
        </Box>
    );
};

export default ProductPage;
