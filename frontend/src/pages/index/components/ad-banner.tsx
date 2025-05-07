import * as React from 'react'
import { Box, Typography, styled } from '@mui/material'
import Avatar from '@mui/material/Avatar';
import AvatarGroup from '@mui/material/AvatarGroup';
import ImageList from 'imageList'

const StyledTextBox = styled(Box)(({ theme }) => ({
  backgroundColor: theme.palette.primary.light,
  borderRadius: '8px',
  padding: '16px',
  maxWidth: '300px',
  textAlign: 'center',
  color: theme.palette.primary.contrastText,
}));

export default function AdBanner({text}){
	return (
		<Box>
			<Stack>
			 	<StyledTextBox>
			      <Typography variant="body1">{text}</Typography>
			      <Typography variant="body1">{text}</Typography>
			      <Typography variant="body1">{text}</Typography>
			      <Box sx={{ '& > :not(style)': { m: 1 } }}>
				      <FormControl variant="standard">
				        <Input
				          id="input-with-icon-adornment"
				          startAdornment={
				            <InputAdornment position="start">
				              <EmailIcon/>
				            </InputAdornment>
				          }
				        />
				        <Button onClick= {()=> submit()} type="submit" ariant="contained">Contained</Button>
				      </FormControl>
				   </Box>
				   <AvatarGroup total={24} max={4}>
						  <Avatar alt="Remy Sharp" src="/static/images/avatar/1.jpg" />
						  <Avatar alt="Travis Howard" src="/static/images/avatar/2.jpg" />
						  <Avatar alt="Agnes Walker" src="/static/images/avatar/4.jpg" />
						  <Avatar alt="Trevor Henderson" src="/static/images/avatar/5.jpg" />
						  <Typography>"Total Users"</Typography>
				   </AvatarGroup>
			    </StyledTextBox>
			    <ImageList />
    		</Stack>
    		<Stack direction="row" spacing={2}>
    			customerLogos.map(()=>{
    				<Icon/>
    			})
    		</Stack>
    	</Box>
  );
}