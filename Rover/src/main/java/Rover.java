/*
 * Rover.java
 * 
 * Robot wrapper class to implement more complex robot functionality, 
 * such as handling turning, moving specific distances, synchronizing 
 * motors to move simultaneously.
 * 
 * Authors: SEP UG02
 */

import java.net.InetSocketAddress;
import java.util.Properties;
import lejos.robotics.navigation.*;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.navigation.DifferentialPilot;
import lejos.robotics.navigation.Navigator;
import java.util.List;


public class Rover {

	private static final double trackWidth = 120;
	@SuppressWarnings("unused")
	private static final double wheelDiameter = 56;
	
    public EV3LargeRegulatedMotor lmotor;
    public EV3LargeRegulatedMotor rmotor;
    
    private TouchSensor touchSensor;
    private ColourSensor colourSensor;
    private GyroSensor gyroSensor;
    private UltrasonicSensor ultrasonicSensor;
    private Navigator navi;
    
    private float rotationCircumference;
    private float wheelCircumference;
	private MODE mode;
	
	private DifferentialPilot movePilot;
	private myOdometryPoseProvider pose;
		
	//Update display every 0.5 seconds
	SensorUpdater updater;
	Communication communicator;
	private InetSocketAddress hostAddress = null;
	
	private boolean isRunning = true;
	private boolean isMoving = false; 
		
	private boolean connected = false;
	private boolean inRadiation = false;
	private NGZGrid ngzGrid;
    
    /*
     * Constructor
     */
    public Rover() {
    	
    	Properties prop = Helpers.loadProperties();
    	
    	// Initialise motors
    	lmotor = new EV3LargeRegulatedMotor(Helpers.getPort(prop.getProperty("left_motor")));
    	rmotor = new EV3LargeRegulatedMotor(Helpers.getPort(prop.getProperty("right_motor")));

    	lmotor.resetTachoCount();
    	rmotor.resetTachoCount();
    	
    	lmotor.setSpeed(Integer.valueOf(prop.getProperty("default_speed")));
    	rmotor.setSpeed(Integer.valueOf(prop.getProperty("default_speed")));
    	
    	lmotor.setAcceleration(Integer.valueOf(prop.getProperty("default_acceleration")));
    	rmotor.setAcceleration(Integer.valueOf(prop.getProperty("default_acceleration")));
    	

    	// Initialise sensors
    	touchSensor = new TouchSensor(Helpers.getPort(prop.getProperty("touch_sensor")));    	
    	colourSensor = new ColourSensor(Helpers.getPort(prop.getProperty("colour_sensor")));
    	gyroSensor = new GyroSensor(Helpers.getPort(prop.getProperty("gyro_sensor")));
    	ultrasonicSensor = new UltrasonicSensor(Helpers.getPort(prop.getProperty("ultrasonic_sensor")));

    	ngzGrid = new NGZGrid(500,500,20);		
   
    	// Create a new SensorUpdater
    	updater = new SensorUpdater(this);
    	
    	// Create a new communicator
    	communicator = new Communication(this);
    	    	    	
    	//Create movePilot and pose provider
    	movePilot = new DifferentialPilot(56, trackWidth, lmotor, rmotor);
    	pose = new myOdometryPoseProvider(movePilot);
    	navi = new Navigator(movePilot, pose);
    	setOrientation(90f);
    	setLocation(0f, 0f);
    }
    
    public void startSensors() {
    	// Start polling for sensor data.
    	touchSensor.setDaemon(true);
    	colourSensor.setDaemon(true);
    	gyroSensor.setDaemon(true);
    	ultrasonicSensor.setDaemon(true);
    	updater.setDaemon(true);

    	
    	touchSensor.start();
    	colourSensor.start();
    	gyroSensor.start();
    	ultrasonicSensor.start();
    	updater.start();
    }
    
    public void startCommunication() {
    	communicator.start();
    }
    
    /*
     * Move robot forward
     */
    public void forward() {
    	movePilot.forward();
    }

    /*
     * Move robot forward specific distance at speed
     * 
     * @param distance: Distance in mm to move the rover
     * @param speed: Speed in degree of rotation per minute
     */
    public void forward(float distance) {
    	movePilot.travel(distance);
    }

    /*
     * Move robot backward
     */
    public void backward() {
    	movePilot.backward();
    }
    
    /*
     * Move robot backward specific distance at speed
     * 
     * @param distance: Distance in millimeters to move the rover
     * @param speed: Speed in degree of rotation per minute
     */
    public void backward(float distance) {
    	movePilot.travel(-distance);
    }

    /*
     * Stop the robot
     */
    public void stop() {
    	movePilot.stop();
    }
    
    /*
     * Set the speed of the robot
     * 
     * @param speed: Speed in degree of rotation per minute
     */
    public void setSpeed(int speed) {
    	movePilot.setLinearSpeed(speed);
    	movePilot.setAngularSpeed(speed);
    }

    /* 
     * Continuously turn rover left
     */
    public void turnL() {
    	movePilot.rotate(360F,true);
    }
    
    /* 
     * Continuously turn rover right
     */
    public void turnR() {
    	movePilot.rotate(-360F,true);    	
    }
    
    
    /*
     * Turn the rover left
     * 
     * @param degrees: degrees to rotate
     */
    public void turnL(float degrees) {
    	movePilot.rotate(degrees,true);
    }

    /*
     * Turn the rover right
     * 
     * @param degrees: degrees to rotate
     */
    public void turnR(float degrees) {
    	movePilot.rotate(-degrees,true);
    }
    
    /*
     * Rotates the rover to input degree angle
     */
    public void rotateTo(float degrees) {
    	double curOrientation = getOrientation();
    	double angleToRotate = degrees - curOrientation;
    	movePilot.rotate(normalizeAngle(angleToRotate));
    }
    
    private double normalizeAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
    /**
     * Sets the current mode to manual control.
     * If the mode is in manual mode, a behaviour will take over and cause the rover to be able to be manually controlled.
     */
    public void setManualControl() {
    	this.mode = MODE.manual;
    }
    
    /**
     * Sets the current mode to automatic control.
     * If the mode is in automatic mode, the rover will be allowed to continue with its mapping algorithm.
     */
    public void setAutoControl() {
    	this.mode = MODE.automatic;
    }
    
    /**
     * 
     * @return MODE - either MODE.manual or MODE.automatic, indicating which mode the rover is currently in.
     */
    public MODE getMode() {
    	return mode;
    }
    
    /**
     * Returns true if the rover is still running (actively connected to sensors and operating either manually or automatically).
     * 
     * @return true if the rover is currently running, and false if it is not running.
     */
    public boolean isRunning() {
    	return this.isRunning;
    }
    
    /**
     * Declares that the rover is not running anymore, causing it to close all ports and not listen to any more instructions.
     */
    public void stopRunning() {
    	this.isRunning = false;
    	closePorts();
    }
    
    /**
     * Closes all ports the rover was connected to, such that they can be opened again later without having
     * the EV3 brick complain about unclosed ports.
     */
    public void closePorts() {
    	lmotor.close();
    	rmotor.close();
    	touchSensor.close();
    	colourSensor.close();
    	gyroSensor.close();
    	ultrasonicSensor.close();
    }
    
    /*
     * Query touch sensor
     */
    public boolean bumperPressed(){
    	return touchSensor.bumperPressed();
    }
    
    /*
     * Query distance from obstacle
     * 
     * @return distance in mm from obstacle
     */ 
    public int getObstacleDistance() {
    	return ultrasonicSensor.getDistance();
    }
    
    /*
     * Query colour detected by color sensor
     * 
     * @return colour id (0-7) of detected colour (NONE, BLACK, BLUE, GREEN, YELLOW, RED, WHITE, BROWN)
     */ 
    public int getColourSensorColourID(){
    	return colourSensor.getColourID();
    }
    
    /*
     * Query level of red, green and blue light detected by colour sensor
     * 
     * @return 3 elements containing the intensity level between 0 and 1 of red, green and blue light 
     */ 
    public float[] getColourSensorRGB(){
    	return colourSensor.getRGB();
    }
    
    /*
     * Sets the colour mode of the colour sensor to RGB or ID
     */
    public void setColourMode(ColourMode mode) {
    	colourSensor.setColourMode(mode);
    }
    
    /*
     * Query orientation of gyro sensor
     * 
     * @return degree orientation of the gyro sensor
     */ 
    public int getRotationAngle(){
    	return gyroSensor.getAngle();
    }
    
    /*
     * Query angular velocity
     * 
     * @return degrees per second
     */ 
    public int getRotationRate(){
    	return gyroSensor.getRate();
    }
    
    /*
     * Returns the moving status, i.e. true if rover is moving, false otherwise
     * 
     * @return moving status
     */
    public boolean isMoving() {
    	return isMoving;
    }
    
    /*
     * Return the current wheel rate of the wheels 
     * 
     *  @return wheel rate 
     */
    public int getWheelRate() {
    	return Math.abs( lmotor.getSpeed() ); 
    }
    
    
    /*
     * @returns the wheel circumference of the rover 
     */
    public float getWheelCircumference() {
    	return wheelCircumference; 
    }
    

    /**
     * Sets the host address (address of the GUI server).
     * Triggers the sensorUpdater to start sending sensor data to the GUI.
     * @param socketAddress
     */
    public void setHostAddress(InetSocketAddress socketAddress) {
    	this.hostAddress = socketAddress;
    }
    
    /**
     * Gets the host address associated with the connected GUI.
     * @return InetSocketAddress of GUI.
     */
    public InetSocketAddress getHostAddress() {
    	return this.hostAddress;
    }

    /**
     * Checks whether the rover has been connected to the GUI yet.
     * @return true if the rover has connected with the GUI.
     */
	public boolean isConnected() {
		return this.connected;
	}
	
	/**
	 * Sets the boolean for whether the rover is connected to the GUI.	
	 * @param connected
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	
	/**
	 * Gets the X position of the rover from the tracker.
	 * @return x position of rover.
	 */
	public synchronized double getXPos() {
		double x = pose.getPose().getX();
		x = x+33.0*Math.cos(Math.toRadians(getOrientation()));
		return x;
	}
	
	public synchronized double getUnadjustedXPos() {
		return pose.getPose().getX();
	}
	
	/**
	 * Gets the Y position of the rover from the tracker.
	 * @return y position of rover.
	 */
	public synchronized double getYPos() {
		double y = pose.getPose().getY();
		y = y+33.0*Math.sin(Math.toRadians(getOrientation()));
		return y;
	}	
	
	public synchronized double getUnadjustedYPos() {
		return pose.getPose().getY();
	}
	
	/**
	 * Gets the current orientation of the rover in degrees
	 * 	 * @return orientation of rover.
	 */
	public synchronized double getOrientation() {
		return pose.getPose().getHeading();
	}
	
	public synchronized void setOrientation(float degrees) {
		pose.setPose(new Pose((float)getUnadjustedXPos(), (float)getUnadjustedYPos(), degrees));
	}
	
	/*
	 * Sets the current location of the rover
	 */
	public synchronized void setLocation(float x, float y) {
		pose.setPose(new Pose(x, y, (float)getOrientation()));
	}
	
	/*
	 * @return: the rotational circumference 
	 */
	public int getRotationalCircumference() {
		return (int) rotationCircumference;	
	}
	
	/*
	 * Check whether current position is a NGZ
	 */
	public boolean inNGZ() {
		return ngzGrid.inNGZ(getXPos(),getYPos());
	}
	
	/*
	 * Sends NGZ coordinates.
	 */
	public void addNGZ(double start_x, double start_y, double end_x, double end_y) {
		ngzGrid.addNGZ(start_x, start_y, end_x, end_y);
	}
	
	/*
	 * Sends NGZ coordinates.
	 */
	public void removeNGZ(double start_x, double start_y, double end_x, double end_y) {
		ngzGrid.removeNGZ(start_x, start_y, end_x, end_y);
	}
	
	public void waitComplete() {
		lmotor.waitComplete();
		rmotor.waitComplete();
	}
	
	public void setRadiation(boolean bool) {
		this.inRadiation = bool;
	}
	
	public boolean getRadiation() {
		return inRadiation;
	}
		
	public void goTo(float x, float y) {
		navi.goTo(x,y);
		if(!navi.waitForStop()) navi.goTo(x,y);
	}
	
	public List<Float> getWaypoints() {
		return ngzGrid.getWaypoints(getXPos(), getYPos(),getOrientation());
	}
			
}


