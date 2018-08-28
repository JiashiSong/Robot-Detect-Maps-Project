/*
* NGZGrid.java
*
* A grid of ngz points
* 
* Authors: SEP UG02
*/
import java.util.List;
import java.util.ArrayList;

public class NGZGrid {
	private int width;
	private int height;
	private double scale; // cells per m
	private double xoffset;
	private double yoffset;
	
	private byte[][] grid;
	
	public NGZGrid(int width, int height, double scale) {
		
		this.width = width;
		this.height = height;
		this.scale = scale;
		this.xoffset = width/2;
		this.yoffset = height/2;
		
		grid = new byte[height][width];
	}
	
	/*
	 * Get whether position is in NGZ
	 * 
	 * @param x: x coordinate
	 * @param y: y coordinate
	 */
	public boolean inNGZ(double x, double y) {
		int scaled_x = (int)Math.round((x/scale)+xoffset);
		int scaled_y = (int)Math.round((y/scale)+yoffset);
		//if(scaled_x < 0 || scaled_x >= width || scaled_y < 0 || scaled_y >= height) return false;
		return (grid[scaled_x][scaled_y] == 1);		
	}
	
	/*
	 * Sends NGZ coordinates.
	 */
	public void addNGZ(double start_x, double start_y, double end_x, double end_y) {
		int scaled_start_x = (int)Math.round((start_x/scale)+xoffset);
		int scaled_start_y = (int)Math.round((start_y/scale)+yoffset);
		int scaled_end_x = (int)Math.round((end_x/scale)+xoffset);
		int scaled_end_y = (int)Math.round((end_y/scale)+yoffset);
		for(int x = scaled_start_x; x <= scaled_end_x; x++) {
			for(int y = scaled_start_y; y <= scaled_end_y; y++) {
				grid[x][y] = 1;
			}
		}
	}
	
	/*
	 * Sends NGZ coordinates.
	 */
	public void removeNGZ(double start_x, double start_y, double end_x, double end_y) {
		int scaled_start_x = (int)Math.round((start_x/scale)+xoffset);
		int scaled_start_y = (int)Math.round((start_y/scale)+yoffset);
		int scaled_end_x = (int)Math.round((end_x/scale)+xoffset);
		int scaled_end_y = (int)Math.round((end_y/scale)+yoffset);
		for(int x = scaled_start_x; x <= scaled_end_x; x++) {
			for(int y = scaled_start_y; y <= scaled_end_y; y++) {
				grid[x][y] = 0;
			}
		}
	}
	
	public List<Float> getWaypoints(double x, double y, double angle) {
		int multiplier = (angle > 0)? 1 : -1;
		
		// Find entry point
		double entry_x = x;
		double entry_y = y;
		while(inNGZ(entry_x, entry_y)) {
			entry_y -= scale*multiplier;
		}
		
		// Find exit point
		double exit_x = x;
		double exit_y = y;
		while(inNGZ(exit_x, exit_y)) {
			exit_y += scale*multiplier;
		}
		
		// Find lower travel point
		double lower_x = x;
		double lower_y = y;
		while(inNGZ(lower_x, lower_y)) {
			lower_x -= scale;
		}
		
		// Find upper travel point
		double upper_x = exit_x;
		double upper_y = exit_y-scale*multiplier;
		while (inNGZ(upper_x, upper_y)) {
			upper_x -= scale;
		}
		
		// Buffer space
		entry_y -= scale*multiplier;
		exit_y += scale*multiplier;
		upper_x -= scale;
		upper_y += 2*scale*multiplier;
		lower_x -= scale;
		lower_y -= 2*scale*multiplier;
		
		List<Float> waypoints = new ArrayList<Float>();
		waypoints.add((float)entry_x);
		waypoints.add((float)entry_y);
		waypoints.add((float)lower_x);
		waypoints.add((float)lower_y);
		waypoints.add((float)upper_x);
		waypoints.add((float)upper_y);
		waypoints.add((float)exit_x);
		waypoints.add((float)exit_y);
		
		return waypoints;
	}

}