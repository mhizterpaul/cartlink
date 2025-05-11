import React from 'react';
import {
    Box,
    Typography,
    Paper,
    Avatar,
    Button,
    Divider,
    LinearProgress,
    Grid,
    IconButton,
    TextField,
    MenuItem,
} from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import StarBorderIcon from '@mui/icons-material/StarBorder';
import FilterListIcon from '@mui/icons-material/FilterList';
import AddIcon from '@mui/icons-material/Add';

const ratingSummary = {
    average: 4.8,
    total: 150,
    breakdown: [
        { stars: 5, count: 133 },
        { stars: 4, count: 13 },
        { stars: 3, count: 2 },
        { stars: 2, count: 0 },
        { stars: 1, count: 2 },
    ],
    recommend: 96,
};

const userImages = [
    'https://randomuser.me/api/portraits/women/1.jpg',
    'https://randomuser.me/api/portraits/men/2.jpg',
    'https://randomuser.me/api/portraits/women/3.jpg',
    'https://randomuser.me/api/portraits/men/4.jpg',
    'https://randomuser.me/api/portraits/women/5.jpg',
];

const reviews = [
    {
        id: 1,
        name: 'Brett P.',
        avatar: 'BP',
        verified: true,
        recommend: true,
        rating: 5,
        title: 'Love it',
        text: `I've been dealing with colitis for quite some time now, and finding a suitable pre-workout protein that wouldn't aggravate my condition was a constant struggle. This pre-workout has been a game changer for me. I no longer have to worry about discomfort during my workouts, and I can push myself harder than ever before. I can't stress enough how much of a relief it is to have finally found a product that works perfectly for my needs. I'm thrilled to have stumbled upon this brand, and I won't be buying my pre-workout elsewhere ever again. Highly recommended!`,
        date: '1 month ago',
        helpful: 21,
        notHelpful: 0,
        image: 'https://images.unsplash.com/photo-1519864600265-abb23847ef2c?auto=format&fit=crop&w=120&q=80',
    },
    {
        id: 2,
        name: 'Kevin M.',
        avatar: 'KM',
        verified: true,
        recommend: true,
        rating: 5,
        title: 'The Best Pre-Workout EVER!!',
        text: 'Its really good. Great flavor.',
        date: '1 month ago',
        helpful: 8,
        notHelpful: 0,
    },
    {
        id: 3,
        name: 'Zoe A.',
        avatar: 'ZA',
        verified: true,
        recommend: true,
        rating: 5,
        title: 'More Energy And Better Focus',
        text: 'I love this stuff!!',
        date: '3 month ago',
        helpful: 6,
        notHelpful: 0,
    },
    {
        id: 4,
        name: 'Lindsey F.',
        avatar: 'LF',
        verified: true,
        recommend: true,
        rating: 5,
        title: 'A Lifesaver',
        text: `Discovering this brand has been a game changer for me, and I couldn't be happier with the results. I've tried numerous options in the past, but this one stands out as an absolute lifesaver.`,
        date: '3 month ago',
        helpful: 0,
        notHelpful: 0,
    },
];

const ReviewPage = () => {
    return (
        <Box sx={{ minHeight: '100vh', background: 'radial-gradient(circle at 70% 10%, #2d1a6b 0%, #0b0b2a 100%)', py: 6 }}>
            <Box sx={{ maxWidth: 1100, mx: 'auto', background: '#fff', borderRadius: 4, p: 4, boxShadow: 2 }}>
                {/* Header */}
                <Typography variant="h5" sx={{ fontWeight: 900, color: '#23237b', mb: 2, letterSpacing: 1 }}>REVIEWS</Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 4, flexWrap: 'wrap', mb: 2 }}>
                    {/* Rating summary */}
                    <Box sx={{ minWidth: 220 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Typography sx={{ fontWeight: 700, fontSize: 28 }}>{ratingSummary.average}</Typography>
                            <StarIcon sx={{ color: '#fbc02d', fontSize: 24, mb: '2px' }} />
                            <Typography sx={{ color: '#888', fontWeight: 500, ml: 1 }}>
                                Based on {ratingSummary.total} reviews
                            </Typography>
                        </Box>
                        <Box sx={{ mt: 1 }}>
                            {ratingSummary.breakdown.map((row) => (
                                <Box key={row.stars} sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5 }}>
                                    <Typography sx={{ width: 16, fontWeight: 600 }}>{row.stars}</Typography>
                                    <StarIcon sx={{ color: '#fbc02d', fontSize: 16 }} />
                                    <LinearProgress
                                        variant="determinate"
                                        value={row.count / ratingSummary.total * 100}
                                        sx={{ width: 80, height: 8, borderRadius: 5, mx: 1, background: '#eee', '& .MuiLinearProgress-bar': { background: '#fbc02d' } }}
                                    />
                                    <Typography sx={{ color: '#888', fontSize: 13 }}>{row.count}</Typography>
                                </Box>
                            ))}
                        </Box>
                    </Box>
                    {/* Recommendation and user images */}
                    <Box sx={{ flex: 1, minWidth: 220 }}>
                        <Typography sx={{ fontWeight: 700, fontSize: 18, mb: 1 }}>{ratingSummary.recommend}% <span style={{ fontWeight: 400, color: '#888', fontSize: 15 }}>would recommend this product</span></Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            {userImages.map((img, i) => (
                                <Avatar key={i} src={img} sx={{ width: 44, height: 44, border: '2px solid #fff', boxShadow: 1, ml: i ? -1.5 : 0, zIndex: 10 - i }} />
                            ))}
                        </Box>
                    </Box>
                    {/* Write a review button */}
                    <Box sx={{ minWidth: 180, textAlign: 'right', flex: 1 }}>
                        <Button variant="outlined" sx={{ borderRadius: 2, fontWeight: 700, px: 3, py: 1, borderColor: '#23237b', color: '#23237b', textTransform: 'none', fontSize: 16 }} startIcon={<AddIcon />}>
                            Write A Review
                        </Button>
                    </Box>
                </Box>
                {/* Filter and sort */}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                    <Button variant="outlined" startIcon={<FilterListIcon />} sx={{ borderRadius: 2, fontWeight: 600, color: '#23237b', borderColor: '#e0e0e0', background: '#fafbfc', textTransform: 'none' }}>Filter</Button>
                    <TextField select size="small" value="most-recent" sx={{ width: 160, borderRadius: 2, background: '#fafbfc' }}>
                        <MenuItem value="most-recent">Most Recent</MenuItem>
                        <MenuItem value="highest">Highest Rated</MenuItem>
                        <MenuItem value="lowest">Lowest Rated</MenuItem>
                    </TextField>
                </Box>
                {/* Reviews List */}
                <Box>
                    {reviews.map((review) => (
                        <Paper key={review.id} sx={{ p: 3, borderRadius: 3, mb: 3, boxShadow: 0, background: '#fafbfc' }}>
                            <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 2 }}>
                                <Avatar sx={{ width: 48, height: 48, fontWeight: 700, fontSize: 20, bgcolor: '#23237b' }}>{review.avatar}</Avatar>
                                <Box sx={{ flex: 1 }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                        <Typography sx={{ fontWeight: 700 }}>{review.name}</Typography>
                                        {review.verified && <Typography sx={{ color: '#2563eb', fontWeight: 600, fontSize: 13, ml: 1, px: 1, borderRadius: 1, background: '#e3eafe' }}>Verified Buyer</Typography>}
                                    </Box>
                                    {review.recommend && <Typography sx={{ color: '#2ecc71', fontWeight: 500, fontSize: 14, mt: 0.5 }}>I recommend this product</Typography>}
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 1, mb: 0.5 }}>
                                        {[...Array(5)].map((_, i) => i < review.rating ? <StarIcon key={i} sx={{ color: '#fbc02d', fontSize: 20 }} /> : <StarBorderIcon key={i} sx={{ color: '#fbc02d', fontSize: 20 }} />)}
                                    </Box>
                                    <Typography sx={{ fontWeight: 700, fontSize: 17, mb: 0.5 }}>{review.title}</Typography>
                                    <Typography sx={{ color: '#444', mb: 1 }}>{review.text}</Typography>
                                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, color: '#888', fontSize: 14 }}>
                                        <Typography>Was this helpful? <b>{review.helpful}</b> <span style={{ marginLeft: 8 }}>|</span> <b>{review.notHelpful}</b></Typography>
                                        <Typography sx={{ ml: 'auto', color: '#aaa', fontSize: 13 }}>{review.date}</Typography>
                                    </Box>
                                </Box>
                                {review.image && (
                                    <Box sx={{ ml: 2 }}>
                                        <img src={review.image} alt="review" style={{ width: 90, height: 90, borderRadius: 8, objectFit: 'cover' }} />
                                    </Box>
                                )}
                            </Box>
                        </Paper>
                    ))}
                </Box>
            </Box>
        </Box>
    );
};

export default ReviewPage; 