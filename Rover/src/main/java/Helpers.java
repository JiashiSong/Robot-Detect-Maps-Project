/*
 * Robot.java
 * 
 * Robot wrapper class to implement more complex robot functionality, 
 * such as handling turning, moving specific distances, synchronising 
 * motors to move simultaneously.
 * 
 * Authors: SEP UG02
 */

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;

public class Helpers {
	
	/**
	 * Opens the config.properties file and loads the configuration map to extract properties.
	 * 
	 * @return properties Configuration map for project properties.
	 */
	public static Properties loadProperties() {
		Properties properties = new Properties();
		String property_file = "config.properties";
		
		try {
			InputStream inputStream = new FileInputStream(property_file);
			properties.load(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return properties;
	}
	
	/**
	 * Converts a string representation of a port to a Lejos Port object.
	 * 
	 * @param portName
	 * @return Lejos Port equivalent to string portName; 
	 */
	public static Port getPort(String portName) {
		switch(portName) {
			case "PORT_A":
				return MotorPort.A;
			case "PORT_B":
				return MotorPort.B;
			case "PORT_C":
				return MotorPort.C;
			case "PORT_D":
				return MotorPort.D;
			case "PORT_S1":
				return SensorPort.S1;
			case "PORT_S2":
				return SensorPort.S2;
			case "PORT_S3":
				return SensorPort.S3;
			case "PORT_S4":
				return SensorPort.S4;
		}
		return null;
	}
}