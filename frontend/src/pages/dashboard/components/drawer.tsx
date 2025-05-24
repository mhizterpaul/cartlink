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
  component?: React.ElementType;
  name?: string; // e.g. 'inventory', 'orders', etc.
}

export interface DrawerProps {
  items: DrawerItem[];
  open: boolean;
  variant: "temporary" | "permanent";
  onClose: () => void;
  children?: React.ReactNode;
  selected?: string;
  onSelect?: (name: string) => void;
}

const StyledDrawer = styled(MuiDrawer)(({ theme }) => ({
  '& .MuiDrawer-paper': {
    boxSizing: 'border-box',
    width: 240,
    backgroundColor: theme.palette.background.paper,
    borderRight: `1px solid ${theme.palette.divider}`,
  },
}));

export default function NavigationDrawer({ items = [], open, onClose, variant, children, selected, onSelect }: DrawerProps) {
  const theme = useTheme();

  const drawerContent = (
    <>
      <Toolbar /> {/* Spacer for AppBar */}
      {children}
      <List>
        {items?.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              component={item.component || 'div'}
              onClick={() => {
                if (onSelect && item.name) onSelect(item.name);
                if (item.onClick) item.onClick();
              }}
              selected={selected === item.name}
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
      variant={variant}
      ModalProps={{
        keepMounted: true, // Better open performance on mobile
      }}
    >
      {drawerContent}
    </StyledDrawer>
  );
} 