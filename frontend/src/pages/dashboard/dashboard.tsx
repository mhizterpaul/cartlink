import * as React from 'react';
import AppBarWithDrawer from '@/components/AppBarWithDrawer';
import { Box, Typography, Grid, Card, CardContent, CardHeader, IconButton } from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import HistoryIcon from '@mui/icons-material/History';
import SettingsIcon from '@mui/icons-material/Settings';
import MoreVertIcon from '@mui/icons-material/MoreVert';

const drawerItems = [
  { text: 'Dashboard', icon: <DashboardIcon /> },
  { text: 'Shopping Lists', icon: <ShoppingCartIcon /> },
  { text: 'History', icon: <HistoryIcon /> },
  { text: 'Settings', icon: <SettingsIcon /> },
];

export default function DashboardPage() {
  return (
    <AppBarWithDrawer title="Dashboard" drawerItems={drawerItems}>
      <Box sx={{ flexGrow: 1, p: 3 }}>
        <Grid container spacing={3}>
          {/* Active Lists Card */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader
                title="Active Shopping Lists"
                action={
                  <IconButton aria-label="settings">
                    <MoreVertIcon />
                  </IconButton>
                }
              />
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  You have 3 active shopping lists
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          {/* Recent Activity Card */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader
                title="Recent Activity"
                action={
                  <IconButton aria-label="settings">
                    <MoreVertIcon />
                  </IconButton>
                }
              />
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  Your recent shopping activities will appear here
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          {/* Price Alerts Card */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader
                title="Price Alerts"
                action={
                  <IconButton aria-label="settings">
                    <MoreVertIcon />
                  </IconButton>
                }
              />
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  You have 5 active price alerts
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          {/* Statistics Card */}
          <Grid item xs={12} md={6}>
            <Card>
              <CardHeader
                title="Shopping Statistics"
                action={
                  <IconButton aria-label="settings">
                    <MoreVertIcon />
                  </IconButton>
                }
              />
              <CardContent>
                <Typography variant="body2" color="text.secondary">
                  View your shopping statistics and trends
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      </Box>
    </AppBarWithDrawer>
  );
} 