import Line from "common/Line"
import ArrowCircleLeftTwoToneIcon from '@mui/icons-material/ArrowCircleLeftTwoTone';
import ArrowCircleRightTwoToneIcon from '@mui/icons-material/ArrowCircleRightTwoTone';
import ArrowCircleLeftOutlinedIcon from '@mui/icons-material/ArrowCircleLeftOutlined';
import ArrowCircleLeftOutlinedIcon from '@mui/icons-material/ArrowCircleRightOutlined';
import ArrowCircleRightIcon from '@mui/icons-material/ArrowCircleRight';
import ArrowCircleRightIcon from '@mui/icons-material/ArrowCircleLeft';

export default function Footer(){
	return(
		<Box>
			<Stack>
				<header>
					<Typography><Line /> Testimonials</Typography>
					<Typography><Line /> Testimonials</Typography>
				</header>
				<Box>
					<IconButton aria-label="delete" disabled color="primary">
					  <ArrowCircleLeftTwoToneIcon />
					</IconButton>
					<IconButton color="secondary" aria-label="add an alarm">
					  <ArrowCircleRightTwoToneIcon />
					</IconButton>	
				</Box>
			</Stack>
			<Stack>
				testimonials.map(()=>(

					 <Card sx={{ maxWidth: 345 }}>
				      <CardContent>
				        <Typography gutterBottom variant="h5" component="div">
				          Lizard
				        </Typography>
				        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
				          Lizards are a widespread group of squamate reptiles, with over 6,000
				          species, ranging across all continents except Antarctica
				        </Typography>
				      </CardContent>
				      <Divider orientation="vertical" flexItem />
				      <CardActions disableSpacing>
				        <Avatar sx={{ bgcolor: red[500] }} aria-label="recipe">
				            R
				        </Avatar>
				        <Box>
							<Typography>
								Name
							</Typography>
							<Typography>
								City
							</Typography>
				        </Box> 
				        <Box sx={{marginLeft: 'auto'}}>
					        <IconButton
					          expand={expanded}
					          onClick={handleExpandClick}
					          aria-expanded={expanded}
					          aria-label="show more"
					        >
					          <ExpandMoreIcon />
					          <Typography>
					          {45}
					          </Typography>
					        </IconButton>
					     </Box>
				      </CardActions>
				    </Card>))
			</Stack>
		</Box>
		)
}