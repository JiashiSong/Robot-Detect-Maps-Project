/*
 * UltrasonicSensor.java
 *
 * A class for implementing basic Ultrasonic Sensor Methods
 *
 * Authors: SEP UG02
 */

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

public class UltrasonicSensor extends Thread {
	
	private EV3UltrasonicSensor ultrasonicSensor;
	
	private int distance; //Unit: mm
	
	private SampleProvider sampler;
	
	/**
	 * Constructor
	 * 
	 * @param port - port to which the UltrasonicSensor is connected
	 */
	UltrasonicSensor(Port port) {
		while(ultrasonicSensor == null) {
			try {
				ultrasonicSensor = new EV3UltrasonicSensor(port);
			} catch(Exception e) {
			}
		}
		sampler = ultrasonicSensor.getDistanceMode();
	}
	
	
	/**
	 * Queries the sensor for the current distance
	 */
	public int getDistance()
	{
		return distance;
	}
	
	
	public void run() {
		while (true) {
			//Retrieve sampler values and store in state
			float[] state = new float[sampler.sampleSize()];
			sampler.fetchSample(state, 0);
			
			// Convert to mm
			distance = (int)(state[0] * 1000f);
		}
	}
	
	/**
     * Closes the ultrasonic sensor port.
     * Call this when the Rover has finished operation and is going to disconnect everything.
     */
    public void close() {
    	ultrasonicSensor.close();
    }
}
