package projects.opticalNet.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.Queue;

import projects.opticalNet.nodes.messages.NewMessage;
import projects.opticalNet.nodes.messages.WeightMessage;
import projects.opticalNet.nodes.messages.OpticalNetMessage;
import projects.opticalNet.nodes.infrastructureImplementations.InputNode;
import projects.opticalNet.nodes.infrastructureImplementations.SynchronizerLayer;

import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.gui.transformation.PositionTransformation;

public class NetworkNode extends SynchronizerLayer {
	
    private Queue<OpticalNetMessage> buffer = new LinkedList<>();

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

    public void removeParent () {
    	this.parent = null;
    }

    public void setChild (InputNode node, int subtreeId) {
        if (this.ID < node.getOutputNode().getIndex()) {
            this.setRightChild(node, subtreeId);

        } else if (this.ID > node.getOutputNode().getIndex()) {
            this.setLeftChild(node, subtreeId);

        } else {
        	Tools.fatalError("Not clear which child to remove from node " + this.ID);
        }
    }

    public void setLeftChild (InputNode node, int minId) {
        this.leftChild = node;
        this.setMinIdInSubtree(minId);
    }

    public void removeLeftChild () {
    	this.leftChild = null;
        this.setMinIdInSubtree(this.ID);
    }

    public int getLeftChildId() {
    	if (this.leftChild == null)
    		return -1;

    	return this.leftChild.getOutputNode().getIndex();
    }

    public int getRightChildId() {
    	if (this.rightChild == null)
    		return -1;

    	return this.rightChild.getOutputNode().getIndex();
    }

    public int getParentId() {
    	if (this.parent == null)
    		return -1;

    	return this.parent.getOutputNode().getIndex();
    }

    public InputNode getLeftChild () {
        return this.leftChild;
    }

    public InputNode getRightChild () {
        return this.rightChild;
    }

    public void setRightChild (InputNode node, int maxId) {
        this.rightChild = node;
        this.setMaxIdInSubtree(maxId);
    }

    public void removeRightChild () {
    	this.rightChild = null;
        this.setMaxIdInSubtree(this.ID);
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

    public int getId () {
        return this.ID;
    }

    @Override
    public void init () {
        super.init();
    }

    public void newMessage (int to) {
    	OpticalNetMessage optmsg = new OpticalNetMessage(this.ID, to);

    	this.buffer.add(optmsg);
        this.sendDirect(new NewMessage(optmsg), this.controller);
    }

    public void sendMsg (OpticalNetMessage optmsg) {
        this.sendDirect(new WeightMessage(this.ID), this.controller);

    	if (optmsg.getDst() == this.ID) {
            System.out.println("Message received from node " + optmsg.getSrc());
            this.sendDirect(optmsg, this.controller);

            return;

    	} 

        optmsg.incrementRouting();  
        if (this.minIdInSubtree <= optmsg.getDst() && optmsg.getDst() < this.ID) {
            this.send(optmsg, this.leftChild);

        } else if (this.ID < optmsg.getDst() && optmsg.getDst() <= this.maxIdInSubtree) {
            this.send(optmsg, this.rightChild);

        } else {
            this.send(optmsg, this.parent);

        }
    }

    @Override
    public void nodeStep () {
    	while (!buffer.isEmpty()) {
	        OpticalNetMessage optmsg = this.buffer.poll();
	        this.sendMsg(optmsg);
	        
    	}
    }

    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if ((msg instanceof OpticalNetMessage)) { 
            	OpticalNetMessage optmsg = (OpticalNetMessage) msg;
            	this.buffer.add(optmsg);

            }
        }
    }

    public void debugNode () {
        System.out.println(
        	"NETID: " + this.getId()
        	+ " lftID: " + this.getLeftChildId()
        	+ " rgtID: " + this.getRightChildId()
        	+ " parentId: " + this.getParentId()
            + " leftSUB: " + this.minIdInSubtree + " rightSUB: " + this.maxIdInSubtree
        );
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
