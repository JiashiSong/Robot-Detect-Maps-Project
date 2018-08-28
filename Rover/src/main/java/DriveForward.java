import lejos.robotics.subsumption.Behavior;



class DriveForward implements Behavior {
    private boolean _suppressed = false;
    private Rover rover;

    public DriveForward(Rover rover) {
        this.rover = rover;
    }

    public boolean takeControl()
    {
        return true;  // this behavior always wants control.
    }

    public void suppress()
    {
        _suppressed = true;// standard practice for suppress methods
    }

    public void action()
    {
        _suppressed = false;
        while (!_suppressed)
        {
            rover.forward();
            Thread.yield(); //don't exit till suppressed
        }
    }
}