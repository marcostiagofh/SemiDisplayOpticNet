package projects.opticalNet.nodes.infrastructureImplementations;

import projects.opticalNet.nodes.nodeImplementations.NetworkController;
import projects.opticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.messages.Message;

public class OutputNode {
    private int index = -1;
    private NetworkNode connectedNode = null;
    private InputNode inputNode = null;

    public void setIndex (int index) {
        this.index = index;
    }

    public int getIndex () {
        return this.index;
    }

    public void setInputNode (InputNode node) {
        this.inputNode = node;
    }

    public InputNode getInputNode () {
        return this.inputNode;
    }

    public void connectToNode (NetworkNode node) {
        this.connectedNode = node;
    }

    public NetworkNode getConnectedNode () {
        return this.connectedNode;
    }

    protected void sendToConnectedNode (Message msg, NetworkController controller) {
        controller.sendDirect(msg, this.connectedNode);
    }

}
