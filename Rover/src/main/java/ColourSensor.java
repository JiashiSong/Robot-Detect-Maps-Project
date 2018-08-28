/*
 * ColourSensor.java
 *
 * A class for implementing basic Colour Sensor Methods
 *
 * Authors: SEP UG02
 */

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3ColorSensor;
import lejos.robotics.SampleProvider;

public class ColourSensor extends Thread {

	private EV3ColorSensor colourSensor;

	private int colourId;
	private float[] rgb;

	private SampleProvider  colourIdSampler;
	private SampleProvider  rgbSampler;
	
	private ColourMode currentMode;
	
	/**
	 * Constructor
	 * 
	 * @param port - port to which the ColourSensor is connected
	 */
	ColourSensor(Port port) {
		colourSensor = new EV3ColorSensor(port);
		colourIdSampler = colourSensor.getColorIDMode();
		rgbSampler = colourSensor.getRGBMode();
		rgb = new float[]{0f, 0f, 0f};
		colourId = 0;
		currentMode = ColourMode.colourID;
	}
	
	/**
	 * Sets the current colour detection mode of sensor
	 * 
	 * @param mode - currentMode is set to this enums value
	 */
	public void setColourMode(ColourMode mode) {
		this.currentMode = mode;
	}

    /**
     * Query colour detected by color sensor
     * 
     * @return colour id (0-7) of detected colour (NONE, BLACK, BLUE, GREEN, YELLOW, RED, WHITE, BROWN)
     * If not in correct mode returns 0;
     */ 
    public int getColourID(){
    	if (currentMode == ColourMode.colourID) {
    		return colourId;
    	}
    	else {
    		return 0;
    	}
    }
    
    /**
     * Query level of red, green and blue light detected by colour sensor
     * 
     * @return 3 elements containing the intensity level between 0 and 1 of red, green and blue light 
     * If not in correct mode returns 0
     */ 
    public float[] getRGB(){
    	if (currentMode == ColourMode.RGB) {
    		return rgb;
    	}
    	else {
    		return new float[]{0f, 0f, 0f};
    	}
    }

	/**
	 * Continuously polls for colour sensor readings
	 */
	public void run() {
		while (true) {	
			
			if (currentMode == ColourMode.colourID) {
				// Retrieve id mode colour
				float[] colourIdState = new float[colourIdSampler.sampleSize()];
				colourIdSampler.fetchSample(colourIdState, 0);
				colourId = (int)colourIdState[0];
			}
			
			else if (currentMode == ColourMode.RGB) {
				// Retrieve rgb mode colour
				rgbSampler.fetchSample(rgb, 0);
			}
		}
	}
	
	/**
     * Closes the colour sensor port.
     * Call this when the Rover has finished operation and is going to disconnect everything.
     */
    public void close() {
    	colourSensor.close();
    }
}
