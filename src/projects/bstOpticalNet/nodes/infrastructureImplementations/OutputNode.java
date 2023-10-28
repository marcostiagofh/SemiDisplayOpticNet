package projects.bstOpticalNet.nodes.infrastructureImplementations;

import projects.bstOpticalNet.nodes.nodeImplementations.NetworkController;
import projects.bstOpticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.messages.Message;

/**
 * This node represents the Output Port of a switch. It passes each message along
 * their connected NetworkNode
 */
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

    /**
     * Update linked InputNode
     * @param node  the inputNode
     */
    public void setInputNode (InputNode node) {
        this.inputNode = node;
    }

    /**
     * Retrieves the InputNode linked to this input port
     * @return      the InputNode linked to this.
     */
    public InputNode getInputNode () {
        return this.inputNode;
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

    /**
     * Sends the message to it's connected node using the controller node.
     * @param msg           message sent
     * @param controller    simulation NetworkController node
     */
    protected void sendToConnectedNode (Message msg, NetworkController controller) {
        controller.sendDirect(msg, this.connectedNode);
    }

}
