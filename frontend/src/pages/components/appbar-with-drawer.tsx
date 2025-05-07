import * as React from 'react';
import Box from '@mui/material/Box';
import Toolbar from '@mui/material/Toolbar';
import Drawer from '@mui/material/Drawer';
import { useTheme } from '@mui/material/styles';
import useMediaQuery from '@mui/material/useMediaQuery';
import CustomAppBar from './appbar';
import CustomDrawer, { DrawerItem } from './drawer';

const drawerWidth = 240;

interface AppBarWithDrawerProps {
  title: string;
  drawerItems: DrawerItem[];
  children: React.ReactNode;
  window?: () => Window;
}

const Main: React.FC<{ open?: boolean; children: React.ReactNode }> = ({ open, children }) => (
  <main
    style={{
      flexGrow: 1,
      padding: 24,
      transition: 'margin 225ms cubic-bezier(0.4, 0, 0.6, 1) 0ms',
      marginLeft: open ? 0 : -drawerWidth,
    }}
  >
    <Toolbar />
    {children}
  </main>
);

const AppBarWithDrawer: React.FC<AppBarWithDrawerProps> = ({ title, drawerItems, children, window }) => {
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const container = window !== undefined ? () => window().document.body : undefined;

  return (
    <Box sx={{ display: 'flex' }}>
      <CustomAppBar title={title} onMenuClick={handleDrawerToggle} />
      <Box component="nav" sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}>
        <Drawer
          container={container}
          variant={isMobile ? 'temporary' : 'permanent'}
          open={isMobile ? mobileOpen : true}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          <CustomDrawer items={drawerItems} title={title} onClose={isMobile ? handleDrawerToggle : undefined} />
        </Drawer>
      </Box>
      <Main open={!isMobile}>{children}</Main>
    </Box>
  );
};

export default AppBarWithDrawer; 