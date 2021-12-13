package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HasMessage extends Message implements Comparable<HasMessage>  {

    private int dst;
    private int currId;
    private double priority;

    public HasMessage (int currId, double priority, int dst) {
        this.dst = dst;
        this.currId = currId;
        this.priority = priority;
    }

    public int getCurrId () {
        return this.currId;
    }

    public double priority () {
    	return this.priority;
    }

    public int getDst () {
        return this.dst;
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
