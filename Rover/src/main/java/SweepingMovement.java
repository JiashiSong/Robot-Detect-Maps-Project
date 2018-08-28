import lejos.robotics.subsumption.Behavior;
import java.util.*;

import lejos.robotics.Color;


public class SweepingMovement implements Behavior {
	       
    private static final int colourSensorOffset = 30; //Colour sensor distance from centre of rotation
    private static final int colourSensorWidth = 20; //Width of colour sensor units = mm
    private static final int searchDistance = 6; //This * colorsensorWidth = distance between sweeps
    private static final int sweepSpeed = 25; //Speed rover will sweep at
    
	private static boolean turnRight = true;
	private boolean _suppressed = false;
	
	private Rover rover;
	
	private List<Float> upperYPositions;
	private List<Float> lowerYPositions;
	
	public SweepingMovement(Rover r) {
		this.rover = r;
		upperYPositions = new ArrayList<Float>();
		lowerYPositions = new ArrayList<Float>();
	}
	
	
    public boolean takeControl() {
        return rover.getMode() == MODE.automatic;  
    }
    
    
    public void suppress() {
        _suppressed = true; // standard practice for suppress methods
    }
    
    
    /* 
     * Rotates rover until it aligns with boundary
     */
    public void alignWithBoundary() {
    	//Move forward so that when we rotate sensor we will be aligned
    	rover.forward(colourSensorOffset);
    	rover.waitComplete();
    	if (turnRight) {
    		//Turn right until we align with the boundary
    		rover.turnR();
			while (rover.getColourSensorColourID() != Color.BLUE 
					&& !_suppressed) { }
			rover.stop();
			
			//Move forward and then turn right to sweep new area
			rover.setOrientation(0f);
			rover.forward(colourSensorWidth*searchDistance);
			rover.waitComplete();
			rover.turnR(90f);
			rover.waitComplete();
		}
		else {
			//Turn left until we align with boundary
			rover.turnL();
			while (rover.getColourSensorColourID() != Color.BLUE
					&& !_suppressed) { }
			rover.stop();
			
			//Move forward and then turn left to sweep new area
			rover.setOrientation(0f);
			rover.forward(colourSensorWidth*searchDistance);
			rover.waitComplete();
			rover.turnL(90f);
			rover.waitComplete();
		}
    	turnRight = !turnRight;
    }
    /*
     * Aligns the Y position to the current average
     * as we know we are bounded in a rectangle
     */
    public void alignYPosition() {
    	float adjustedY = 0;
    	if (turnRight) {
    		upperYPositions.add((float)rover.getUnadjustedYPos());
    		for (float f : upperYPositions) {
    			adjustedY += f;
    		}
    		adjustedY = adjustedY / upperYPositions.size();
    	} else {
    		lowerYPositions.add((float)rover.getUnadjustedYPos());
    		for (Float f : lowerYPositions) {
    			adjustedY += f;
    		}
    		adjustedY = adjustedY / lowerYPositions.size();
    	}
    	rover.setLocation((float)rover.getUnadjustedXPos(), adjustedY);
    }
    
    public void action() {
    	
        _suppressed = false;
        while (!_suppressed) {
        	
        	//Set sweeping speed
        	rover.setSpeed(sweepSpeed);
        	
        	//Move foward until we hit other side of boundary
        	rover.forward();
        	while (rover.getColourSensorColourID() != Color.BLUE
        			&& !_suppressed) { }
        	rover.stop();
        	
        	if (!_suppressed) {
        		alignYPosition();
        	}
        	

        	if(!_suppressed) {
        		
        		//Move slowly until we edge off boundary then align with it
            	
            	rover.forward();
            	while (rover.getColourSensorColourID() == Color.BLUE
            			&& !_suppressed) { }
            	rover.stop();
            	
            	alignWithBoundary();
        	}
        }
    }
}
    

