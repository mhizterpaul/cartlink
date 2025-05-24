import * as React from 'react';
import {
  Box,
  Typography,
  Button,
  Grid,
  Card,
  CardContent,
  Container,
  AppBar,
  Toolbar,
  IconButton,
  useTheme,
  useMediaQuery
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import CompareArrowsIcon from '@mui/icons-material/CompareArrows';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import StorefrontIcon from '@mui/icons-material/Storefront';
import PeopleIcon from '@mui/icons-material/People';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import NavigationDrawer from '../dashboard/components/drawer';

const navigationItems = [
  { text: 'Stores', icon: <StorefrontIcon /> },
  { text: 'Community', icon: <PeopleIcon /> },
  { text: 'Help', icon: <HelpOutlineIcon /> },
];

export default function IndexPage() {
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar
        position="fixed"
        color="default"
        elevation={0}
        sx={{
          borderBottom: `1px solid ${theme.palette.divider}`,
          backgroundColor: theme.palette.background.paper,
        }}
      >
        <Toolbar sx={{ px: { xs: 2, sm: 3, md: 4 } }}>
          {isMobile && (
            <IconButton
              color="inherit"
              aria-label="open drawer"
              edge="start"
              onClick={handleDrawerToggle}
              sx={{ mr: 2 }}
            >
              <MenuIcon />
            </IconButton>
          )}
          <Typography
            variant="h6"
            component="div"
            sx={{
              flexGrow: 1,
              fontWeight: 600,
              color: theme.palette.primary.main,
              fontSize: { xs: '1.1rem', sm: '1.25rem' },
            }}
          >
            CartLink
          </Typography>
          {!isMobile && (
            <Box sx={{ display: 'flex', gap: 4 }}>
              {navigationItems.map((item) => (
                <Button
                  key={item.text}
                  color="inherit"
                  startIcon={item.icon}
                  sx={{
                    textTransform: 'none',
                    fontWeight: 500,
                    fontSize: '1rem',
                    '&:hover': {
                      backgroundColor: 'transparent',
                      color: theme.palette.primary.main,
                    },
                  }}
                >
                  {item.text}
                </Button>
              ))}
            </Box>
          )}
        </Toolbar>
      </AppBar>

      {isMobile && (
        <NavigationDrawer
          items={navigationItems}
          open={mobileOpen}
          onClose={handleDrawerToggle}
        />
      )}

      <Toolbar /> {/* Spacer for fixed AppBar */}

      <Box sx={{ flexGrow: 1 }}>
        {/* Hero Section */}
        <Box
          sx={{
            minHeight: '90vh',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            textAlign: 'center',
            background: 'linear-gradient(135deg, #1a237e 0%, #0d47a1 100%)',
            color: 'white',
            p: { xs: 3, sm: 4, md: 6 },
          }}
        >
          <Container maxWidth="md">
            <Typography
              variant="h2"
              component="h1"
              gutterBottom
              sx={{
                fontSize: { xs: '2.5rem', sm: '3rem', md: '3.75rem' },
                fontWeight: 700,
                letterSpacing: '-0.02em',
                lineHeight: 1.2,
                mb: 3,
              }}
            >
              Shop Smarter, Save More
            </Typography>
            <Typography
              variant="h5"
              gutterBottom
              sx={{
                mb: 5,
                fontSize: { xs: '1.25rem', sm: '1.35rem', md: '1.5rem' },
                opacity: 0.9,
                fontWeight: 400,
                letterSpacing: '0.01em',
                lineHeight: 1.5,
                maxWidth: '800px',
                mx: 'auto',
              }}
            >
              Compare prices, track deals, and shop with confidence
            </Typography>
            <Button
              variant="contained"
              color="secondary"
              size="large"
              sx={{
                mt: 2,
                px: 5,
                py: 1.75,
                fontSize: '1.1rem',
                borderRadius: 2,
                textTransform: 'none',
                fontWeight: 600,
                boxShadow: '0 4px 14px 0 rgba(0,118,255,0.39)',
                '&:hover': {
                  boxShadow: '0 6px 20px 0 rgba(0,118,255,0.23)',
                },
              }}
            >
              Start Shopping
            </Button>
          </Container>
        </Box>

        {/* Features Section */}
        <Container maxWidth="lg" sx={{ py: { xs: 6, sm: 8, md: 10 } }}>
          <Typography
            variant="h3"
            component="h2"
            gutterBottom
            align="center"
            sx={{
              mb: { xs: 4, sm: 6, md: 8 },
              fontWeight: 700,
              letterSpacing: '-0.02em',
              fontSize: { xs: '2rem', sm: '2.5rem', md: '3rem' },
            }}
          >
            Why Choose CartLink?
          </Typography>
          <Grid container spacing={{ xs: 3, sm: 4, md: 6 }}>
            {[
              {
                title: 'Smart Shopping Lists',
                description: 'Create and manage your shopping lists with ease. Get price alerts and track your favorite items.',
                icon: <ShoppingCartIcon sx={{ fontSize: { xs: 50, sm: 60 }, color: 'primary.main' }} />,
              },
              {
                title: 'Price Comparison',
                description: 'Compare prices across different stores in real-time. Find the best deals and save money on every purchase.',
                icon: <CompareArrowsIcon sx={{ fontSize: { xs: 50, sm: 60 }, color: 'primary.main' }} />,
              },
              {
                title: 'Deal Alerts',
                description: 'Never miss a great deal. Get instant notifications when prices drop on items you want.',
                icon: <NotificationsActiveIcon sx={{ fontSize: { xs: 50, sm: 60 }, color: 'primary.main' }} />,
              },
            ].map((feature) => (
              <Grid size={{ xs: 12, md: 4 }} key={feature.title}>
                <Card
                  elevation={0}
                  sx={{
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    textAlign: 'center',
                    p: { xs: 2.5, sm: 3 },
                    border: '1px solid',
                    borderColor: 'divider',
                    borderRadius: 2,
                    transition: 'all 0.3s ease-in-out',
                    '&:hover': {
                      boxShadow: '0 4px 20px 0 rgba(0,0,0,0.12)',
                      transform: 'translateY(-4px)',
                    },
                  }}
                >
                  <Box sx={{ mb: 2 }}>
                    {feature.icon}
                  </Box>
                  <CardContent sx={{ p: { xs: 1, sm: 2 } }}>
                    <Typography
                      gutterBottom
                      variant="h5"
                      component="div"
                      sx={{
                        fontWeight: 600,
                        mb: 2,
                        letterSpacing: '-0.01em',
                        fontSize: { xs: '1.25rem', sm: '1.5rem' },
                      }}
                    >
                      {feature.title}
                    </Typography>
                    <Typography
                      variant="body1"
                      color="text.secondary"
                      sx={{
                        lineHeight: 1.7,
                        fontSize: { xs: '0.95rem', sm: '1rem' },
                      }}
                    >
                      {feature.description}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>
    </Box>
  );
}