import * as React from 'react';
import AppBarWithDrawer from '@/components/AppBarWithDrawer';
import { Box, Typography, Button, Grid, Card, CardContent, CardMedia } from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import InfoIcon from '@mui/icons-material/Info';
import ContactMailIcon from '@mui/icons-material/ContactMail';

const drawerItems = [
  { text: 'Home', icon: <HomeIcon /> },
  { text: 'About', icon: <InfoIcon /> },
  { text: 'Contact', icon: <ContactMailIcon /> },
];

export default function IndexPage() {
  return (
    <AppBarWithDrawer title="CartLink" drawerItems={drawerItems}>
      <Box sx={{ flexGrow: 1 }}>
        {/* Hero Section */}
        <Box
          sx={{
            height: '80vh',
            display: 'flex',
            flexDirection: 'column',
            justifyContent: 'center',
            alignItems: 'center',
            textAlign: 'center',
            background: 'linear-gradient(45deg, #2196F3 30%, #21CBF3 90%)',
            color: 'white',
            p: 4,
          }}
        >
          <Typography variant="h2" component="h1" gutterBottom>
            Welcome to CartLink
          </Typography>
          <Typography variant="h5" gutterBottom>
            Your Ultimate Shopping Companion
          </Typography>
          <Button
            variant="contained"
            color="secondary"
            size="large"
            sx={{ mt: 4 }}
          >
            Get Started
          </Button>
        </Box>

        {/* Features Section */}
        <Box sx={{ p: 4 }}>
          <Typography variant="h4" component="h2" gutterBottom align="center">
            Features
          </Typography>
          <Grid container spacing={4} sx={{ mt: 2 }}>
            {[
              {
                title: 'Smart Shopping Lists',
                description: 'Create and manage your shopping lists with ease',
                image: '/images/shopping-list.jpg',
              },
              {
                title: 'Price Comparison',
                description: 'Compare prices across different stores',
                image: '/images/price-comparison.jpg',
              },
              {
                title: 'Deal Alerts',
                description: 'Get notified about the best deals',
                image: '/images/deal-alerts.jpg',
              },
            ].map((feature) => (
              <Grid item xs={12} sm={4} key={feature.title}>
                <Card>
                  <CardMedia
                    component="img"
                    height="200"
                    image={feature.image}
                    alt={feature.title}
                  />
                  <CardContent>
                    <Typography gutterBottom variant="h5" component="div">
                      {feature.title}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {feature.description}
                    </Typography>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        </Box>
      </Box>
    </AppBarWithDrawer>
  );
}