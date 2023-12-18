package projects.bstOpticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * This message informs the controller which nodes want to rout a message
 * in one specific round, where that message is heading towards and the priority
 * of that message.
 */
public class HasMessage extends Message implements Comparable<HasMessage>  {

    private int dst;
    private int currId;
    private double priority;

    /**
     * Creates the HasMessage informing that the node with currId has a
     * message with priority to send to the dst node
     * @param currId    the id of the node with the message
     * @param priority  the priority of the message
     * @param dst       the id of the destination node
     */
    public HasMessage (int currId, double priority, int dst) {
        this.dst = dst;
        this.currId = currId;
        this.priority = priority;
    }

    /**
     * Getter for the id of the current node holder of the message
     * @return          the id of the node current holder of the message
     */
    public int getCurrId () {
        return this.currId;
    }

    /**
     * Getter for the message priority
     * @return          the message priority
     */
    public double priority () {
        return this.priority;
    }

    /**
     * Getter for the message destination node id
     * @return          the destination node id
     */
    public int getDst () {
        return this.dst;
    }

    /**
     * Comparator between two HasMessages, first comparing its priority,
     * then its destination node id.
     */
    @Override
    public int compareTo (HasMessage o) {
        int value = Double.compare(this.priority, o.priority);
        if (value == 0) { // In case tie, compare the id of the source node
            return this.currId - o.currId;

        } else {
            return value;

        }
    }

    public void debugMsg () {
        System.out.println(
            "has-msg"
            + " curId: " + this.getCurrId() + " dstId: " + this.getDst()
        );
    }

    @Override
    public Message clone () {
        return this;
    }
}
