package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class ConnectNodesMessage extends Message implements Comparable<ConnectNodesMessage> {

	private int from;
    private int to;
    private int subtreeId;
    private int priority;

    public ConnectNodesMessage (int from, int to, int priority) {
    	this.from = from;
    	this.to = to;
    	this.subtreeId = -1;
    	this.priority = priority;
    }

    public ConnectNodesMessage (int from, int to, int subtreeId, int priority) {
    	this.from = from;
    	this.to = to;
    	this.subtreeId = subtreeId;
    	this.priority = priority;
    }

    public int getFrom () {
    	return this.from;
    }

    public int getTo () {
        return this.to;
    }

    public int getSubtreeId () {
    	return this.subtreeId;
    }

    public int getPriority () {
    	return this.priority;
    }
    
    public void setFrom (int from) {
    	this.from = from;
    }

    public void setTo (int to) {
        this.to = to;
    }

    public void setSubtreeId (int subtreeId) {
    	this.subtreeId = subtreeId;
    }

    @Override
    public int compareTo (ConnectNodesMessage o) {
        return Integer.compare(this.priority, o.priority);
    }
    
    @Override
    public Message clone () {
        return this;
    }


}
