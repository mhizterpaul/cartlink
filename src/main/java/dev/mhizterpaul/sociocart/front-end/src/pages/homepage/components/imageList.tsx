import {ImageList as Mui_ImageList, ImageListItem} from '@mui/material'

export default function ImageList(){
	return (
		<Mui_ImageList sx={{maxWidth: 520;}}>
			<ImageListItem key={item.img} cols={cols} rows={rows}>
	            <img
	              {...srcset(item.img, 250, 200, rows, cols)}
	              alt={item.title}
	              loading="lazy"
	            />
	        </ ImageListItem>
	        <ImageListItem key={item.img} cols={cols} rows={rows}>
	            <img
	              {...srcset(item.img, 250, 200, rows, cols)}
	              alt={item.title}
	              loading="lazy"
	            />
	         </ ImageListItem>
		</Mui_ImageList>)
}