import {styled} from "@mui/material/styles"
import EastRoundedIcon from '@mui/icons-material/EastRounded';
import Line from "common/Line"


export default function(){
	return(
		<Stack>
			<imageList />
			<Stack sx={{alignSelf: {
				[theme.breakpoints.down('md')]: "flex-start"
			} }}>
				<Typography> <Line /> Benefits</Typography>
				<Typography variant="h3"> Benefits</Typography>
				<ol style={{listStyleType: "✦"}}>
					<li>
						Embrace your unique beauty, be true to yourself and never be afraid to show the world who you truly are
					</li>
					<li>
						Develop your own sense of style by experimenting, taking inspiration from others and staying true to who you are. its not about fitting in, its about standing out
					</li>
				</ol>
				<Button variant="contained" EndIcon={<EastRoundedIcon/>}>Contained</Button>
			</Stack>
		</Stack>
	)
}