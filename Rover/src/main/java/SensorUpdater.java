/*
 * SensorUpdater.java
 * 
 * Class for displaying sensor data to EV3 LCD screen
 * 
 * Authors: SEP UG02
 */
import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.TextLCD;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import lejos.hardware.ev3.LocalEV3;

public class SensorUpdater extends Thread {
	
	private Rover rover;
	private Socket socket;
	private BufferedWriter pw;
	
	/**
	 * Constructor
	 */
	SensorUpdater(Rover rover) {
        this.rover = rover;
    }
	
	/**
	 * Continuously polls for sensor readings
	 */
	public void run() {
		TextLCD t = LocalEV3.get().getTextLCD();
		LCD.setAutoRefresh(false);
		
		// Draw initial sensor text values and titles.
		t.drawString("Sensors", 5, 0);		
		t.drawString("Colour: "+ Integer.toString(rover.getColourSensorColourID()), 0, 2);
		t.drawString("Gyro: "+ Double.toString(rover.getOrientation())+
				   " "+ Integer.toString(rover.getRotationRate()),  0, 3);
		t.drawString("Ultrasonic: "+ Integer.toString(rover.getObstacleDistance()), 0, 4);
		t.drawString("Bumper: "+ Boolean.toString(rover.bumperPressed()), 0, 5);
		t.drawString("X: "+ Double.toString(rover.getXPos()), 0, 6);
		t.drawString("Y: "+ Double.toString(rover.getXPos()), 0, 7);
		
		
		// Clear old sensor readings and draw
		while(true) {			
			t.clear(8,2,5);
			t.drawString(Integer.toString(rover.getColourSensorColourID()), 8, 2);
			t.clear(6,3,10);
			t.drawString(Double.toString(rover.getOrientation()), 6, 3);
			t.drawString(Integer.toString(rover.getRotationRate()), 10, 3);
			t.clear(12,4,6);
			t.drawString(Integer.toString(rover.getObstacleDistance()), 12, 4);
			t.clear(8,5,5);
			t.drawString(Boolean.toString(rover.bumperPressed()), 8, 5);
			t.clear(3,6,10);
			t.drawString(Double.toString(rover.getXPos()), 3, 6);
			t.clear(3,7,10);
			t.drawString(Double.toString(rover.getYPos()), 3, 7);
			LCD.refresh();
			
			// Send to UI.
			// If the host address is defined and the rover is not connected, connect the rover.
			if (rover.getHostAddress() != null && !rover.isConnected()) {
				createSocket();
				rover.setConnected(true);
			}
			
			// If the rover is connected, send the sensor data as a single message to the server.
			if (rover.isConnected()) {
				// Order of messages sent are in one line as follows:
				// SENSORS x y colorid rotationangle obstacledistance
				StringBuilder sb = new StringBuilder();
				sb.append("SENSORS ");
				sb.append(Double.toString(rover.getXPos()) + " ");
				sb.append(Double.toString(rover.getYPos()) + " ");
				sb.append(Integer.toString(rover.getColourSensorColourID()) + " ");
				sb.append(Double.toString(rover.getOrientation()) + " ");
				sb.append(Integer.toString(rover.getObstacleDistance()) + " ");
				sb.append(Boolean.toString(rover.getRadiation()) + " ");
				sendCommand(sb.toString());
			}
		}
	
	}
	
	/**
	 * Sends a given message over the communication socket to the rover to be interpreted as a command.
	 * 
	 * @param command - message to send to the rover.
	 */
	public void sendCommand(String command) {
		if (pw == null)
		       throw new IllegalArgumentException("writer is null");
		if (command == null)
		       throw new IllegalArgumentException("stats is null");
		try {
			pw.write(command+"\n");
			pw.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	/**
	 * Opens a socket to the GUI via the address in rover.getHostAddress()
	 */
	public void createSocket() {
		try {
			this.socket = new Socket(rover.getHostAddress().getAddress().getHostAddress(), 19234);
			pw = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
