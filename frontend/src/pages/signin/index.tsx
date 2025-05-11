import React from 'react';
import {
    Box,
    Button,
    Paper,
    TextField,
    Typography,
    Divider,
} from '@mui/material';

const SignInPage: React.FC = () => {
    return (
        <Box
            sx={{
                minHeight: '100vh',
                background: '#eaf3fa',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
            }}
        >
            <Paper
                elevation={0}
                sx={{
                    width: { xs: '100%', md: 900 },
                    height: { xs: '100%', md: 540 },
                    display: 'flex',
                    borderRadius: 3,
                    overflow: 'hidden',
                }}
            >
                {/* Left Side */}
                <Box
                    sx={{
                        background: '#286086',
                        color: '#fff',
                        width: '50%',
                        display: { xs: 'none', md: 'flex' },
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        px: 6,
                        py: 8,
                    }}
                >
                    <Box sx={{ width: '100%', textAlign: 'center' }}>
                        <Typography variant="h5" sx={{ fontWeight: 600, mb: 2 }}>
                            Become A Merchant
                        </Typography>
                        <Typography variant="body1" sx={{ mb: 3, opacity: 0.9 }}>
                            Register to become a Merchant on the Zone POS platform. Register and Upload all necessary requirements without leaving your comfort zone.
                        </Typography>
                        <Button
                            variant="outlined"
                            sx={{
                                color: '#fff',
                                borderColor: '#fff',
                                borderRadius: 10,
                                px: 4,
                                py: 1.2,
                                mb: 5,
                                fontWeight: 500,
                                fontSize: 16,
                                textTransform: 'none',
                                '&:hover': {
                                    borderColor: '#fff',
                                    background: 'rgba(255,255,255,0.08)',
                                },
                            }}
                        >
                            Merchant Registration
                        </Button>
                        <Divider sx={{ background: 'rgba(255,255,255,0.2)', my: 4 }} />
                        <Typography variant="h6" sx={{ fontWeight: 500, mb: 2 }}>
                            Create your Merchant Profile
                        </Typography>
                        <Typography variant="body2" sx={{ mb: 3, opacity: 0.9 }}>
                            Recieved your Merchant Id? <br />
                            Create your merchant profile and start customizing your Admin for better point of sale experience including active Loyalty Programme.
                        </Typography>
                        <Button
                            variant="outlined"
                            sx={{
                                color: '#fff',
                                borderColor: '#fff',
                                borderRadius: 10,
                                px: 4,
                                py: 1.2,
                                fontWeight: 500,
                                fontSize: 16,
                                textTransform: 'none',
                                '&:hover': {
                                    borderColor: '#fff',
                                    background: 'rgba(255,255,255,0.08)',
                                },
                            }}
                        >
                            Create Merchant Profile
                        </Button>
                    </Box>
                </Box>
                {/* Right Side */}
                <Box
                    sx={{
                        width: { xs: '100%', md: '50%' },
                        background: '#fff',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        justifyContent: 'center',
                        px: { xs: 3, md: 8 },
                        py: { xs: 6, md: 0 },
                    }}
                >
                    <Box sx={{ width: '100%', maxWidth: 340, textAlign: 'center' }}>
                        {/* Logo */}
                        <Box sx={{ mb: 3, mt: -2 }}>
                            <img
                                src="/logo.svg"
                                alt="Zone POS Logo"
                                style={{ height: 48, marginBottom: 8 }}
                                onError={e => (e.currentTarget.style.display = 'none')}
                            />
                            <Typography
                                variant="h4"
                                sx={{
                                    fontWeight: 700,
                                    color: '#e08a2e',
                                    fontFamily: 'inherit',
                                    letterSpacing: 2,
                                    mb: 1,
                                }}
                            >
                                ZONE
                            </Typography>
                        </Box>
                        <Typography variant="h5" sx={{ fontWeight: 600, mb: 1 }}>
                            Sign In to Zone POS
                        </Typography>
                        <Typography variant="body2" sx={{ color: '#888', mb: 3 }}>
                            You need to have registered and verified as merchant, before you can proceed.
                        </Typography>
                        <Box component="form" sx={{ width: '100%' }}>
                            <TextField
                                fullWidth
                                label="Username"
                                variant="outlined"
                                sx={{ mb: 2, background: '#fafbfc' }}
                                InputProps={{ style: { borderRadius: 10 } }}
                            />
                            <TextField
                                fullWidth
                                label="Password"
                                type="password"
                                variant="outlined"
                                sx={{ mb: 3, background: '#fafbfc' }}
                                InputProps={{ style: { borderRadius: 10 } }}
                            />
                            <Button
                                type="submit"
                                fullWidth
                                variant="contained"
                                sx={{
                                    background: 'linear-gradient(90deg, #e08a2e 0%, #e08a2e 100%)',
                                    color: '#fff',
                                    borderRadius: 10,
                                    fontWeight: 600,
                                    fontSize: 18,
                                    py: 1.5,
                                    textTransform: 'none',
                                    boxShadow: 'none',
                                    '&:hover': {
                                        background: '#d07a1e',
                                    },
                                }}
                            >
                                Sign In
                            </Button>
                        </Box>
                    </Box>
                </Box>
            </Paper>
        </Box>
    );
};

export default SignInPage;
