package projects.opticalNet.nodes.messages;

import sinalgo.nodes.messages.Message;

public class HasMessage extends Message {
    
    private int nodeId;

    public HasMessage (int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId () {
        return this.nodeId;
    }

    @Override
    public Message clone () {
        return this;
    }
}
