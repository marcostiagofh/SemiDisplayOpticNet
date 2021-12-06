package projects.opticalNet.nodes.messages;

import projects.opticalNet.nodes.infrastructureImplementations.Direction;

import sinalgo.nodes.messages.Message;

public class HasMessage extends Message implements Comparable<HasMessage>  {

    private int currId;
    private double priority;
    private Direction direction;

    public HasMessage (int currId, double priority, Direction direction) {
        this.currId = currId;
        this.priority = priority;
        this.direction = direction;
    }

    public int getCurrId () {
        return this.currId;
    }
    
    public double priority () {
    	return this.priority;
    }

    public Direction getDirection () {
        return this.direction;
    }

    @Override
    public int compareTo (HasMessage o) {
        int value = Double.compare(this.priority, o.priority);
        if (value == 0) { // In case tie, compare the id of the source node
            return this.currId - o.currId;

        } else {
            return value;

        }
    }

    @Override
    public Message clone () {
        return this;
    }
}
