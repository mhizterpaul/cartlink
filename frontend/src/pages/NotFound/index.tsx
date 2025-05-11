import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router';

export default function NotFound() {
    const navigate = useNavigate();

    return (
        <Box
            sx={{
                height: '100vh',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                textAlign: 'center',
                p: 4,
            }}
        >
            <Typography variant="h1" component="h1" gutterBottom>
                404
            </Typography>
            <Typography variant="h4" gutterBottom>
                Page Not Found
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
                The page you're looking for doesn't exist or has been moved.
            </Typography>
            <Button
                variant="contained"
                color="primary"
                onClick={() => navigate('/')}
            >
                Go Home
            </Button>
        </Box>
    );
} 