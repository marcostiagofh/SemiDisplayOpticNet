package projects.opticalNet.nodes.infrastructureImplementations;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;

public abstract class SynchronizerLayer extends Node {

    private int MAX_TIMESLOT = 7;
    private int timeslot;
    private long round;

    public int getCurrentTimeSlot () {
        return this.timeslot;
    }

    public long getCurrentRound () {
        return this.round;
    }

    @Override
    public void init () {
        this.timeslot = 0;
    }

    @Override
    public void postStep () {
        switch (this.timeslot) {
            case 0:
                controllerStep();
                break;
            case 1: break;
            case 2: break;
            case 3:
                nodeStep();
                break;
            case 4: break;
            case 5: break;
            case 6:
                posRound();
                this.round++;
                break;
        }
        this.timeslot = (this.timeslot + 1) % MAX_TIMESLOT;
    }

    public void nodeStep () { }

    public void controllerStep () { }

    public void posRound () { }

    // unused methods ----------------------------------------------------
	@Override
	public void preStep () { }

	@Override
	public void neighborhoodChange () { }

	@Override
	public void checkRequirements () throws WrongConfigurationException { }

}
