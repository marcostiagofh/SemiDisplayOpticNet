package projects.opticalNet.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import projects.opticalNet.nodes.messages.NetworkMessage;
import projects.opticalNet.nodes.infrastructureImplementations.InputNode;
import projects.opticalNet.nodes.infrastructureImplementations.SynchronizerLayer;

import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.gui.transformation.PositionTransformation;

public class NetworkNode extends SynchronizerLayer {

    private int weights = 0;
    private ArrayList<InputNode> interfaces = new ArrayList<>();
    private Queue<NetworkMessage> buffer = new LinkedList<>();

    private InputNode parent = null;
    private InputNode leftChild = null;
    private InputNode rightChild = null;

    private NetworkController controller = null;

    private int minIdInSubtree = 0;
    private int maxIdInSubtree = 0;

    public NetworkNode () {
    	this.minIdInSubtree = this.ID;
    	this.maxIdInSubtree = this.ID;
    }

    public void connectToInputNode (InputNode node) {
        this.interfaces.add(node);
        this.addConnectionTo(node);
    }

    public void setParent (InputNode node) {
        this.parent = node;
    }

    public void setController (NetworkController controller) {
        this.controller = controller;
    }

    public InputNode getParent () {
        return this.parent;
    }

    public void setChild (InputNode node, int subtreeId) {
        if (this.ID < node.getIndex()) {
            this.setRightChild(node, subtreeId);

        } else {
            this.setLeftChild(node, subtreeId);

        }
    }

    public void setLeftChild (InputNode node, int minId) {
        this.leftChild = node;
        this.setMinIdInSubtree(minId);
    }

    public InputNode getLeftChild () {
        return this.leftChild;
    }

    public void setRightChild (InputNode node, int maxId) {
        this.rightChild = node;
        this.setMaxIdInSubtree(maxId);
    }

    public void setMinIdInSubtree (int value) {
    	if (value == -1) {
    		this.minIdInSubtree = this.ID;

    	} else {
    		this.minIdInSubtree = value;

    	}
    }

    public int getMinIdInSubtree () {
        return this.minIdInSubtree;
    }

    public void setMaxIdInSubtree (int value) {
    	if (value == -1) {
    		this.maxIdInSubtree = this.ID;

    	} else {
    		this.maxIdInSubtree = value;

    	}
    }

    public int getMaxIdInSubtree () {
        return this.maxIdInSubtree;
    }

    public int getWeight () {
        return this.weights;
    }

    public int getId () {
        return this.ID;
    }

    @Override
    public void init () {
        super.init();
    }

    public void newMessage (int to) {
    	NetworkMessage netmsg = new NetworkMessage(this.ID, to);
    	this.buffer.add(netmsg);
    }

    public void sendMsg (NetworkMessage msg) {
    	NetworkMessage netmsg = new NetworkMessage(this.ID, msg.getDst());
    	if (msg.getDst() == this.ID) {
            System.out.println("Message received from node " + msg.getSrc());
            return;

        }

        if (this.minIdInSubtree <= msg.getDst() && msg.getDst() < this.ID) {
            this.send(netmsg, this.leftChild);

        } else if (this.ID < msg.getDst() && msg.getDst() <= this.maxIdInSubtree) {
            this.send(netmsg, this.rightChild);

        } else {
            this.send(netmsg, this.parent);

        }
    }

    @Override
    public void nodeStep () {
    	if (buffer.isEmpty())
            return;

    	NetworkMessage netmsg = this.buffer.poll();
    	this.sendMsg(netmsg);
    }

    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();
            if (!(msg instanceof NetworkMessage)) {
                continue;

            }

            NetworkMessage optmsg = (NetworkMessage) msg;
            if (optmsg.getDst() == this.ID) {
            	this.sendDirect(optmsg, this.controller);
                continue;

            }

            if (this.minIdInSubtree <= optmsg.getDst() && optmsg.getDst() < this.ID) {
                this.send(optmsg, this.leftChild);

            } else if (this.ID < optmsg.getDst() && optmsg.getDst() <= this.maxIdInSubtree) {
                this.send(optmsg, this.rightChild);

            } else {
                this.send(optmsg, this.parent);

            }
        }
    }

    //-----------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------
    // Drawing method of the Network Node
    //-----------------------------------------------------------------------------------
    //-----------------------------------------------------------------------------------

    @Override
    public void draw (Graphics g, PositionTransformation pt, boolean highlight) {
        String text = "" + ID;
        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 12, Color.YELLOW);
    }
}
