/*
 * Colour.java
 * 
 * Represents colours that will be found on the map 
 *
 * Black - Crater
 * Red - Boundary
 * Brown - Landing Trail
 * Yellow - Footsteps
 * Green - Radiation (solid/line)
 * Blue - Rover Tracks
 * White - Everything Else
 * 
 * Authors: SEP UG02
 */

public class Colour {
	public static final int NONE = 0xFFFFFF;
	public static final int RED = 0xFF0000;
	public static final int GREEN = 0x91d050;
	public static final int BLUE = 0x0000ff;
	public static final int YELLOW = 0xffff00;
	public static final int MAGENTA = 0xff00ff;
	public static final int ORANGE = 0xffa500;
	public static final int WHITE = 0xFFFFFF;
	public static final int BLACK = 0x000000;
	public static final int PINK = 0xffc0cb;
	public static final int GRAY = 0x808080;
	public static final int LIGHT_GRAY = 0xd3d3d3;
	public static final int DARK_GRAY = 0x4F4F4F;
	public static final int CYAN = 0x00ffff;
	
	public static final int ID_NONE = -1;
	public static final int ID_RED = 0;
	public static final int ID_GREEN = 1;
	public static final int ID_BLUE = 2;
	public static final int ID_YELLOW = 3;
	public static final int ID_MAGENTA = 4;
	public static final int ID_ORANGE = 5;
	public static final int ID_WHITE = 6;
	public static final int ID_BLACK = 7;
	public static final int ID_PINK = 8;
	public static final int ID_GRAY = 9;
	public static final int ID_LIGHT_GRAY = 10;
	public static final int ID_DARK_GRAY = 11;
	public static final int ID_CYAN = 12;

	public static String getString(int id) {
		switch(id) {
		case -1:
			return "NONE";
		case 0:
			return "RED";
		case 1:
			return "GREEN";
		case 2:
			return "BLUE";
		case 3:
			return "YELLOW";
		case 4:
			return "MAGENTA";
		case 5:
			return "ORANGE";
		case 6:
			return "WHITE";
		case 7:
			return "BLACK";
		case 8:
			return "PINK";
		case 9:
			return "GRAY";
		case 10:
			return "LIGHT GRAY";
		case 11:
			return "DARK GRAY";
		case 12:
			return "CYAN";
		}
		return "ERROR";
	}
	
	public static int getRGB(int id) {
		switch(id) {
		case -1:
			return NONE;
		case 0:
			return RED;
		case 1:
			return GREEN;
		case 2:
			return BLUE;
		case 3:
			return YELLOW;
		case 4:
			return MAGENTA;
		case 5:
			return ORANGE;
		case 6:
			return WHITE;
		case 7:
			return BLACK;
		case 8:
			return PINK;
		case 9:
			return GRAY;
		case 10:
			return LIGHT_GRAY;
		case 11:
			return DARK_GRAY;
		case 12:
			return CYAN;
		}
		return WHITE;
	}
}