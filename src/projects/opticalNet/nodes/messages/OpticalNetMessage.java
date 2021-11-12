package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class OpticalNetMessage extends Message implements Comparable<OpticalNetMessage> {

    private int src;
    private int dst;

    private long routing;
    private double priority;

    public OpticalNetMessage (int src, int dst, double priority) {
        this.src = src;
        this.dst = dst;

        this.priority = priority;
        this.routing = 0;
    }

    public int getSrc () {
        return this.src;
    }

    public int getDst () {
        return this.dst;
    }

    public double getPriority () {
        return this.priority;
    }

    public void setPriority (double priority) {
        this.priority = priority;
    }

    public void setSrc (int src) {
        this.src = src;
    }

    public void setDst (int dst) {
        this.dst = dst;
    }

    public long getRouting () {
        return routing;
    }

    public void incrementRouting () {
        this.routing++;
    }

    @Override
    public int compareTo (OpticalNetMessage o) {
        int value = Double.compare(this.priority, o.priority);
        if (value == 0) { // In case tie, compare the id of the source node
            return this.dst - o.dst;

        } else {
            return value;

        }
    }

    @Override
    public Message clone () {
        return this;
    }
}
