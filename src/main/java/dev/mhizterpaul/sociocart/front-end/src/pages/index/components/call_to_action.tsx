import shop4 from "../original-store4.jpg"

export default function CallToAction(){
	return ({
		<Box>
			<Stack gap>
				<Typography>Best Feature</Typography>
				<Typography>Build your own taste of beauty</Typography>
				<Typography>we launch CURIOUS ME for you to build your own identity</Typography>
			</Stack>
			<Stack direction={"row"}>
				<Typography>Curious You</Typography>
				<img
	              {...srcset(shop4, 250, 200, rows, cols)}
	              alt={item.title}
	              loading="lazy"
	            />
				<Typography>Build your unique and Beauty Story</Typography/>
			</Stack>
		</Box>
	})
}