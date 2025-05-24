import React, { useState, useEffect } from 'react';
import {
    Box,
    Typography,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemAvatar,
    ListItemText,
    Avatar,
    Chip,
    Rating,
    Divider,
    Button,
} from '@mui/material';
import { merchantApi } from '../../../services/api';

interface Review {
    reviewId: number;
    rating: number;
    comment: string;
    createdAt: string;
    customer: {
        firstName: string;
        lastName: string;
    };
    merchantProduct: {
        product: {
            name: string;
        };
    };
}

export default function Reviews() {
    const [reviews, setReviews] = useState<Review[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchReviews = async () => {
            try {
                const response = await merchantApi.getReviews();
                setReviews(response.data);
            } catch (error) {
                console.error('Error fetching reviews:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchReviews();
    }, []);

    if (loading) {
        return <Typography>Loading reviews...</Typography>;
    }

    return (
        <Box>
            <Card elevation={0} sx={{ mb: 3, border: '1px solid', borderColor: 'grey.200' }}>
                <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                        <Typography variant="h6">Customer Reviews</Typography>
                        <Button variant="contained" color="primary">
                            Export Reviews
                        </Button>
                    </Box>
                    <List>
                        {reviews.map((review, index) => (
                            <React.Fragment key={review.reviewId}>
                                <ListItem alignItems="flex-start" sx={{ px: 0 }}>
                                    <ListItemAvatar>
                                        <Avatar sx={{ bgcolor: 'primary.main' }}>
                                            {review.customer.firstName[0]}
                                        </Avatar>
                                    </ListItemAvatar>
                                    <ListItemText
                                        primary={
                                            <Box display="flex" alignItems="center" gap={1}>
                                                <Typography variant="subtitle1">
                                                    {review.customer.firstName} {review.customer.lastName}
                                                </Typography>
                                                <Chip
                                                    label={review.merchantProduct.product.name}
                                                    size="small"
                                                    color="primary"
                                                    variant="outlined"
                                                />
                                            </Box>
                                        }
                                        secondary={
                                            <Box mt={1}>
                                                <Rating value={review.rating} readOnly size="small" />
                                                <Typography
                                                    variant="body2"
                                                    color="text.secondary"
                                                    sx={{ mt: 1 }}
                                                >
                                                    {review.comment}
                                                </Typography>
                                                <Typography
                                                    variant="caption"
                                                    color="text.secondary"
                                                    sx={{ mt: 1, display: 'block' }}
                                                >
                                                    {new Date(review.createdAt).toLocaleDateString()}
                                                </Typography>
                                            </Box>
                                        }
                                    />
                                </ListItem>
                                {index < reviews.length - 1 && <Divider />}
                            </React.Fragment>
                        ))}
                    </List>
                </CardContent>
            </Card>
        </Box>
    );
} 