import lejos.robotics.Color;
import lejos.robotics.subsumption.Behavior;

public class AvoidCrater implements Behavior {

private Rover rover;
private boolean _suppressed = false;
private static final float minMovementSize = 5; //units = mm, tradeoff of accuracy vs speed as it gets smaller

private boolean firstSwivel = true;
private boolean goingRight;
private boolean sweepingUp;
private int numSwivels;

//Colour values
  private static final int craterColour = Color.BLACK;

  
public AvoidCrater(Rover r) {
  this.rover = r;
}

/*
 * Takes control when we colour sensor detects crater in automatic mode
 */
public boolean takeControl() {
  return rover.getColourSensorColourID() == craterColour
      && rover.getMode() == MODE.automatic;
}


public void suppress() {
  //_suppressed = true;
}

/*
 * Swivels the rover back and forth an increasing amount
 * until it finds the line of the crater again
 * 
 */
public void findLine() {
  
  float degreesToRotate = 3; //Initial swivel amount
  boolean turnRight = true; //Whether we swivel left or right
  numSwivels++;
  
  //Start swivelling until crater line found
  while (rover.getColourSensorColourID() != craterColour
      && !_suppressed) {
    if (turnRight) {
      rover.turnR(degreesToRotate);
    }
    else {
      rover.turnL(degreesToRotate);
    }
    rover.waitComplete();
    //Update direction and increase the amount of rotation
        
    turnRight = !turnRight;
    degreesToRotate += 4; 		
  }
  if (firstSwivel) {
  	goingRight = !turnRight;
  	firstSwivel = false;
  }

  
}


/*
 * Attempts to move around a crater object
 */
public void action() {
	
  rover.setSpeed(25);
  _suppressed = false;
  firstSwivel = true;
  
  //Keep track of relative orientation and position
  double initialXPos = rover.getUnadjustedXPos();
  double curXPos = initialXPos;
  double initialOrientation = (rover.getOrientation() > 0) ? 90 : -90;
  sweepingUp = initialOrientation > 0;
  boolean goneAroundObject = false;
  numSwivels = 0;
  
  //Continue going around crater unless suppressed
  while (!_suppressed) {
	
    //Go forward until we lose the line
	double startY = rover.getUnadjustedYPos();
    rover.forward();
    while ((rover.getColourSensorColourID() == craterColour)
        && !_suppressed) { }
    rover.stop();	
    
    //Edge case fail safe
    if (Math.abs(rover.getUnadjustedYPos() - startY) > 15
    	&& firstSwivel) {
    	break;
    }
    
    //Find the line
    findLine();
    
    //Check if gone around the object
    if (goingRight && sweepingUp
    		&& rover.getUnadjustedXPos() - curXPos < -2) {
    	goneAroundObject = true;
    } else if (goingRight && !sweepingUp
    		&& rover.getUnadjustedXPos() - curXPos > 2) {
    	goneAroundObject = true;
    } else if (!goingRight && sweepingUp
    		&& rover.getUnadjustedXPos() - curXPos > 2) {
    	goneAroundObject = true;
    } else if (!goingRight && !sweepingUp
    		&& rover.getUnadjustedXPos() - curXPos < -2) {
    	goneAroundObject = true;
    }
    curXPos = rover.getUnadjustedXPos();

    //Check if at destination and goneAroundObject
    if (goneAroundObject) {
	    if (goingRight && sweepingUp
	    		&& curXPos < initialXPos) {
	    	break;
	    } else if (goingRight && !sweepingUp
	    		&& curXPos > initialXPos) {
	    	break;
	    } else if (!goingRight && sweepingUp
	    		&& curXPos > initialXPos) {
	    	break;
	    } else if (!goingRight && !sweepingUp
	    		&& curXPos < initialXPos) {
	    	break;
	    }
    }
    
    
  }	
  
  //Readjust rover so it's facing initial orientation
  rover.rotateTo((float)initialOrientation);
  rover.waitComplete();	
  
  //Get off of crater line so we can continue movement
  rover.forward();
  while (rover.getColourSensorColourID() == craterColour
      && !_suppressed) { }
  rover.stop();
}
}