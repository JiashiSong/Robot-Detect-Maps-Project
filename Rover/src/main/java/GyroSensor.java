/*
 * GyroSensor.java
 *
 * A class for implementing basic Gyro Sensor Methods
 *
 * Authors: SEP UG02
 */

import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;

public class GyroSensor extends Thread {
	
	private EV3GyroSensor gyroSensor;
	
	private int rotationAngle; //Unit Degrees
	private int rotationRate; //Unit: Degrees/sec
	
	private SampleProvider angleSampler;
	private SampleProvider rateSampler;
	
	/**
	 * Constructor
	 * 
	 * @param port - port to which GyroSensor is connected
	 */
	GyroSensor(Port port) {
		gyroSensor = new EV3GyroSensor(port);
		angleSampler = gyroSensor.getAngleMode();
		rateSampler = gyroSensor.getRateMode();
		rotationAngle = 0;
		rotationRate = 0;
	}
	
	/**
	 * 
	 * @return current rotation angle of gyroscope
	 */
	public int getAngle() {
		return rotationAngle;
	}
	
	/**
	 * 
	 * @return current rotation rate of gyroscope
	 */
	public int getRate() {
		return rotationRate;
	}
	
	/**
	 *  Continuously polls for sensor readings
	 */
	public void run() {
		while (true) {		
			// Retrieve angle of rotation
			float[] angleState = new float[angleSampler.sampleSize()];
			angleSampler.fetchSample(angleState, 0);
			rotationAngle = (int)angleState[0];
			
			// Retrieve rate of rotation
			float[] rateState = new float[rateSampler.sampleSize()];
			rateSampler.fetchSample(rateState, 0);
			rotationRate = (int)rateState[0];
		}
	}
	
	/**
     * Closes the gyro sensor port.
     * Call this when the Rover has finished operation and is going to disconnect everything.
     */
    public void close() {
    	gyroSensor.close();
    }	
}
