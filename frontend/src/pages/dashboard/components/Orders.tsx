import React, { useState, useEffect } from 'react';
import {
    Box,
    Typography,
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
    Menu,
    MenuItem,
    Button,
} from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { merchantApi } from '../../../services/api';

interface Order {
    orderId: number;
    orderSize: number;
    status: string;
    createdAt: string;
    customer: {
        firstName: string;
        lastName: string;
    };
    merchantProduct: {
        product: {
            name: string;
            price: number;
        };
    };
}

export default function Orders() {
    const [orders, setOrders] = useState<Order[]>([]);
    const [loading, setLoading] = useState(true);
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);

    useEffect(() => {
        const fetchOrders = async () => {
            try {
                const response = await merchantApi.getOrders();
                setOrders(response.data);
            } catch (error) {
                console.error('Error fetching orders:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchOrders();
    }, []);

    const handleMenuClick = (event: React.MouseEvent<HTMLElement>, order: Order) => {
        setAnchorEl(event.currentTarget);
        setSelectedOrder(order);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
        setSelectedOrder(null);
    };

    const handleStatusChange = async (newStatus: string) => {
        if (!selectedOrder) return;

        try {
            // TODO: Implement status update API call
            // await merchantApi.updateOrderStatus(selectedOrder.orderId, newStatus);

            // Update local state
            setOrders(orders.map(order =>
                order.orderId === selectedOrder.orderId
                    ? { ...order, status: newStatus }
                    : order
            ));
        } catch (error) {
            console.error('Error updating order status:', error);
        } finally {
            handleMenuClose();
        }
    };

    if (loading) {
        return <Typography>Loading orders...</Typography>;
    }

    return (
        <Box>
            <Card elevation={0} sx={{ mb: 3, border: '1px solid', borderColor: 'grey.200' }}>
                <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                        <Typography variant="h6">Orders</Typography>
                        <Button variant="contained" color="primary">
                            Export Orders
                        </Button>
                    </Box>
                    <TableContainer component={Paper} elevation={0}>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Order ID</TableCell>
                                    <TableCell>Customer</TableCell>
                                    <TableCell>Product</TableCell>
                                    <TableCell>Quantity</TableCell>
                                    <TableCell>Total</TableCell>
                                    <TableCell>Date</TableCell>
                                    <TableCell>Status</TableCell>
                                    <TableCell>Actions</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {orders.map((order) => (
                                    <TableRow key={order.orderId}>
                                        <TableCell>#{order.orderId}</TableCell>
                                        <TableCell>
                                            {order.customer.firstName} {order.customer.lastName}
                                        </TableCell>
                                        <TableCell>{order.merchantProduct.product.name}</TableCell>
                                        <TableCell>{order.orderSize}</TableCell>
                                        <TableCell>
                                            ${(order.orderSize * order.merchantProduct.product.price).toFixed(2)}
                                        </TableCell>
                                        <TableCell>
                                            {new Date(order.createdAt).toLocaleDateString()}
                                        </TableCell>
                                        <TableCell>
                                            <Chip
                                                label={order.status}
                                                color={
                                                    order.status === 'DELIVERED' ? 'success' :
                                                        order.status === 'PROCESSING' ? 'warning' :
                                                            order.status === 'CANCELLED' ? 'error' : 'default'
                                                }
                                                size="small"
                                            />
                                        </TableCell>
                                        <TableCell>
                                            <IconButton
                                                size="small"
                                                onClick={(e) => handleMenuClick(e, order)}
                                            >
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

            <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
            >
                <MenuItem onClick={() => handleStatusChange('PROCESSING')}>
                    Mark as Processing
                </MenuItem>
                <MenuItem onClick={() => handleStatusChange('DELIVERED')}>
                    Mark as Delivered
                </MenuItem>
                <MenuItem onClick={() => handleStatusChange('CANCELLED')}>
                    Mark as Cancelled
                </MenuItem>
            </Menu>
        </Box>
    );
} 