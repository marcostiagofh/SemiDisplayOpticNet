package projects.opticalNet.nodes.infrastructureImplementations;

import projects.opticalNet.nodes.nodeImplementations.NetworkController;
import projects.opticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.messages.Message;

public class InputNode {
    private int index = -1;
    private NetworkNode connectedNode = null;
    private OutputNode outputNode = null;

    public void setIndex (int index) {
        this.index = index;
    }

    public int getIndex () {
        return this.index;
    }

    public OutputNode getOutputNode () {
        return this.outputNode;
    }

    public void connectToNode (NetworkNode node) {
        this.connectedNode = node;
    }

    public NetworkNode getConnectedNode () {
        return this.connectedNode;
    }

    public void setLinkToOutputNode (OutputNode node) {
        this.outputNode = node;
        node.setInputNode(this);
    }

    public void sendToOutputNode (Message msg, NetworkController controller) {
        this.outputNode.sendToConnectedNode(msg, controller);

    }
}
