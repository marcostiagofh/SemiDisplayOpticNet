package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class RoutingInfoMessage extends Message implements Comparable<RoutingInfoMessage> {

    private int nodeId = -1;
    private int routingTimes = 0;
    private OpticalNetMessage routedMsg = null;

    public RoutingInfoMessage (int routingTimes) {
        this.routingTimes = routingTimes;
    }

    public void setNodeId (int nodeId) {
        this.nodeId = nodeId;

    }

    public void setRoutedMsg (OpticalNetMessage optmsg) {
        this.routedMsg = optmsg;

    }

    public void decreaseRoutingTimes () {
        this.routingTimes--;

    }

    public int getNodeId () {
        return this.nodeId;

    }

    public int getRoutingTimes () {
        return this.routingTimes;

    }

    public OpticalNetMessage getRoutedMsg () {
        return this.routedMsg;

    }

    public double getMessagePriority () {
        return this.routedMsg.getPriority();

    }

    @Override
    public int compareTo (RoutingInfoMessage o) {
        int value = Double.compare(this.getMessagePriority(), o.getMessagePriority());
        if (value == 0) { // In case tie, compare the id of the source node
            return this.nodeId - o.nodeId;

        } else {
            return value;

        }
    }

    @Override
    public Message clone () {
        return this;
    }


}
