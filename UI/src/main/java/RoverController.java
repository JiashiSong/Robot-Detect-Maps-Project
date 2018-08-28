
/*
* roverController.java
*
* rover controller class that handles all communication with the rover through a socket.
*
* Authors: SEP UG02
*/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.Thread;
import java.util.concurrent.TimeUnit;

public class RoverController {
	
	private ConnectionCheckThread connectionCheckThread; 

	private BufferedWriter pw;
	private Socket sendSocket;	
	private ServerSocket receiveSocket;

	private boolean connected;
	private double[] rover_position;
	
	// We need to communicate with the ui
	private UI ui;
	
	// Control mode state (automatic or manual)
	private MODE controlState;

	private ReadSensorData readSensorDataThread;
	
	public RoverController( UI ui ) {
		// Let's assign our UI
		this.ui = ui;
		ui.enableButtons(false);
		this.connected = false;
		this.rover_position = new double[2];
		rover_position[0] = 0;
		rover_position[1] = 0;
		
		// Default to manual control 
		controlState = MODE.manual;
		
		try {
			this.receiveSocket = new ServerSocket(19234);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Connects to the rover at the specified IP on port 19232.
	 * Assumes that the rover is currently running and listening with a socket on port 19232.
	 * 
	 * @param IP - IP address of the rover to connect to.
	 */
	public void connectToRover(String IP) {
		try {
			this.sendSocket = new Socket(IP, 19232);
			pw = new BufferedWriter(new OutputStreamWriter(this.sendSocket.getOutputStream()));
		
			// Let's create a connection check thread and start it 
			connectionCheckThread = new ConnectionCheckThread();
			connectionCheckThread.start();
			
			readSensorDataThread = new ReadSensorData();
			readSensorDataThread.start();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a message to the rover to stop running everything.
	 * Causes the rover to close all of its ports and shut down the connection.
	 */
	public void disconnectFromRover() {
		sendCommand("EXIT");
		try {
			this.sendSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a given message over the communication socket to the rover to be interpreted as a command.
	 * 
	 * @param command - message to send to the rover.
	 */
	public void sendCommand(String command) {
		if(isConnected()) {
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
	}
	
	/**
	 * Sends a message indicating what control state we would like, and toggles 
	 * current state. 
	 */
	public void toggleControlMode() {
		switch(controlState) {
			case manual:
				sendCommand("STATE-AUTO");
				sendCommand("STATE-AUTO");
				controlState = MODE.automatic; 
				break;
			case automatic:
				sendCommand("STATE-MANUAL");
				controlState = MODE.manual;
				break;
		}
	}
	
	public MODE getMode() {
		return this.controlState;
	}
	
	public double getX() {
		return rover_position[0];
	}
	
	public double getY() {
		return rover_position[1];
	}
	
	
	/**
	 * Sends a message indicating that the down button has been pressed on the manual controller (rover should reverse).
	 */
	public void downPress() {
		sendCommand("DOWN-PRESS");
	}
	
	/**
	 * Sends a message indicating that the down button has been released on the manual controller (rover should stop).
	 */	
	public void downRelease() {
		sendCommand("DOWN-RELEASE");
	}
 
	/**
	 * Sends a message indicating that the left button has been pressed on the manual controller (rover should turn left).
	 */
	public void leftPress() {
		sendCommand("LEFT-PRESS");
	}
 
	/**
	 * Sends a message indicating that the left button has been released on the manual controller (rover should stop).
	 */
	public void leftRelease() {
		sendCommand("LEFT-RELEASE");
	}
	
	/**
	 * Sends a message indicating that the up button has been pressed on the manual controller (rover should travel forward).
	 */
	public void upPress() {
		sendCommand("UP-PRESS");
	}
	
	/**
	 * Sends a message indicating that the up button has been released on the manual controller (rover should stop).
	 */
	public void upRelease() {
		sendCommand("UP-RELEASE");
	}
 
	/**
	 * Sends a message indicating that the right button has been pressed on the manual controller (rover should turn right).
	 */
	public void rightPress() {
		sendCommand("RIGHT-PRESS");
	}
	
	/**
	 * Sends a message indicating that the right button has been released on the manual controller (rover should stop).
	 */
	public void rightRelease() {
		sendCommand("RIGHT-RELEASE");
	}
	
	/**
	 * Sends NGZ coordinates.
	 */
	public void addNGZ(double start_x, double start_y, double end_x, double end_y) {
		sendCommand(String.format("ADDNGZ %f %f %f %f", start_x, Math.min(-start_y, -end_y), end_x, Math.max(-start_y, -end_y)));
	}
	
	/**
	 * Sends NGZ coordinates.
	 */
	public void removeNGZ(double start_x, double start_y, double end_x, double end_y) {
		sendCommand(String.format("REMOVENGZ %f %f %f %f", start_x, Math.min(-start_y, -end_y), end_x, Math.max(-start_y, -end_y)));
	}
  
	public boolean isConnected() {
		return connected;
	}
	
  /**
	 * We will use this thread to continuously check the connection status.
	 * If we're connected then we tell the UI to enable buttons, otherwise 
	 * we want to disable the buttons 
	 */
	private class ConnectionCheckThread extends Thread {
		public void run() {
			while(true) {
				// We want to check the connection every two seconds
				
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
				// Check that we can read, otherwise there must be no connection
				try {
					sendSocket.getOutputStream().write("".getBytes());
					// Now check that we can write, otherwise there is no connection
					try {
						pw.write("\n");
						pw.flush();
						connected = true;
						ui.enableButtons(true);
						
					} catch (IOException e1) {
						e1.printStackTrace();
						connected = false;
						ui.enableButtons(false);
					}
					// We must not be connected, so let the ui know to disable buttons
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					connected = false;
					ui.enableButtons(false);
				}
			}
		}
	}
	
	/**
	 * Class to read sensor data from a server socket, and print that to the GUI.
	 */
	private class ReadSensorData extends Thread {
		
		public void run() {
			while (true) {
				try {
					Socket socket = receiveSocket.accept();
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					// the listener with the while readline
					String line;
					while ((line = reader.readLine()) != "STOP") {
						if (line.startsWith("SENSORS ")) {
							String[] data = line.split("\\s+");
							
							// Convert strings to values
							int colour = Integer.valueOf(data[3]);
							rover_position[0] = Double.valueOf(data[1]);
							rover_position[1] = -Double.valueOf(data[2]);
							double orientation = Double.valueOf(data[4]);
							int obstacleDistance = Integer.valueOf(data[5]);
							boolean inRadiation = Boolean.valueOf(data[6]);
							
							// Order of messages sent are in one line as follows:
							// SENSORS x y colorid rotationangle obstacledistance
							ui.roverXY.setText(data[1] + ", " + data[2]);
							ui.roverColour.setText(Colour.getString(colour));
							ui.roverAngle.setText(data[4]);
							ui.roverDistance.setText(data[5]);
							
							// Set the value on the grid

							ui.grid.setValue(rover_position[0],rover_position[1], Colour.getRGB(colour),true);
							
							// if an object is detected 7.5cm away (same as obstacle avoidance), map it
							if (inRadiation && obstacleDistance <= 150) {
								double objectX = rover_position[0]+((obstacleDistance+50)*Math.cos(Math.toRadians(orientation)));
								double objectY = rover_position[1]-((obstacleDistance+50)*Math.sin(Math.toRadians(orientation)));
								ui.grid.setValue(objectX,objectY,Colour.CYAN,false);
							}
							else if(obstacleDistance <= 75) {
								double objectX = rover_position[0]+((obstacleDistance+50)*Math.cos(Math.toRadians(orientation)));
								double objectY = rover_position[1]-((obstacleDistance+50)*Math.sin(Math.toRadians(orientation)));
								ui.grid.setValue(objectX,objectY,Colour.DARK_GRAY,false);
							}
								
						}	
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		}
	}
}
