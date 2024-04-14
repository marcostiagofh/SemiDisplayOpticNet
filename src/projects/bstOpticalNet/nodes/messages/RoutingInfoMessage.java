package projects.bstOpticalNet.nodes.messages;

import projects.bstOpticalNet.nodes.nodeImplementations.NetworkSwitch;
import sinalgo.nodes.messages.Message;

/**
 * Message that informs the NetworkNodes how many times the OpticalNetMessage enclosed
 * should be routed, and where it should be routed to.
 */
public class RoutingInfoMessage extends Message implements Comparable<RoutingInfoMessage> {

    private int nodeId = -1;
    private int routNodeId = -1;
    private int routingTimes = 0;
    private OpticalNetMessage routedMsg = null;
    private boolean heuristicLink = false;
    private NetworkSwitch swt = null;

    /**
     * Creates a message that should be routed routingTimes.
     * @param routingTimes  How many times this message should be routed.
     */
    public RoutingInfoMessage (int routingTimes) {
        this.routingTimes = routingTimes;
    }

    public void setHeuristicLink(boolean value) {
    	this.heuristicLink = value;
    }
    
    public boolean getHeuristicLink() {
    	return this.heuristicLink;
    }
    
	public void setSwt(NetworkSwitch swt) {
		this.swt = swt;
	}
	
	public NetworkSwitch getSwt() {
		return this.swt;
	}
	
	/**
     * Sets the node id from the node owner of the message
     * @param nodeId        the nodeId for the holder node.
     */
    public void setNodeId (int nodeId) {
        this.nodeId = nodeId;

    }

    /**
     * Sets the OpticalNetMessage that should be routed alongside this message
     * @param optmsg        the OpticalNetMessage
     */
    public void setRoutedMsg (OpticalNetMessage optmsg) {
        this.routedMsg = optmsg;

    }

    /**
     * Sets the id of the node where the message should be routed to.
     * @param routNodeId    the rout node id
     */
    public void setRoutNodeId (int routNodeId) {
        this.routNodeId = routNodeId;

    }

    /**
     * Getter for the rout node id
     * @return              the routNodeId
     */
    public int getRoutNodeId () {
        return routNodeId;

    }

    /**
     * Decrement the number of times the message needs to be routed, called when it is
     * routed one time.
     */
    public void decreaseRoutingTimes () {
        this.routingTimes--;

    }

    /**
     * Getter for the id of the message holder node.
     * @return  the nodeId
     */
    public int getNodeId () {
        return this.nodeId;

    }

    /**
     * Getter for the number of times this message needs to be routed
     * @return the routing times
     */
    public int getRoutingTimes () {
        return this.routingTimes;

    }

    /**
     * Getter for the source node id of the routed message
     * @return the routed msg source id.
     */
    public int getSrc () {
        return this.routedMsg.getSrc();

    }

    /**
     * Getter for the destination node id of the routed message
     * @return the routed msg destination id.
     */
    public int getDst () {
        return this.routedMsg.getDst();

    }

    /**
     * Getter for the routed message
     * @return the routed msg
     */
    public OpticalNetMessage getRoutedMsg () {
        return this.routedMsg;

    }

    /**
     * Getter for the routed message priority
     * @return the routed message priority
     */
    public double getMessagePriority () {
        return this.routedMsg.getPriority();

    }

    /**
     * Comparator between two RoutingInfoMessage, first comparing its priority,
     * then its current holder node id.
     */
    @Override
    public int compareTo (RoutingInfoMessage o) {
        int value = Double.compare(this.getMessagePriority(), o.getMessagePriority());
        if (value == 0) { // In case tie, compare the id of the source node
            return this.nodeId - o.nodeId;

        } else {
            return value;

        }
    }


    public void debugMsg () {
        System.out.println(
            "rout-msg"
            + " srcId: " + this.getSrc() + " dstId: " + this.getDst()
            + " curId: " + this.getNodeId() + " nxtId: " + this.getRoutNodeId()
            + " routingTimes: " + this.getRoutingTimes()
        );
    }

    @Override
    public Message clone () {
        return this;
    }


}
