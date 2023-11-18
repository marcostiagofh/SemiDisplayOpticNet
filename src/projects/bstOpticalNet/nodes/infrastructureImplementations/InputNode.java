package projects.bstOpticalNet.nodes.infrastructureImplementations;

import projects.bstOpticalNet.nodes.nodeImplementations.NetworkController;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * This node represents the Input Port of a switch. It passes each message along
 * their connected OutputNode
 */
public class InputNode {
    private int index = -1;
    private NetworkNode connectedNode = null;
    private OutputNode outputNode = null;
    private boolean active = false;

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

    public boolean isActive () {
        return this.active;
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

    public int getOutputConnectedNodeId () {
        return (this.getOutputNode() == null ? -1 : this.getOutputNode().getConnectedNode().getId());
    }

    /**
     * Update linked OutputNode and its respective link
     * @param node      the outputNode
     * @param active    true if this port's link active
     */
    public void setLinkToOutputNode (OutputNode node, boolean active) {
        this.outputNode = node;
        this.active = active;

        node.setInputNode(this, active);
    }

    /**
     * Passes the message to the connected OutputNode.
     * @param msg           Message sent
     * @param controller    Simulation NetworkController node
     */
    public void sendToOutputNode (Message msg, NetworkController controller) {
        if (!this.active) {
            Tools.fatalError("Sending message through inactive input node");

        }

        this.outputNode.sendToConnectedNode(msg, controller);

    }

    public void debugPort () {
        System.out.println(this.getConnectedNode().getId() + " -> " + this.getOutputConnectedNodeId() + " " + this.isActive());
    }
}
