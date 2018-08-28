import lejos.robotics.subsumption.Behavior;

class HitObject implements Behavior {

    private Rover rover;

    // Default reverse distance.
    private int reverse_distance = 50;

    /**
     * Constructor.
     *
     * @param rover rover to take control of if the touchSensor is triggered.
     */
    public HitObject(Rover rover) {
        this.rover = rover;
    }

    /**
     * Checks whether the touchSensor is pressed;
     *
     * @return true if either or both touch sensors have been pressed.
     */
    private boolean checkCollision() {
        return rover.bumperPressed();
    }

    /**
     * Defines the trigger to take control of the rover and run the code in action();
     */
    public boolean takeControl() {
        return checkCollision();
    }

    /**
     * Suppresses this behavior if other behaviors take priority.
     */
    public void suppress() {
        // Since this is highest priority behavior, suppress will never be called.
    }
    
    /*
     * Searchs a 60 degree cone in front of the rover at 3 degree resolution
     * for the closest distance to object then faces that object
     */
	public void faceObject() {
		
		double closestAngle = 0;
		double closestDistance = Double.MAX_VALUE;
		for (int i=0; i<12; i++)
		{
			rover.turnR(3f);
			rover.waitComplete();
			if (rover.getObstacleDistance()< (float)closestDistance) {
				closestAngle = rover.getOrientation();
				closestDistance = (float)rover.getObstacleDistance();
			}
		}
		rover.turnL(30f);
		rover.waitComplete();
		for (int i=0; i<12; i++)
		{
			rover.turnL(3f);
			rover.waitComplete();
			if (rover.getObstacleDistance()< closestDistance) {
				closestAngle = rover.getOrientation();
				closestDistance = (float)rover.getObstacleDistance();
			}
		}
		
		rover.rotateTo((float)closestAngle);
		rover.waitComplete();
		
	}


    /**
     * Runs when the touch sensor is pressed.
     * Reverses and turns in the opposite direction of the collision side and returns control to the rover.
     */
    public void action() {
        // Back up.
        rover.backward(reverse_distance);
        rover.waitComplete();
        
        //Turn right to avoid obstacle
        faceObject();
    }
}