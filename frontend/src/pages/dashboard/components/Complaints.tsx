import React, { useState, useEffect } from 'react';
import {
    Box,
    Typography,
    Card,
    CardContent,
    List,
    ListItem,
    ListItemText,
    Chip,
    Button,
    Divider,
    IconButton,
    Menu,
    MenuItem,
} from '@mui/material';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import { merchantApi } from '../../../services/api';

interface Complaint {
    complaintId: number;
    title: string;
    description: string;
    status: string;
    createdAt: string;
    order: {
        orderId: number;
        merchantProduct: {
            product: {
                name: string;
            };
        };
    };
}

export default function Complaints() {
    const [complaints, setComplaints] = useState<Complaint[]>([]);
    const [loading, setLoading] = useState(true);
    const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
    const [selectedComplaint, setSelectedComplaint] = useState<Complaint | null>(null);

    useEffect(() => {
        const fetchComplaints = async () => {
            try {
                const response = await merchantApi.getComplaints();
                setComplaints(response.data);
            } catch (error) {
                console.error('Error fetching complaints:', error);
            } finally {
                setLoading(false);
            }
        };
        fetchComplaints();
    }, []);

    const handleMenuClick = (event: React.MouseEvent<HTMLElement>, complaint: Complaint) => {
        setAnchorEl(event.currentTarget);
        setSelectedComplaint(complaint);
    };

    const handleMenuClose = () => {
        setAnchorEl(null);
        setSelectedComplaint(null);
    };

    const handleStatusChange = async (newStatus: string) => {
        if (!selectedComplaint) return;

        try {
            // TODO: Implement status update API call
            // await merchantApi.updateComplaintStatus(selectedComplaint.complaintId, newStatus);

            // Update local state
            setComplaints(complaints.map(complaint =>
                complaint.complaintId === selectedComplaint.complaintId
                    ? { ...complaint, status: newStatus }
                    : complaint
            ));
        } catch (error) {
            console.error('Error updating complaint status:', error);
        } finally {
            handleMenuClose();
        }
    };

    if (loading) {
        return <Typography>Loading complaints...</Typography>;
    }

    return (
        <Box>
            <Card elevation={0} sx={{ mb: 3, border: '1px solid', borderColor: 'grey.200' }}>
                <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                        <Typography variant="h6">Customer Complaints</Typography>
                        <Button variant="contained" color="primary">
                            Export Complaints
                        </Button>
                    </Box>
                    <List>
                        {complaints.map((complaint, index) => (
                            <React.Fragment key={complaint.complaintId}>
                                <ListItem alignItems="flex-start" sx={{ px: 0 }}>
                                    <ListItemText
                                        primary={
                                            <Box display="flex" alignItems="center" justifyContent="space-between">
                                                <Typography variant="subtitle1">
                                                    {complaint.title}
                                                </Typography>
                                                <Box display="flex" alignItems="center" gap={1}>
                                                    <Chip
                                                        label={complaint.status}
                                                        color={
                                                            complaint.status === 'RESOLVED' ? 'success' :
                                                                complaint.status === 'IN_PROGRESS' ? 'warning' :
                                                                    'error'
                                                        }
                                                        size="small"
                                                    />
                                                    <IconButton
                                                        size="small"
                                                        onClick={(e) => handleMenuClick(e, complaint)}
                                                    >
                                                        <MoreVertIcon />
                                                    </IconButton>
                                                </Box>
                                            </Box>
                                        }
                                        secondary={
                                            <Box mt={1}>
                                                <Typography variant="body2" color="text.secondary">
                                                    {complaint.description}
                                                </Typography>
                                                <Box mt={1} display="flex" gap={1}>
                                                    <Chip
                                                        label={`Order #${complaint.order.orderId}`}
                                                        size="small"
                                                        variant="outlined"
                                                    />
                                                    <Chip
                                                        label={complaint.order.merchantProduct.product.name}
                                                        size="small"
                                                        variant="outlined"
                                                    />
                                                </Box>
                                                <Typography
                                                    variant="caption"
                                                    color="text.secondary"
                                                    sx={{ mt: 1, display: 'block' }}
                                                >
                                                    {new Date(complaint.createdAt).toLocaleDateString()}
                                                </Typography>
                                            </Box>
                                        }
                                    />
                                </ListItem>
                                {index < complaints.length - 1 && <Divider />}
                            </React.Fragment>
                        ))}
                    </List>
                </CardContent>
            </Card>

            <Menu
                anchorEl={anchorEl}
                open={Boolean(anchorEl)}
                onClose={handleMenuClose}
            >
                <MenuItem onClick={() => handleStatusChange('IN_PROGRESS')}>
                    Mark as In Progress
                </MenuItem>
                <MenuItem onClick={() => handleStatusChange('RESOLVED')}>
                    Mark as Resolved
                </MenuItem>
                <MenuItem onClick={() => handleStatusChange('PENDING')}>
                    Mark as Pending
                </MenuItem>
            </Menu>
        </Box>
    );
} 