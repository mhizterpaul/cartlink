import React form 'react'
import Container from '../../components/container'
import AppBar from '../../components/appbar' 
import Stack from '@mui/material/Stack';
import StackItems from 'components';
import { ThemeProvider, createTheme } from '@mui/material/styles';


const theme = createTheme({
  palette: {
    primary: {
      main: purple[500],
    },
    secondary: {
      main: green[500],
    },
  },
  components: {
    MuiStack: {
      defaultProps: {
        useFlexGap: true,
        direction:"row",
        spacing: 2,
        sx:{{ flexWrap: 'wrap' }}
      },
    },
  },
});

export default function Homepage(){
	return (	
		<ThemeProvider  theme={theme}>
			<Stack>
				<StackItems.AdBanner />
				<StackItems.Benefits />
				<StackItems.Metrics />
				<StackItems.BestFeature />
				<StackItems.Testimonials />
				<StackItems.Footer />
			</Stack>
		</ThemeProvider>
	)
}