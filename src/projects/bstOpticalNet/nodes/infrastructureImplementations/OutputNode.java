package projects.bstOpticalNet.nodes.infrastructureImplementations;

import projects.bstOpticalNet.nodes.nodeImplementations.NetworkController;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkNode;
import projects.bstOpticalNet.nodes.nodeImplementations.HeuristicController;

import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * This node represents the Output Port of a switch. It passes each message along
 * their connected NetworkNode
 */
public class OutputNode {
    private int index = -1;
    private NetworkNode connectedNode = null;
    private InputNode inputNode = null;
    private boolean active = false;

    public void setIndex (int index) {
        this.index = index;
    }

    public int getIndex () {
        return this.index;
    }

    /**
     * Update linked InputNode
     * @param node      the inputNode
     * @param active    true if this port's link is active
     */
    public void setInputNode (InputNode node, boolean active) {
        this.inputNode = node;
        this.active = active;
    }

    /**
     * Retrieves the InputNode linked to this input port
     * @return      the InputNode linked to this.
     */
    public InputNode getInputNode () {
        return this.inputNode;
    }

    public boolean isActive () {
        return this.active;
    }

    /**
     * Update which node is connected to this port, should only be used
     * when initializing the OutputNode.
     * @param node
     */
    public void connectToNode (NetworkNode node) {
        this.connectedNode = node;
    }

    /**
     * Retrieves the NetworkNode connected to the output port
     * @return      the NetworkNode conected to this.
     */
    public NetworkNode getConnectedNode () {
        return this.connectedNode;
    }

    public int getInputConnectedNodeId () {
        return (this.getInputNode() == null ? -1 : this.getInputNode().getConnectedNode().getId());
    }

    /**
     * Sends the message to it's connected node using the controller node.
     * @param msg           message sent
     * @param controller    simulation NetworkController node
     */
    protected void sendToConnectedNode (Message msg, HeuristicController controller) {
        if (!this.active) {
            Tools.fatalError("Sending message through inactive input node");

        }

        controller.sendDirect(msg, this.connectedNode);
    }

    public void debugPort () {
        System.out.println(this.getInputConnectedNodeId() + " -> " + this.getConnectedNode().getId() + " " + this.isActive());
    }
}
