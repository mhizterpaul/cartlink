import InstagramIcon from '@mui/icons-material/Instagram';
import FacebookIcon from '@mui/icons-material/Facebook';
import XIcon from '@mui/icons-material/X';
import YouTubeIcon from '@mui/icons-material/YouTube';

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
			 		<Typography>
			 			Our Services
			 		</Typography>
			 		<Typography>
			 			Help center
			 		</Typography>
			 		<Typography>
			 			FAQ
			 		</Typography>
			 		<Typography>
			 			transaction	
			 		</Typography>
			 		<Typography>
			 			Investation
			 		</Typography>
			 	</Stack>
			 	<Stack>
			 		<Typography>
			 			Company
			 		</Typography>
			 		<Typography>
			 			About Us
			 		</Typography>
			 		<Typography>
			 			Career
			 		</Typography>
			 		<Typography>
			 			Management	
			 		</Typography>
			 		<Typography>
			 			Blog
			 		</Typography>
			 	</Stack>
			 	<Stack>
			 		<Typography>
			 			Get the latest information from us
			 		</Typography>
			 		<Box>
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
			 		<Typography>
			 			Join social platform
			 		</Typography>
			 		<Stack direction="row">
			 			<InstagramIcon />
			 			<FacebookIcon />
			 			<XIcon />
			 			<YouTubeIcon />
			 		</Stack>
			 	</Stack>
			 </Stack>
			 <Typography>
			 	©Copyright 2025 RopBop. All right reserved
			 </Typography>
		</Footer>
		)