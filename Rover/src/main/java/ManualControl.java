import lejos.robotics.subsumption.Behavior;

public class ManualControl implements Behavior {
	       
	private boolean _suppressed = false;
	
	private Rover rover;
	
	public ManualControl(Rover r) {
		this.rover = r;
	}
	
    public boolean takeControl() {
        return rover.getMode() == MODE.manual;  
    }
    
    
    public void suppress() {
        _suppressed = true;
    }
    
    public void action() {
        _suppressed = false;
    }
}
    

