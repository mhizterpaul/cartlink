import * as React from 'react';
import { styled } from '@mui/material/styles';
import MuiDrawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';
import { useTheme } from '@mui/material/styles';
import Toolbar from '@mui/material/Toolbar';

export interface DrawerItem {
  text: string;
  icon?: React.ReactNode;
  onClick?: () => void;
}

export interface DrawerProps {
  items: DrawerItem[];
  open: boolean;
  onClose: () => void;
  children?: React.ReactNode;
}

const StyledDrawer = styled(MuiDrawer)(({ theme }) => ({
  '& .MuiDrawer-paper': {
    boxSizing: 'border-box',
    width: 240,
    backgroundColor: theme.palette.background.paper,
    borderRight: `1px solid ${theme.palette.divider}`,
  },
}));

export default function NavigationDrawer({ items = [], open, onClose, children }: DrawerProps) {
  const theme = useTheme();

  const drawerContent = (
    <>
      <Toolbar /> {/* Spacer for AppBar */}
      {children}
      <List>
        {items?.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              onClick={item.onClick}
              sx={{
                py: 1.5,
                '&:hover': {
                  backgroundColor: theme.palette.action.hover,
                },
              }}
            >
              {item.icon && (
                <ListItemIcon sx={{
                  minWidth: 40,
                  color: theme.palette.primary.main
                }}>
                  {item.icon}
                </ListItemIcon>
              )}
              <ListItemText
                primary={item.text}
                primaryTypographyProps={{
                  fontSize: '1rem',
                  fontWeight: 500,
                }}
              />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </>
  );

  return (
    <StyledDrawer
      open={open}
      onClose={onClose}
      ModalProps={{
        keepMounted: true, // Better open performance on mobile
      }}
    >
      {drawerContent}
    </StyledDrawer>
  );
} 