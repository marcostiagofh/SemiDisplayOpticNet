package projects.bstOpticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * Message that will be routed along the network from the src node
 * up to it's destination dst
 */
public class OpticalNetMessage extends Message implements Comparable<OpticalNetMessage> {

    private int src;
    private int dst;

    private long routing;
    private double priority;

    /**
     * Creates the message that will be routed from the src node up to it's dst node.
     * Their priority indicates which message will be prioritized in a conflict.
     * @param src       src node id
     * @param dst       dst node id
     * @param priority  message priority
     */
    public OpticalNetMessage (int src, int dst, double priority) {
        this.src = src;
        this.dst = dst;

        this.priority = priority;
        this.routing = 0;
    }

    /**
     * Getter for the src node id
     * @return          src node id
     */
    public int getSrc () {
        return this.src;
    }

    /**
     * Getter for the dst node id
     * @return          dst node id
     */
    public int getDst () {
        return this.dst;
    }

    /**
     * Getter for the message priority
     * @return          message priority
     */
    public double getPriority () {
        return this.priority;
    }

    /**
     * Getter for the number of routings this message went through
     * @return          how many routings this message went through
     */
    public long getRouting () {
        return routing;
    }

    /**
     * Increment the number of routings this message went through
     */
    public void incrementRouting () {
        this.routing++;
    }

    /**
     * Comparator between two OpticalNetMessages, first comparing its priority,
     * then its destination node id.
     */
    @Override
    public int compareTo (OpticalNetMessage o) {
        int value = Double.compare(this.priority, o.priority);
        if (value == 0) { // In case tie, compare the id of the source node
            return this.dst - o.dst;

        } else {
            return value;

        }
    }

    public void debugMsg () {
        System.out.println(
            "opt-msg"
            + " srcId: " + this.getSrc() + " dstId: " + this.getDst()
        );
    }

    @Override
    public Message clone () {
        return this;
    }
}
