/*
* Logic.java
*
* Logic class will make a Robot object and use it to test the user stories in Sprint 1.
*
* Authors: SEP UG02
*/

/*
 * Class that will provide testing 
 */

import java.util.Properties;
import lejos.robotics.subsumption.Arbitrator;
import lejos.robotics.subsumption.Behavior;

public class Logic {
	private static Properties prop = Helpers.loadProperties();

	static final int TESTING_SPEED = Integer.valueOf(prop.getProperty("default_speed"));

	/*
	 * We'll run through some testing here in the main function
	 */
	public static void main(String[] args) {
		final Rover rover = new Rover();
		rover.startSensors();
		rover.startCommunication();
		rover.setSpeed(50);
		rover.setManualControl();
		
		// Define behaviors.
		Behavior collisions = new HitObject(rover);
		Behavior sweepSearch = new SweepingMovement(rover);
		Behavior avoidCrater = new AvoidCrater(rover);
		Behavior avoidPhysicalObject = new AvoidPhysicalObject(rover);
		Behavior radiation = new RadiationSweep(rover);
		Behavior avoidNGZ = new AvoidNGZ(rover);
		Behavior manual = new ManualControl(rover);
		
		// Define list of all behaviors in increasing order of priority.
		Behavior[] behaviorList = {sweepSearch, avoidCrater, avoidPhysicalObject, avoidNGZ,  radiation, collisions, manual}; 
		Arbitrator arbitrator = new Arbitrator(behaviorList);
		
		while (rover.getObstacleDistance() < 10) {}
		arbitrator.go();
		
	}
}
