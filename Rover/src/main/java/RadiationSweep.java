import lejos.robotics.subsumption.Behavior;

import lejos.robotics.Color;

public class RadiationSweep implements Behavior{
	
	private Rover rover;
	
	public RadiationSweep(Rover r) {
		this.rover = r;
	}
	
	public void suppress() {
		//Never suppress this behaviour
	}
	
	public boolean takeControl() {
		return rover.getColourSensorColourID() == Color.GREEN
        		&& rover.getMode() == MODE.automatic;  
	}
	
	public void action() {
		rover.setRadiation(true);
		rover.setSpeed(20);
		
		//Do 360 sweep to find the rover landing
		rover.turnL(360f);
		rover.waitComplete();
		rover.setRadiation(false);
		//Head home
		rover.goTo((float)rover.getXPos(), 0);
		rover.goTo(0, 0);
		rover.rotateTo(90f);
		
		rover.setManualControl();
	}
}
