import {Box, Stack, Typography, ListItemIcon, ListItem, ListItemText} from "@mui/material"

export default function Metrics(){
	return (
		<Box
		  sx={{
		    bgcolor: 'background.paper',
		    boxShadow: 1,
		    borderRadius: 1,
		    p: 2,
		    minWidth: 300,
		  }}>

		    <Stack>
		    	<Typography variant="h3">Be Who you are be different</Typography>
		    	<Box>
		    		<Typography>Be Who you are be different</Typography>
		    		<Stack>
		    			<Box>
			    			<Typography>
			    				100k
			    			</Typography>
			    			<Typography>
			    				Users
			    			</Typography>
		    			</Box>
		    			<Box>
			    			<Typography>
			    				100+
			    			</Typography>
			    			<Typography>
			    				Brands
			    			</Typography>
		    			</Box>
		    			<Box>
			    			<Typography>
			    				1M+
			    			</Typography>
			    			<Typography>
			    				Stories
			    			</Typography>
		    			</Box>
		    		</Stack>
		    	</Box>
		  	</Stack>
			<List sx={{ width: '100%', maxWidth: 360, bgcolor: 'background.paper' }}>
		      <ListItem>
		            <ListItemIcon>
		              <StarBorder />
		            </ListItemIcon>
		        <ListItemText primary="Photos" secondary="Jan 9, 2014" />
		      </ListItem>
	      	  <ListItem>
	        	<ListItemIcon>
	              <StarBorder />
	            </ListItemIcon>
	          	<ListItemText primary="Work" secondary="Jan 7, 2014" />
	      	</ListItem>
		      <ListItem>
		        <ListItemIcon>
		            <StarBorder />
		            </ListItemIcon>
		        <ListItemText primary="Vacation" secondary="July 20, 2014" />
		      </ListItem>
		      <ListItem>
		        <ListItemIcon>
		            <StarBorder />
		            </ListItemIcon>
		        <ListItemText primary="Vacation" secondary="July 20, 2014" />
		      </ListItem>
		      <ListItem>
		        <ListItemIcon>
		            <StarBorder />
		            </ListItemIcon>
		        <ListItemText primary="Vacation" secondary="July 20, 2014" />
		      </ListItem>
		      <ListItem>
		        <ListItemIcon>
		            <StarBorder />
		            </ListItemIcon>
		        <ListItemText primary="Vacation" secondary="July 20, 2014" />
		      </ListItem>
    		</List>
  		</Box>)
}