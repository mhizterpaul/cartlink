import React, { useState, useRef, useEffect } from 'react';
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
    Dialog,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import FilterListIcon from '@mui/icons-material/FilterList';
import ProductForm from './Product/ProductForm';

const products = [
    {
        name: 'Gabriela Cashmere Blazer', sku: 'T1816', price: 113.99, products: 1113, views: 14012, status: 'Active', image: '/images/jacket1.png',
    },
    {
        name: 'Loewe blend Jacket - Blue', sku: 'T1816', price: 113.99, products: 721, views: 13212, status: 'Active', image: '/images/jacket2.png',
    },
    {
        name: 'Sandro - Jacket - Black', sku: 'T1816', price: 113.99, products: 407, views: 8201, status: 'Active', image: '/images/jacket3.png',
    },
    {
        name: 'Adidas By Stella McCartney', sku: 'T1816', price: 113.99, products: 1203, views: 10902, status: 'Active', image: '/images/jacket4.png',
    },
    {
        name: 'Meteo Hooded Wool Jacket', sku: 'T1816', price: 113.99, products: 306, views: 807, status: 'Active', image: '/images/jacket5.png',
    },
    {
        name: 'Hida Down Ski Jacket - Red', sku: 'T1816', price: 113.99, products: 201, views: 204, status: 'Active', image: '/images/jacket6.png',
    },
    {
        name: 'Dolce & Gabbana', sku: 'T1816', price: 113.99, products: 108, views: 64, status: 'Active', image: '/images/jacket7.png',
    },
];

export default function Inventory() {
    const [isFormOpen, setIsFormOpen] = useState(false);
    const formRef = useRef<HTMLDivElement>(null);

    const handleClickOutside = (event: MouseEvent) => {
        if (formRef.current && !formRef.current.contains(event.target as Node)) {
            setIsFormOpen(false);
        }
    };

    useEffect(() => {
        document.addEventListener('mousedown', handleClickOutside);
        return () => {
            document.removeEventListener('mousedown', handleClickOutside);
        };
    }, []);

    const handleClose = () => {
        setIsFormOpen(false);
    };

    return (
        <Box sx={{ p: { xs: 0, md: 2 } }}>
            <Card elevation={0} sx={{ border: '1px solid', borderColor: 'grey.200', mb: 2 }}>
                <CardHeader
                    title={<Typography variant="h6" fontWeight={700}>Products</Typography>}
                    action={
                        <Button
                            variant="contained"
                            color="primary"
                            sx={{ borderRadius: 2, fontWeight: 600 }}
                            onClick={() => setIsFormOpen(true)}
                        >
                            + Add Product
                        </Button>
                    }
                />
                <CardContent sx={{ pt: 0 }}>
                    <Grid container spacing={2} alignItems="center" sx={{ mb: 2 }}>
                        <Grid item xs={12} md={3}>
                            <TextField fullWidth size="small" placeholder="Search customer..." />
                        </Grid>
                        <Grid item xs={12} md={9}>
                            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', justifyContent: { xs: 'flex-start', md: 'flex-end' } }}>
                                <FormControl size="small" sx={{ minWidth: 120 }}>
                                    <InputLabel>Category</InputLabel>
                                    <Select label="Category" defaultValue="Jackets">
                                        <MenuItem value="Jackets">Jackets (132)</MenuItem>
                                    </Select>
                                </FormControl>
                                <FormControl size="small" sx={{ minWidth: 120 }}>
                                    <InputLabel>Status</InputLabel>
                                    <Select label="Status" defaultValue="All Status">
                                        <MenuItem value="All Status">All Status</MenuItem>
                                    </Select>
                                </FormControl>
                                <FormControl size="small" sx={{ minWidth: 120 }}>
                                    <InputLabel>Price</InputLabel>
                                    <Select label="Price" defaultValue="$50-$100">
                                        <MenuItem value="$50-$100">$50 - $100</MenuItem>
                                    </Select>
                                </FormControl>
                                <FormControl size="small" sx={{ minWidth: 120 }}>
                                    <InputLabel>Store</InputLabel>
                                    <Select label="Store" defaultValue="All Store">
                                        <MenuItem value="All Store">All Store</MenuItem>
                                    </Select>
                                </FormControl>
                                <Button variant="outlined" startIcon={<FilterListIcon />} sx={{ borderRadius: 2 }}>
                                    Filter
                                </Button>
                                <Button variant="outlined" sx={{ borderRadius: 2 }}>
                                    Show: All Products
                                </Button>
                                <Button variant="outlined" sx={{ borderRadius: 2 }}>
                                    Sort by: Default
                                </Button>
                            </Box>
                        </Grid>
                    </Grid>
                    <TableContainer component={Paper} sx={{ borderRadius: 2, boxShadow: 'none', border: '1px solid', borderColor: 'grey.200' }}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell padding="checkbox"></TableCell>
                                    <TableCell>Product Name</TableCell>
                                    <TableCell>Purchase Unit Price</TableCell>
                                    <TableCell>Products</TableCell>
                                    <TableCell>Views</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell>Action</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {products.map((product, idx) => (
                                    <TableRow key={idx}>
                                        <TableCell padding="checkbox">
                                            <input type="checkbox" />
                                        </TableCell>
                                        <TableCell>
                                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                                                <Avatar src={product.image} alt={product.name} sx={{ width: 40, height: 40 }} />
                                                <Box>
                                                    <Typography fontWeight={600}>{product.name}</Typography>
                                                    <Typography variant="caption" color="text.secondary">SKU: {product.sku}</Typography>
                                                </Box>
                                            </Box>
                                        </TableCell>
                                        <TableCell>${product.price.toFixed(2)}</TableCell>
                                        <TableCell>{product.products}</TableCell>
                                        <TableCell>{product.views}</TableCell>
                                        <TableCell>
                                            <Chip label={product.status} color={product.status === 'Active' ? 'success' : 'default'} size="small" />
                                        </TableCell>
                                        <TableCell>
                                            <IconButton color="primary" size="small">
                                                <EditIcon />
                                            </IconButton>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', mt: 2 }}>
                        <Pagination count={6} page={3} color="primary" />
                    </Box>
                </CardContent>
            </Card>

            <Dialog
                open={isFormOpen}
                onClose={handleClose}
                maxWidth="md"
                fullWidth
                PaperProps={{
                    sx: {
                        borderRadius: 2,
                        boxShadow: '0 4px 20px rgba(0,0,0,0.1)'
                    }
                }}
            >
                <Box ref={formRef}>
                    <ProductForm onClose={handleClose} />
                </Box>
            </Dialog>
        </Box>
    );
} 