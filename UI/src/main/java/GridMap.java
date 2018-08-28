import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;

/*
* GridMap.java
*
* Provides utilities for representing a grid map posessing pixels of varying levels
* 
* Authors: SEP UG02
*/
public class GridMap {
	private int width;
	private int height;
	private float scale; // cells per m
	private float xoffset;
	private float yoffset;
	private float map_zoom;
	private int minimum_mappable_x;
	private int minimum_mappable_y;
	private int maximum_mappable_x;
	private int maximum_mappable_y;
	
	private BufferedImage cells;
	private BufferedImage NGZ;
	
	private BufferedImage rendered;
	private boolean outdated;
	private boolean zoomed;
	
	// Scaled Variables
	private int scaled_top_left_y;
	private int scaled_top_left_x;
	private int scaled_bottom_right_y;
	private int scaled_bottom_right_x;
	private int scaled_rover_x;
	private int scaled_rover_y;
	private int scaled_rover_start_x;
	private int scaled_rover_start_y;
	
	// Real World Variables
	private int panel_width;
	private int panel_height;
	private int top_left_y;
	private int top_left_x;
	private int bottom_right_y;
	private int bottom_right_x;
	private double rover_x;
	private double rover_y;
	
	public GridMap(int width, int height, float scale, int panel_width, int panel_height) {
		
		this.width = width;
		this.height = height;
		this.scale = scale;
		this.panel_width = panel_width;
		this.panel_height = panel_height;
		
		// minimum mappable area with an indented buffer zone
		minimum_mappable_x = -Math.round(((width+1)/2)*scale);
		minimum_mappable_y = -Math.round(((height+1)/2)*scale);
		maximum_mappable_x = Math.round(((width-1)/2)*scale);
		maximum_mappable_y = Math.round(((height-1)/2)*scale);
		
		map_zoom = 1;
		xoffset = width/2;
		yoffset = height/2;
		
		cells = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		NGZ = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		// Initialise all cells to null 
		for(int x = 0; x < width; x++ ) {
			for( int y = 0; y < height; y++ ) {
				cells.setRGB(x,y,Colour.LIGHT_GRAY);
			}
		}
		
		rover_x = 0;
		rover_y = 0;
		top_left_x = -Math.round(panel_width/2f);
		top_left_y = -Math.round(panel_height/2f);
		bottom_right_x = Math.round(panel_width/2f);
		bottom_right_y = Math.round(panel_height/2f);
		
		scaled_rover_x = Math.round(xoffset);
		scaled_rover_y = Math.round(yoffset);
		
		// Assume rover started at 0,0
		scaled_rover_start_x = scaled_rover_x;
		scaled_rover_start_y = scaled_rover_x;
		
		scaled_top_left_x = Math.round((top_left_x/scale)+xoffset);
		scaled_top_left_y = Math.round((top_left_y/scale)+yoffset);
		scaled_bottom_right_x = Math.round((bottom_right_x/scale)+xoffset);
		scaled_bottom_right_y = Math.round((bottom_right_y/scale)+yoffset);
		
		ColorModel cm = cells.getColorModel();
		BufferedImage copy_map = new BufferedImage(cm, cells.copyData(null), cm.isAlphaPremultiplied(), null);
		copy_map.setRGB(scaled_rover_x,scaled_rover_y,0x800080);
		rendered = copy_map.getSubimage(scaled_top_left_x, scaled_top_left_y, (scaled_bottom_right_x-scaled_top_left_x), (scaled_bottom_right_y-scaled_top_left_y));
		outdated = false;
		zoomed = false;
		
	}
	
	/*
	 * Reset Map
	 */
	public void resetMap() {
		for(int x = 0; x < width; x++ ) {
			for( int y = 0; y < height; y++ ) {
				cells.setRGB(x,y,Colour.LIGHT_GRAY);
				NGZ.setRGB(x,y,0x00000000);
			}
		}
	}
	
	/*
	 * @return: how man cells in horizontal direction
	 */
	public int getWidth() {
		return width;
	}
	
	/*
	 * @return: how many cells in vertical direction
	 */
	public int getHeight() {
		return height;
	}
	
	/*
	 * @return: how man cells per metre 
	 */
	public long getScale() {
		return Math.round(scale);
	}
	
	/*
	 * Set the value of the cell at x, y 
	 * 
	 * @param x: the x coordinate of the cell
	 * @param y: the y coordinate of the cell
	 * @param colour: the colour you want to set cell to
	 */
	public void setValue(double x, double y, int colour, boolean rover_position) {	
		// Check if rover has moved
		int scaled_x = (int)Math.round((x/scale)+xoffset);
		int scaled_y = (int)Math.round((y/scale)+yoffset);

		if(rover_position) {
			rover_x = x;
			rover_y = y;
			if(scaled_x == scaled_rover_x && scaled_y == scaled_rover_y) outdated = true;
			scaled_rover_x = scaled_x;
			scaled_rover_y = scaled_y;
		}
		
		if(scaled_x < 0 || scaled_x >= width || scaled_y < 0 || scaled_y >= height) return;
		int currentColour = cells.getRGB(scaled_x ,scaled_y) & 0xFFFFFF;
		if(currentColour == Colour.LIGHT_GRAY || currentColour == Colour.WHITE) {
			cells.setRGB(scaled_x,scaled_y,colour);
		}
	}
	
	public void setRoverLanding(double x, double y) {	
		// Check if rover has moved
		scaled_rover_start_x = (int)Math.round((x/scale)+xoffset);
		scaled_rover_start_y = (int)Math.round((y/scale)+yoffset);
	}
	
	/**
	 * Sends NGZ coordinates.
	 */
	public void addNGZ(double start_x, double start_y, double end_x, double end_y, RoverController controller) {
		start_x = top_left_x+(start_x*map_zoom);
		start_y = top_left_y+(start_y*map_zoom);
		end_x = top_left_x+(end_x*map_zoom);
		end_y = top_left_y+(end_y*map_zoom);

		int scaled_start_x = (int) ((int)(start_x/scale)+xoffset);
		int scaled_start_y = (int) ((int)(start_y/scale)+yoffset);
		int scaled_end_x = (int) ((int)(end_x/scale)+xoffset);
		int scaled_end_y = (int) ((int)(end_y/scale)+yoffset);
		
		if (start_x < 0) scaled_start_x--;
		if (start_y < 0) scaled_start_y--;
		if (end_x < 0) scaled_end_x--;
		if (end_y < 0) scaled_end_y--;

		for(int x = scaled_start_x; x <= scaled_end_x; x++) {
			for(int y = scaled_start_y; y <= scaled_end_y; y++) {
				NGZ.setRGB(x,y,0x40FF0000);
			}
		}
		outdated = true;
		controller.addNGZ(start_x, start_y, end_x, end_y);
	}
	
	/**
	 * Sends NGZ coordinates.
	 */
	public void removeNGZ(double start_x, double start_y, double end_x, double end_y, RoverController controller) {
		start_x = top_left_x+(start_x*map_zoom);
		start_y = top_left_y+(start_y*map_zoom);
		end_x = top_left_x+(end_x*map_zoom);
		end_y = top_left_y+(end_y*map_zoom);

		int scaled_start_x = (int) ((int)(start_x/scale)+xoffset);
		int scaled_start_y = (int) ((int)(start_y/scale)+yoffset);
		int scaled_end_x = (int) ((int)(end_x/scale)+xoffset);
		int scaled_end_y = (int) ((int)(end_y/scale)+yoffset);
		
		if (start_x < 0) scaled_start_x--;
		if (start_y < 0) scaled_start_y--;
		if (end_x < 0) scaled_end_x--;
		if (end_y < 0) scaled_end_y--;

		for(int x = scaled_start_x; x <= scaled_end_x; x++) {
			for(int y = scaled_start_y; y <= scaled_end_y; y++) {
				NGZ.setRGB(x,y,0x00000000);
			}
		}
		outdated = true;
		controller.removeNGZ(start_x, start_y, end_x, end_y);
	}
  
	
	/* 
	 * @param y: the y value of the cell
	 * @param x: the x value of the cell
	 * @return: the colour of cell at x, y 
	 */
	public int getValue(int x, int y) {
		int scaled_x = (int)Math.round((x/scale)+xoffset);
		int scaled_y = (int)Math.round((y/scale)+yoffset);
		if(scaled_x < 0 || scaled_x >= width || scaled_y < 0 || scaled_y >= height) return Colour.WHITE;
		return cells.getRGB(scaled_x,scaled_y) & 0xFFFFFF;
	}
	
	public void zoom(int times) {
		if(times < 0) map_zoom /= (-times*2);
		else map_zoom *= (times*2);
		if(map_zoom < 0.25) map_zoom = 0.25f;
		if(map_zoom > 4) map_zoom = 4f;
		outdated = true; // force image refresh
		
		zoomed = true;
	}
	
	public float getZoom() {
		return map_zoom;
	}
	
	public synchronized boolean is_outdated() {
		return outdated;
	}
	
    public synchronized void force_refresh() {
        outdated = true;
    }
	
	private void refreshMap() {
		outdated = false;

		// Check Whether rover is in bounds
		if(zoomed || rover_x < (top_left_x+100) || rover_x > (bottom_right_x-100) || 
				rover_y < (top_left_y+100) || rover_y > (bottom_right_y-100)) {
			
			// Centre the map over the rover
			top_left_x = (int) Math.round(rover_x-(map_zoom*(panel_width/2f)));
			top_left_y = (int) Math.round(rover_y-(map_zoom*(panel_height/2f)));	
			bottom_right_x = (int) Math.round(rover_x+(map_zoom*(panel_width/2f)));
			bottom_right_y = (int) Math.round(rover_y+(map_zoom*(panel_height/2f)));
			
			// If the rover is too close to the edge to centre it, move the start corners
			if(top_left_x < minimum_mappable_x) {
				top_left_x = minimum_mappable_x;
				bottom_right_x = (int) Math.round(top_left_x+(map_zoom*(panel_width)));
			}
			if(top_left_y < minimum_mappable_y){
				top_left_y = minimum_mappable_y;
				bottom_right_y = (int) Math.round(top_left_y+(map_zoom*(panel_height)));
			} 
			if(bottom_right_x > maximum_mappable_x){
				bottom_right_x = maximum_mappable_x;
				top_left_x = (int) Math.round(bottom_right_x-(map_zoom*(panel_width)));
			} 	
			if(bottom_right_y > maximum_mappable_y){
				bottom_right_y = maximum_mappable_y;
				top_left_y = (int) Math.round(bottom_right_y-(map_zoom*(panel_height)));
			}
					
			scaled_top_left_x = Math.round((top_left_x/scale)+xoffset);
			scaled_top_left_y = Math.round((top_left_y/scale)+yoffset);
			scaled_bottom_right_x = Math.round((bottom_right_x/scale)+xoffset);
			scaled_bottom_right_y = Math.round((bottom_right_y/scale)+yoffset);
		}
		
		ColorModel cm = cells.getColorModel();
		BufferedImage copy_map = new BufferedImage(cm, cells.copyData(null), cm.isAlphaPremultiplied(), null);
		copy_map.getGraphics().drawImage(NGZ,0,0,null);
		copy_map.setRGB(scaled_rover_x,scaled_rover_y,0x800080);
		rendered = copy_map.getSubimage(scaled_top_left_x, scaled_top_left_y, (scaled_bottom_right_x-scaled_top_left_x), (scaled_bottom_right_y-scaled_top_left_y));
		zoomed = false;
	}
	
	public synchronized BufferedImage getFullImage() {
		ColorModel cm = cells.getColorModel();
		BufferedImage copy_map = new BufferedImage(cm, cells.copyData(null), cm.isAlphaPremultiplied(), null);
		copy_map.setRGB(scaled_rover_x,scaled_rover_y,0x800080);
		copy_map.setRGB(scaled_rover_start_x,scaled_rover_start_y,Colour.MAGENTA);
		return copy_map;
	}
	
	public synchronized BufferedImage getUIImage() {
		if(outdated) 
			refreshMap();
		return rendered;
	}


}