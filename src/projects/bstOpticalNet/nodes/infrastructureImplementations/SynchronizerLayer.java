package projects.bstOpticalNet.nodes.infrastructureImplementations;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;

public abstract class SynchronizerLayer extends Node {

    private int MAX_TIMESLOT = 4;
    private int timeslot;
    private long round;

    /**
     * Retrieves the current timeslot, this timeslot is sincronized among the NetworkNodes
     * and controller node of the simulation
     * @return  the current timeslot.
     */
    public int getCurrentTimeSlot () {
        return this.timeslot;
    }

    /**
     * Retrieves the current round, this round is sincronized among the NetworkNodes
     * and controller node of the simulation
     * @return  the current round.
     */
    public long getCurrentRound () {
        return this.round;
    }

    /**
     * Sets the current timeslot as 0, called when initializing the simulation.
     */
    @Override
    public void init () {
        this.timeslot = 0;
    }

    /**
     * Occurs after the simulation step, where each node handles their received messages.
     * It's responsible to call the apropriated function for each timeslot. This functions
     * are called in the order:
     * nodeInformStep -> controllerStep -> nodeRoutingStep -> logRoundResults
     * When the timeslot is reseted and the round incremented.
     */
    @Override
    public void postStep () {
        switch (this.timeslot) {
            case 0:
                nodeInformStep();
                break;
            case 1:
                controllerStep();
                break;
            case 2:
                nodeRoutingStep();
                break;
            case 3:
                logRoundResults();
                posRound();
                this.round++;
                break;
        }

        this.timeslot = (this.timeslot + 1) % MAX_TIMESLOT;
    }

    /**
     * This node does not act in this step.
     */
    public void nodeInformStep () { }

    /**
     * This node does not act in this step.
     */
    public void controllerStep () { }

    /**
     * This node does not act in this step.
     */
    public void nodeRoutingStep () { }

    /**
     * This node does not act in this step.
     */
    public void logRoundResults () { }


    public void posRound () { }

    // unused methods ----------------------------------------------------
    @Override
    public void preStep () { }

    @Override
    public void neighborhoodChange () { }

    @Override
    public void checkRequirements () throws WrongConfigurationException { }

}
