package projects.opticalNet.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;

import projects.opticalNet.nodes.messages.ConnectNodesMessage;
import projects.opticalNet.nodes.infrastructureImplementations.InputNode;
import projects.opticalNet.nodes.infrastructureImplementations.OutputNode;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class NetworkSwitch extends Node {

    private int index = -1;

    private int size = 0;

    private HashMap<Integer, InputNode> inputId2Node;
    private HashMap<Integer, OutputNode> outputId2Node;

    private ArrayList<InputNode> inputNodes;
    private ArrayList<OutputNode> outputNodes;

    private int width = 0;
    private int height = 0;

    private int unitSize = 0;
    private int internalNodeSize = 0;

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
            InputNode inNode = new InputNode();
            inNode.finishInitializationWithDefaultModels(true);
            inNode.setIndex(minId1 + i);

            NetworkNode node = netNodes.get(minId1 + i - 1);
            node.connectToInputNode(inNode);
            inNode.connectToNode(node);

            this.inputNodes.add(inNode);
            this.inputId2Node.put(minId1 + i, inNode);
        }

        for (int i = 0; i < this.size; ++i) {
            OutputNode outNode = new OutputNode();
            outNode.finishInitializationWithDefaultModels(true);
            outNode.setIndex(minId2 + i);

            NetworkNode node = netNodes.get(minId2 + i - 1);
            outNode.connectToNode(node);

            this.outputNodes.add(outNode);
            this.outputId2Node.put(minId2 + i, outNode);
        }

        for (int i = 0; i < this.size; ++i) {
            InputNode inNode = this.inputNodes.get(i);
            OutputNode outNode = this.outputNodes.get((i + 1 == this.size ? 0 : i + 1));

            inNode.addLinkToOutputNode(outNode);
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

        oldInNode.updateLinkToOutputNode(inNode.getOutputNode());
        inNode.updateLinkToOutputNode(outNode);
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

    @Override
    public void init() {
    }

    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (!(msg instanceof ConnectNodesMessage)) {
                continue;

            }

            ConnectNodesMessage conmsg = (ConnectNodesMessage) msg;
            if (conmsg.getSubtreeId() == -1) {
            	this.updateSwitch(conmsg.getFrom(), conmsg.getTo());

            } else {
            	this.updateSwitch(conmsg.getFrom(), conmsg.getTo(), conmsg.getSubtreeId());

            }
        }
    }

    @Override
    public void preStep () { }

    @Override
    public void neighborhoodChange () { }

    @Override
    public void postStep () { }

    @Override
    public void checkRequirements () throws WrongConfigurationException { }

    //-----------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------
    // Drawing methods of the switch node
    //-----------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------

    private void updateInternalNodesPositions () {
        double unitSize = this.height / (double)((6 * this.size) - 1);
        double xCof = this.getPosition().xCoord - (this.width/2.0) + (this.internalNodeSize/2.0);
        double yCof = this.getPosition().yCoord - (this.height/2.0) + (this.internalNodeSize/2.0);
        for (int i = 0; i < this.size; ++i) {
            this.inputNodes.get(i).setPosition(xCof, yCof + (6 * unitSize * i) ,0);
        }

        xCof = this.getPosition().xCoord + (this.width/2.0) - (this.internalNodeSize/2.0);
        yCof = this.getPosition().yCoord - (this.height/2.0) + (this.internalNodeSize/2.0);
        for (int i = 0; i < this.size; ++i) {
            this.outputNodes.get(i).setPosition(xCof, yCof + (6 * unitSize * i) ,0);
        }
    }

    public void setSwitchDimension (int width, int height) {
        this.height = height;
        this.unitSize = this.height / ((6 * this.size) - 1); // unit used to construct internal nodes
        this.internalNodeSize = 5 * this.unitSize;
        this.width = Math.max(width, 4 * this.internalNodeSize);

        for (int i = 0; i < this.size; ++i) {
            this.inputNodes.get(i).setDefaultDrawingSizeInPixels(this.internalNodeSize);
            this.outputNodes.get(i).setDefaultDrawingSizeInPixels(this.internalNodeSize);
        }
        this.updateInternalNodesPositions();
    }

    // TODO: improve this method, separate into other methods
    @Override
    public void draw (Graphics g, PositionTransformation pt, boolean highlight) {
        if (!Global.isGuiMode) {
            return;
        }
        int widthInPixels = (int) (this.width * pt.getZoomFactor());
        int heightInPixels = (int) (this.height * pt.getZoomFactor());
        pt.translateToGUIPosition(this.getPosition());
        int x = pt.guiX - (widthInPixels >> 1);
        int y = pt.guiY - (heightInPixels >> 1);
        Color backupColor = g.getColor();
        Color color = getColor();
        if(highlight) {
            // a highlighted node is surrounded by a red square
            g.setColor(color == Color.RED ? Color.BLACK : Color.RED);
            g.drawRect(x-2, y-2, widthInPixels+4, heightInPixels+4);
        }
        g.setColor(Color.BLACK);
        g.drawRect(x, y, widthInPixels, heightInPixels);
        g.setColor(backupColor);

        int internalNodeSize = (int)(this.internalNodeSize * pt.getZoomFactor());
        // Set the font
        String text_in = "In";
//        String text_in = this.index + "";
        int fontSize = (int) (internalNodeSize * 0.5);
        Font font = new Font(null, 0, (int) (fontSize));
        g.setFont(font);
        // Determine the height and width of the text to be written
        FontMetrics fm = g.getFontMetrics(font);
        int h = (int) Math.ceil(fm.getHeight());
        int w = (int) Math.ceil(fm.stringWidth(text_in));
        g.setColor(Color.BLACK);
        g.drawRect(x, y - internalNodeSize, internalNodeSize, internalNodeSize);
        g.setColor(Color.BLACK);
        g.drawString(text_in, x + internalNodeSize/2 - w/2, y - internalNodeSize/2 + h/2 - 2); // print the text onto the circle
        g.setColor(backupColor); // restore color

        String text_out = "Out";
        g.setFont(font);
        // Determine the height and width of the text to be written
        fm = g.getFontMetrics(font);
        h = (int) Math.ceil(fm.getHeight());
        w = (int) Math.ceil(fm.stringWidth(text_out));
        g.setColor(Color.BLACK);
        g.drawRect(x + widthInPixels - internalNodeSize, y - internalNodeSize, internalNodeSize, internalNodeSize);
        g.setColor(Color.BLACK);
        g.drawString(text_out, x - internalNodeSize/2 - w/2 + widthInPixels, y - internalNodeSize/2 + h/2 - 2); // print the text onto the circle
        g.setColor(backupColor); // restore color
    }

    @Override
    protected void nodePositionUpdated () {
        this.updateInternalNodesPositions();
    }
}
