package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class ConnectNodesMessage extends Message {

	private int from;
    private int to;
    private int subtreeId;

    public ConnectNodesMessage (int to, int from) {
    	this.from = from;
    	this.to = to;
    	this.subtreeId = -1;
    }

    public ConnectNodesMessage (int to, int from, int subtreeId) {
    	this.from = from;
    	this.to = to;
    	this.subtreeId = subtreeId;
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
    public Message clone () {
        return this;
    }


}
