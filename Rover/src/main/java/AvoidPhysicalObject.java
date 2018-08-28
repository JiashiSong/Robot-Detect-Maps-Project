import lejos.robotics.subsumption.Behavior;

public class AvoidPhysicalObject implements Behavior{
	
	private Rover rover;
	private boolean _suppressed = false;
	private static final int minObstacleDistance = 75; //Aim to keep this distance from object
	private static final int errorMargin = 10; //Error margin for how close to minObstacleDistance value
	private static final int adjustDistance = 75; //How far we move horizontally to object
	private static final float PI = 3.14159f;
	private float multiplier = 1;
	
	
	public AvoidPhysicalObject(Rover r) {
		this.rover = r;
	}
	
	
	/*
	 *  Take control when ultrasonic sensor detects an object closer
	 *  than minObstacleDistance and we are in automatic mode
	 */
	public boolean takeControl() {
		return rover.getObstacleDistance() < 60
				&& rover.getMode() == MODE.automatic;
	}
	
	
	public void suppress() {
		//_suppressed = true;
	}
	
	
	/*
	 * Rotates the rover to face directly at the closest physical
	 * object such that it minimizes ultrasonic sensor value
	 * 
	 * @Return: returns the change in orientation of the rover
	 */
	public void faceObject() {
		
		//Calculate extra rotation due to moving horizontally to object
		float extraRotation = (float)Math.atan((float)adjustDistance/(float)minObstacleDistance);
		
		//Convert to degrees
		extraRotation = (extraRotation/PI)*180f; 
				
		//Turn to face original point
		rover.turnL((90f + extraRotation)*multiplier);
		rover.waitComplete();

		
		float rotationSweepSize = 5;
		int curDistance = Integer.MAX_VALUE;
		
		while (rover.getObstacleDistance() < 150) { }
		
		
		
		//Turn right until we find minimum distance
		do {
			
			//Update distance
			if (rover.getObstacleDistance() < 150) {
				curDistance = rover.getObstacleDistance();
			}
						
			//Keep turning right
			rover.turnR(rotationSweepSize*multiplier);
			rover.waitComplete();

		} while ((rover.getObstacleDistance() < curDistance 
				|| curDistance == Integer.MAX_VALUE) //In case we detect something far in the distance
				&& !_suppressed);
		
		//Undo last turn
		rover.turnL(rotationSweepSize*multiplier);
		rover.waitComplete();
	}
	
	/*
	 * Adjusts the distance of the rover from the object
	 * 
	 * @return: returns the overall distance moved
	 */
	public void adjustDistance() {
		
		int movementSweepSize = 5;
		
		//Adjust distance from object until within error margin
		while (Math.abs(rover.getObstacleDistance() - minObstacleDistance) > errorMargin
				&& !_suppressed) {
			
			//Move forward or backwards depending on distance from object
			if (rover.getObstacleDistance() > minObstacleDistance) {
				rover.forward(movementSweepSize);
			}
			else {
				rover.backward(movementSweepSize);
			}
			rover.waitComplete();
		}
	}
	
	
	public void action() {
		
		//Keep track of relative position and orientation
		double initialXPos = rover.getUnadjustedXPos();
		double curXPos = initialXPos;
		double initialOrientation = rover.getOrientation();
		double exitOrientation = (initialOrientation > 0) ? 90.0 : -90.0;
		multiplier = (initialOrientation > 0) ? 1 : -1;
		
		//Used to work out whether we have moved around an object
		boolean goneAroundObject = false;
		rover.setSpeed(50);
		_suppressed = false;
		
		while (!_suppressed) {
			
			//Turn right and update orientation
			rover.turnR(80f*multiplier);
			rover.waitComplete();
			
			//Check if we move past goal xPos and if we do move slowly
			double futureXPos = rover.getUnadjustedXPos() + (double)minObstacleDistance*Math.cos(Math.toRadians(rover.getOrientation()));
			if (futureXPos < initialXPos
					&& goneAroundObject) { 
				rover.forward();
				while (rover.getUnadjustedXPos() > initialXPos
						&& !_suppressed) { }
				rover.stop();
				break;
			}
			else {
				rover.forward(adjustDistance);
				rover.waitComplete();
				
				//Check if we have gone around object depends on if we are sweeping up or down
				if (rover.getUnadjustedXPos() - curXPos < -3) {
					goneAroundObject = true;
				}
				curXPos = rover.getUnadjustedXPos();
				
				//Make rover face the object
				faceObject();
				
				//Adjust distance to the object
				adjustDistance();
			}
		}
		
		//Adjust orientation so that we are facing original direction
		rover.rotateTo((float)exitOrientation);
	}

}
