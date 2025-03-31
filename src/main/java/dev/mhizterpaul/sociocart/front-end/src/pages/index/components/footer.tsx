export default function(){
	return(
		<Footer>
			 <Divider orientation="vertical" flexItem />
			 <Stack>
			 	<Stack>
			 		<header>
			 			<img {...srcset(shop4, 250, 200, rows, cols)}
			              alt={item.title}
			              loading="lazy"
			            />
			            <Typography>
			            	RopBop	
			            </Typography>
			        </header>
			        <Typography>
			        	RopBop leverages a commerce-focused variant of the transformer architecture
			        	to deliver highly personalized product recommendation and truly intelligent shopping experience 
			        </Typography>
			        <Box>
			        	<Button variant="outlined" startIcon={<DeleteIcon />}>
			        		<Typography>Coming Soon </Typography>
			        		<Typography>App Store</Typography>
			        	</Button>
			        	<Button variant="outlined" startIcon={<DeleteIcon />}>
			        		<Typography>Coming Soon </Typography>
			        		<Typography>Play Store</Typography>
			        	</Button>
			        </Box>
			 	</Stack>
			 	<Stack>
			 	</Stack>
			 	<Stack>
			 	</Stack>
			 	<Stack>
			 	</Stack>
			 </Stack>
			 <Typography>
			 	©Copyright 2025 RopBop. All right reserved
			 </Typography>
		</Footer>
		)
}