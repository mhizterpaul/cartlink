import * as React from 'react';
import Box from '@mui/material/Box';
import Typography from '@mui/material/Typography';
import Divider from '@mui/material/Divider';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemIcon from '@mui/material/ListItemIcon';
import ListItemText from '@mui/material/ListItemText';

// This file requires React and @mui/material to be installed in your project.

export interface DrawerItem {
  text: string;
  icon?: React.ReactNode;
  onClick?: () => void;
}

interface CustomDrawerProps {
  items: DrawerItem[];
  title?: string;
  onClose?: () => void;
}

const CustomDrawer: React.FC<CustomDrawerProps> = ({ items, title = '', onClose }: CustomDrawerProps) => (
  <Box onClick={onClose} sx={{ textAlign: 'center' }}>
    {title && (
      <Typography variant="h6" sx={{ my: 2 }}>
        {title}
      </Typography>
    )}
    <Divider />
    <List>
      {items.map((item) => (
        <ListItem key={item.text} disablePadding>
          <ListItemButton onClick={item.onClick} sx={{ textAlign: 'center' }}>
            {item.icon && <ListItemIcon>{item.icon}</ListItemIcon>}
            <ListItemText primary={item.text} />
          </ListItemButton>
        </ListItem>
      ))}
    </List>
  </Box>
);

export default CustomDrawer;