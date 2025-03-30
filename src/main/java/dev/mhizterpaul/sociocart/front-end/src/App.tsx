import * as React from "react";
import { BrowserRouter, Navigate, Routes, Route } from "react-router";
import logo from './logo.svg';
import './App.css';
import Homepage from './pages/homepage'
import '@fontsource/roboto';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import { green, purple } from '@mui/material/colors';

const theme = createTheme({
  palette: {
    primary: {
      main: purple[500],
    },
    secondary: {
      main: green[500],
    },
  },
});

function App() {
  return (
    <BrowserRouter>
      <Routes>
      <Route element={<ThemeProvider theme={theme} />}>
        <Route path="/" element={<Homepage />} />
        <Route path="*" element={<Navigate to="/404" />} />
      </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
