package projects.bstOpticalNet.nodes.nodeImplementations;

import java.util.ArrayList;
import java.util.HashMap;

import projects.bstOpticalNet.nodes.infrastructureImplementations.InputNode;
import projects.bstOpticalNet.nodes.infrastructureImplementations.OutputNode;
import projects.bstOpticalNet.nodes.models.AvailablePorts;
import sinalgo.tools.Tools;

public class NetworkSwitch {

    private int index = -1;

    private int size = 0;
    private int offset = -1;

    private HashMap<Integer, InputNode> inputId2Node;
    private HashMap<Integer, OutputNode> outputId2Node;

    private ArrayList<InputNode> inputNodes;
    private ArrayList<OutputNode> outputNodes;

    public void setIndex (int index) {
        this.index = index;
    }

    public int getIndex () {
        return this.index;
    }

    public int getOffset () {
        return this.offset;

    }

    /**
     * Constructor for switches belonging to clusters of type 1. Sets the ranges for the input
     * and output ports as the same, minId to maxId.
     * @param minId     the smallest id in the range of nodes in the switch
     * @param maxId     the biggest id in the range of nodes in the switch
     * @param netNodes  the NetworkNodes
     */
    public NetworkSwitch (int minId, int maxId, int offset, ArrayList<NetworkNode> netNodes) {
        this(minId, maxId, minId, maxId, offset, netNodes);
    }

    /**
     * Constructor for switches belonging to clusters of type 2 and 1. Creates a switch with
     * inputNode connected with the nodes in the range minId1 to maxId1 and OutputNode connected
     * with the nodes in the range minId2 to maxId2. Then fill it with dummy links.
     * @param minId1    the smallest id in the range of nodes connected to the input ports
     * @param maxId1    the biggest id in the range of nodes connected to the input ports
     * @param minId2    the smallest id in the range of nodes connected to the output ports
     * @param maxId2    the biggest id in the range of nodes connected to the output ports
     * @param netNodes  the NetworkNodes
     */
    public NetworkSwitch (
        int minId1, int maxId1, int minId2, int maxId2, int offset, ArrayList<NetworkNode> netNodes
    ) {
        this.size = maxId1 - minId1 + 1;
        this.offset = offset;
        this.inputId2Node = new HashMap<>();
        this.outputId2Node = new HashMap<>();
        this.inputNodes = new ArrayList<>();
        this.outputNodes = new ArrayList<>();

        for (int i = 0; i < this.size; ++i) {
            Integer networkNodeId = (minId1 + i < netNodes.size() ? minId1 + i : netNodes.size());

            InputNode inNode = new InputNode();
            inNode.setIndex(networkNodeId);

            NetworkNode node = netNodes.get(networkNodeId - 1);
            inNode.connectToNode(node);

            this.inputNodes.add(inNode);
            this.inputId2Node.put(networkNodeId, inNode);
        }

        for (int i = 0; i < this.size; ++i) {
            Integer networkNodeId = (minId2 + i < netNodes.size() ? minId2 + i : netNodes.size());

            OutputNode outNode = new OutputNode();
            outNode.setIndex(networkNodeId);

            NetworkNode node = netNodes.get(networkNodeId - 1);
            outNode.connectToNode(node);

            this.outputNodes.add(outNode);
            this.outputId2Node.put(networkNodeId, outNode);
        }

        for (int i = 0; i < this.size; ++i) {
            InputNode inNode = this.inputNodes.get(i);
            OutputNode outNode = this.outputNodes.get((i + 1 == this.size ? 0 : i + 1));

            inNode.setLinkToOutputNode(outNode, false);
        }
    }

    public int getConnectedInputNodeId (int out) {
        OutputNode outNode = this.outputId2Node.get(out);

        return outNode.getInputConnectedNodeId();
    }

    public int getConnectedOutputNodeId (int in) {
        InputNode inNode = this.inputId2Node.get(in);

        return inNode.getOutputConnectedNodeId();
    }

    public AvailablePorts getAvailablePorts (int in, int out) {
        InputNode inNode = this.inputId2Node.get(in);
        if (inNode == null) {
        	if (this.outputId2Node.get(in) == null) {
        		Tools.fatalError("Wrong cluster to check available ports");

        	}

        	return AvailablePorts.NONE;

        }
        OutputNode outNode = this.outputId2Node.get(out);

        boolean inActive = inNode.isActive();
        boolean outActive = outNode.isActive();

        if (!inActive && !outActive) {
            return AvailablePorts.BOTH;

        } else if (!inActive) {
            return AvailablePorts.INPUT;

        } else if (!outActive) {
            return AvailablePorts.OUTPUT;

        }

        return AvailablePorts.NONE;
    }

    public void removeLink (int in, int out) {
        InputNode inNode = this.inputId2Node.get(in);
        OutputNode outNode = this.outputId2Node.get(out);

        inNode.setLinkToOutputNode(outNode, false);
    }

    /**
     * Function that sets the link between the InputNode connected to the NetworkNode in and the
     * OutputNode connected to the NetworkNode out
     * @param in    the parent network node id
     * @param out   the child network node id
     */
    public void updateParent (int in, int out) {
        InputNode inNode = this.inputId2Node.get(in);
        OutputNode outNode = this.outputId2Node.get(out);

        this.connectNodes(inNode, outNode);
        inNode.getConnectedNode().setParent(inNode);
    }

    /**
     * Function that sets the link between the InputNode connected to the NetworkNode in and the
     * OutputNode connected to the NetworkNode out
     * @param in    the child network node id
     * @param out   the parent network node id
     */
    public void updateChild (int in, int out) {
        InputNode inNode = this.inputId2Node.get(in);
        OutputNode outNode = this.outputId2Node.get(out);

        this.connectNodes(inNode, outNode);
        inNode.getConnectedNode().setChild(inNode);
    }

    /**
     * This method actually updates the switch links, linking the old inputNode to the
     * old output node and the new InputNode inNode to the new OutputNode outNode.
     * @param inNode    the inputNode
     * @param outNode   the outputNode
     */
    private void connectNodes (InputNode inNode, OutputNode outNode) {
        InputNode oldInNode = outNode.getInputNode();
        OutputNode oldOutNode = inNode.getOutputNode();

        oldInNode.setLinkToOutputNode(oldOutNode, false);
        inNode.setLinkToOutputNode(outNode, true);
    }

    /**
     * Getter for the InputNode connected to the NetworkNode with nodeId
     * @param nodeId    the id of the network node
     * @return          the connected InputNode
     */
    public InputNode getInputNode (int nodeId) {
        return this.inputId2Node.get(nodeId);
    }

    /**
     * Getter for the OutputNode connected to the NetworkNode with nodeId
     * @param nodeId    the id of the network node
     * @return          the connected OutputNode
     */
    public OutputNode getOutputNode (int nodeId) {
        return this.outputId2Node.get(nodeId);
    }

    /**
     * Control method to debug the representation of the switch, only prints the InputNodes
     * and OutputNodes in the switch, not their links
     */
    public void debugSwitch () {
        System.out.println("SWITCH ID: " + this.index);
        System.out.println("Links: ");
        for (int i = 0; i < this.size; i++) {
            InputNode in = this.inputNodes.get(i);
            OutputNode out = in.getOutputNode();

            System.out.println(i + "th link ----------");
            in.debugPort();
            out.debugPort();
        }
    }

}
