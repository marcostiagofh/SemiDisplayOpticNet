package projects.opticalNet.nodes.nodeImplementations;

import java.util.ArrayList;
import java.util.HashMap;

import projects.opticalNet.nodes.infrastructureImplementations.InputNode;
import projects.opticalNet.nodes.infrastructureImplementations.OutputNode;

public class NetworkSwitch {

    private int index = -1;

    private int size = 0;

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

    // Type 1
    public NetworkSwitch (int minId, int maxId, ArrayList<NetworkNode> netNodes) {
        this(minId, maxId, minId, maxId, netNodes);
    }

    // Type 2
    public NetworkSwitch (
        int minId1, int maxId1, int minId2, int maxId2, ArrayList<NetworkNode> netNodes
    ) {
        this.size = maxId1 - minId1 + 1;
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

            inNode.setLinkToOutputNode(outNode);
        }
    }

    public void updateSwitch (int in, int out, int subtreeId) {
        InputNode inNode = this.inputId2Node.get(in);
        OutputNode outNode = this.outputId2Node.get(out);

        this.connectNodes(inNode, outNode);
        inNode.getConnectedNode().setChild(inNode, subtreeId);
    }

    public void updateSwitch (int in, int out) {
        InputNode inNode = this.inputId2Node.get(in);
        OutputNode outNode = this.outputId2Node.get(out);

        this.connectNodes(inNode, outNode);
        inNode.getConnectedNode().setParent(inNode);
    }

    public void connectNodes (InputNode inNode, OutputNode outNode) {
        int oldInNodeIndex = outNode.getInputNode().getIndex();
        InputNode oldInNode = this.inputId2Node.get(oldInNodeIndex);

        oldInNode.setLinkToOutputNode(inNode.getOutputNode());
        inNode.setLinkToOutputNode(outNode);
    }

    public InputNode getInputNode (int nodeId) {
        return this.inputId2Node.get(nodeId);
    }

    public OutputNode getOutputNode (int nodeId) {
        return this.outputId2Node.get(nodeId);
    }

    public void debugSwitch () {
        System.out.println("SWITCH ID: " + this.index);
        System.out.println("Input Nodes: ");
        for (int i = 0; i < this.size; i++) {
            System.out.println((i + 1) + " Node: " + this.inputNodes.get(i).getIndex());
        }
        System.out.println("Output Nodes: ");
        for (int i = 0; i < this.size; i++) {
            System.out.println((i + 1) + " Node: " + this.outputNodes.get(i).getIndex());
        }
    }

}
