/*
* ColourMode.java
*
* A ColourMode enum that contains the two possible modes the colour sensor could be in:
* colourID - Values returned as an ID value from 0-7
* RGB - Values are returned as RGB array
*
* Authors: SEP UG02
*/
import java.util.List;

import lejos.robotics.subsumption.Behavior;

public class AvoidNGZ implements Behavior{
	
	private Rover rover;
	private boolean _suppressed = false;
	
	public AvoidNGZ(Rover r) {
		this.rover = r;
	}
	
	
	/*
	 *  Take control when ultrasonic sensor detects an object closer
	 *  than minObstacleDistance and we are in automatic mode
	 */
	public boolean takeControl() {
		return rover.inNGZ()
			&& rover.getMode() == MODE.automatic;
	}
	
	
	public void suppress() {
		_suppressed = true;
	}
	
	public void action() {
		_suppressed = false;
	    if(!_suppressed) {
	    	rover.stop();
			double initial = rover.getOrientation();
			List<Float> waypoints = rover.getWaypoints();
			for(int i = 0; i < waypoints.size(); i += 2) {
				rover.goTo(waypoints.get(i), waypoints.get(i+1));
				rover.waitComplete();
			}
			rover.rotateTo((float)initial);
	    }
	    
	}

}
