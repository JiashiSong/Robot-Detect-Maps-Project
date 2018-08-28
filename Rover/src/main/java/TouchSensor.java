/*
 * TouchSensor.java
 *
 * A class for implementing basic Touch Sensor Methods
 *
 * Authors: SEP UG02
 */

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3TouchSensor;
import lejos.robotics.SampleProvider;

public class TouchSensor extends Thread {

	private EV3TouchSensor touchSensor;
	
	private boolean pressed;
	
	private SampleProvider sampler;
	
	/**
	 * Constructor
	 * 
	 * @param port - port to which the TouchSensor is connected
	 */
	TouchSensor(Port port) {
		touchSensor = new EV3TouchSensor(port);
		// Retrieve TouchMode
		sampler = touchSensor.getTouchMode();
	}

	/**
	 * Query the state of the touch sensor
	 */
	public boolean bumperPressed() {
		return pressed;
	}

	/**
	 * Continuously polls for the bumpers being pressed.
	 */
	public void run() {
		while (true) {
			// Fill touch state variable with TouchMode result
			float[] state = new float[sampler.sampleSize()];
			sampler.fetchSample(state, 0);

			// return boolean equivalent of touch state
			pressed = (state[0] == 1);
		}
	}
	
	/**
     * Closes the touch sensor port.
     * Call this when the Rover has finished operation and is going to disconnect everything.
     */
    public void close() {
    	touchSensor.close();
    }
}
