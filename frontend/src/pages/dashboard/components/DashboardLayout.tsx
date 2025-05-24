import * as React from 'react';
import {
    Box,
    Typography,
    AppBar,
    Toolbar,
    IconButton,
    useTheme,
    useMediaQuery,
    Avatar,
    InputBase,
    Menu,
    MenuItem,
    ListItemIcon,
    ListItemText,
} from '@mui/material';
import DashboardIcon from '@mui/icons-material/Dashboard';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import PeopleIcon from '@mui/icons-material/People';
import SettingsIcon from '@mui/icons-material/Settings';
import ListAltIcon from '@mui/icons-material/ListAlt';
import InventoryIcon from '@mui/icons-material/Inventory';
import NavigationDrawer from './drawer';
import { Link } from 'react-router';
import MenuIcon from "@mui/icons-material/Menu";
import SearchIcon from "@mui/icons-material/Search";
import NotificationsIcon from "@mui/icons-material/Notifications";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import CloudDownloadIcon from "@mui/icons-material/CloudDownload";
import LogoutIcon from "@mui/icons-material/Logout";

const drawerWidth = 240;

const navigationItems = [
    { text: 'Dashboard', icon: <DashboardIcon />, name: "dashboard" },
    { text: 'Customers', icon: <PeopleIcon />, name: "customers" },
    { text: 'Orders', icon: <ShoppingCartIcon />, name: "orders" },
    { text: 'Wallet', icon: <ListAltIcon />, name: "wallet" },
    { text: 'Inventory', icon: <InventoryIcon />, name: "inventory" },
    { text: 'Coupons', icon: <SettingsIcon />, name: "coupons" },
];

interface DashboardLayoutProps {
    children: React.ReactNode;
    onSelect: (name: string) => void;
    selected: string;
}

export default function DashboardLayout({ children, onSelect, selected }: DashboardLayoutProps) {
    const [mobileOpen, setMobileOpen] = React.useState(false);
    const theme = useTheme();
    const isMobile = useMediaQuery(theme.breakpoints.down('md'));
    const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
    const open = Boolean(anchorEl);

    const handleAvatarClick = (event: React.MouseEvent<HTMLElement>) => {
        setAnchorEl(event.currentTarget);
    };
    const handleDrawerToggle = () => {
        setMobileOpen(!mobileOpen);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };


    return (
        <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'grey.50' }}>
            <AppBar
                position="fixed"
                sx={{
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` },
                    backgroundColor: theme.palette.background.paper,
                    color: theme.palette.text.primary,
                    boxShadow: 'none',
                    borderBottom: `1px solid ${theme.palette.divider}`,
                }}
            >
                <Toolbar>
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
                    <Typography variant="h6" sx={{ fontWeight: 700, mr: 3, color: 'primary.main', display: { xs: 'none', md: 'block' } }}>
                        Sasrio.
                    </Typography>
                    <Box sx={{ flexGrow: 1, display: 'flex', alignItems: 'center' }}>
                        <Box
                            sx={{
                                display: 'flex',
                                alignItems: 'center',
                                backgroundColor: theme.palette.grey[100],
                                borderRadius: 1,
                                px: 2,
                                py: 0.5,
                                width: { xs: '100%', sm: 350 },
                            }}
                        >
                            <SearchIcon sx={{ color: theme.palette.grey[500], mr: 1 }} />
                            <InputBase
                                placeholder="Search Customers, Orders, Products"
                                sx={{ flex: 1, fontSize: '0.95rem' }}
                            />
                        </Box>
                    </Box>
                    <IconButton color="inherit">
                        <NotificationsIcon />
                    </IconButton>
                    <IconButton onClick={handleAvatarClick} sx={{ ml: 2 }}>
                        <Avatar sx={{ bgcolor: "primary.main", width: 36, height: 36 }}>A</Avatar>
                    </IconButton>
                    <Menu
                        anchorEl={anchorEl}
                        open={open}
                        onClose={handleClose}
                        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
                        transformOrigin={{ vertical: "top", horizontal: "center" }}
                    >
                        <Box px={2} py={1.5} display="flex" alignItems="center">
                            <Avatar
                                src="/mnt/data/original-75820e458a8237cd396ee5e4e44e4ec6.jpg"
                                sx={{ width: 40, height: 40, mr: 1.5 }}
                            />
                            <Box>
                                <Typography fontWeight={600}>Michelle Lewis</Typography>
                                <Typography variant="body2" color="text.secondary">
                                    mwillis@sdl.com
                                </Typography>
                            </Box>
                        </Box>
                        <MenuItem onClick={handleClose}>
                            <ListItemIcon>
                                <AccountCircleIcon fontSize="small" />
                            </ListItemIcon>
                            <ListItemText>Manage Account</ListItemText>
                        </MenuItem>
                        <MenuItem onClick={handleClose}>
                            <ListItemIcon>
                                <CloudDownloadIcon fontSize="small" />
                            </ListItemIcon>
                            <ListItemText>Product Updates</ListItemText>
                        </MenuItem>
                        <MenuItem onClick={handleClose}>
                            <ListItemIcon>
                                <LogoutIcon fontSize="small" />
                            </ListItemIcon>
                            <ListItemText>Sign out</ListItemText>
                        </MenuItem>
                    </Menu>
                </Toolbar>
            </AppBar>

            <NavigationDrawer
                items={navigationItems.map(item => ({
                    ...item,
                    component: Link,
                }))}
                open={isMobile ? mobileOpen : true}
                variant={isMobile ? "temporary" : "permanent"}
                onClose={handleDrawerToggle}
                onSelect={onSelect}
                selected={selected}
            >
                <Box sx={{ px: 2, py: 3, display: { xs: 'block', md: 'none' } }}>
                    <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>Sasrio.</Typography>
                </Box>
            </NavigationDrawer>

            <Box
                component="main"
                sx={{
                    flexGrow: 1,
                    p: { xs: 1, sm: 2, md: 3 },
                    width: { md: `calc(100% - ${drawerWidth}px)` },
                    ml: { md: `${drawerWidth}px` },
                }}
            >
                <Toolbar />
                {children}
            </Box>
        </Box>
    );
} 