package projects.bstOpticalNet.nodes.infrastructureImplementations;

import projects.bstOpticalNet.nodes.nodeImplementations.NetworkController;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.messages.Message;

/**
 * This node represents the Input Port of a switch. It passes each message along
 * their connected OutputNode
 */
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

    /**
     * Retrieves the OutputNode linked to this input port
     * @return      The OutputNode linked to this.
     */
    public OutputNode getOutputNode () {
        return this.outputNode;
    }

    /**
     * Update which node is connected to this port, should be used only
     * when initializing the InputNode.
     * @param node  the NetworkNode
     */
    public void connectToNode (NetworkNode node) {
        this.connectedNode = node;
    }

    /**
     * Retrieves the NetworkNode connected to the input port
     * @return      The NetworkNode conected to this.
     */
    public NetworkNode getConnectedNode () {
        return this.connectedNode;
    }

    /**
     * Update linked OutputNode and its respective link
     * @param node  the outputNode
     */
    public void setLinkToOutputNode (OutputNode node) {
        this.outputNode = node;
        node.setInputNode(this);
    }

    /**
     * Passes the message to the connected OutputNode.
     * @param msg           Message sent
     * @param controller    Simulation NetworkController node
     */
    public void sendToOutputNode (Message msg, NetworkController controller) {
        this.outputNode.sendToConnectedNode(msg, controller);

    }
}
