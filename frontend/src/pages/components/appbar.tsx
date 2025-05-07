// This file requires React and @mui/material to be installed in your project.
import * as React from 'react';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import Box from '@mui/material/Box';

interface AppBarProps {
  title: string;
  onMenuClick?: () => void;
  actions?: React.ReactNode;
}

const CustomAppBar: React.FC<AppBarProps> = ({ title, onMenuClick, actions }: AppBarProps) => (
  <AppBar position="fixed">
    <Toolbar>
      {onMenuClick && (
        <IconButton
          color="inherit"
          aria-label="open drawer"
          edge="start"
          onClick={onMenuClick}
          sx={{ mr: 2, display: { sm: 'none' } }}
        >
          <MenuIcon />
        </IconButton>
      )}
      <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
        {title}
      </Typography>
      {actions && <Box>{actions}</Box>}
    </Toolbar>
  </AppBar>
);

export default CustomAppBar; 