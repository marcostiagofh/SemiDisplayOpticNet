package projects.opticalNet.nodes.nodeImplementations;

import java.awt.Color;
import java.util.Random;
import java.awt.Graphics;
import java.util.PriorityQueue;

import projects.opticalNet.nodes.messages.NewMessage;
import projects.opticalNet.nodes.messages.AllowRoutingMessage;
import projects.opticalNet.nodes.messages.HasMessage;
import projects.opticalNet.nodes.messages.OpticalNetMessage;
import projects.opticalNet.nodes.infrastructureImplementations.Direction;
import projects.opticalNet.nodes.infrastructureImplementations.InputNode;
import projects.opticalNet.nodes.infrastructureImplementations.SynchronizerLayer;

import sinalgo.tools.Tools;
import sinalgo.runtime.Global;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.gui.transformation.PositionTransformation;

public class NetworkNode extends SynchronizerLayer {

    private PriorityQueue<OpticalNetMessage> buffer = new PriorityQueue<OpticalNetMessage>();
    private OpticalNetMessage currMsg = null;

    private InputNode parent = null;
    private InputNode leftChild = null;
    private InputNode rightChild = null;

    private NetworkController controller = null;

    private int minIdInSubtree = 0;
    private int maxIdInSubtree = 0;

    private boolean allowRouting = false;
    private Random rand = Tools.getRandomNumberGenerator();

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
        double priority = Global.currentTime + this.rand.nextDouble();
        OpticalNetMessage optmsg = new OpticalNetMessage(this.ID, to, priority);

        this.buffer.add(optmsg);
        this.sendDirect(new NewMessage(optmsg), this.controller);
    }

    public void informController (OpticalNetMessage optmsg) {
        if (optmsg.getDst() == this.ID) {
            System.out.println("Message received from node " + optmsg.getSrc());
            this.sendDirect(optmsg, this.controller);
            this.currMsg = null;

            return;

        }

        if (this.minIdInSubtree <= optmsg.getDst() && optmsg.getDst() < this.ID) {
            if (this.getLeftChildId() == optmsg.getDst()) {
                this.sendDirect(new HasMessage(this.ID, optmsg.getPriority(), Direction.LEFTROUT), this.controller);

            } else {
                this.sendDirect(new HasMessage(this.ID, optmsg.getPriority(), Direction.LEFT), this.controller);

            }

        } else if (this.ID < optmsg.getDst() && optmsg.getDst() <= this.maxIdInSubtree) {
            if (this.getRightChildId() == optmsg.getDst()) {
                this.sendDirect(new HasMessage(this.ID, optmsg.getPriority(),Direction.RIGHTROUT), this.controller);

            } else {
                this.sendDirect(new HasMessage(this.ID, optmsg.getPriority(), Direction.RIGHT), this.controller);

            }

        } else {
            if (this.getParentId() == optmsg.getDst()) {
                this.sendDirect(new HasMessage(this.ID, optmsg.getPriority(), Direction.PARENTROUT), this.controller);

            } else {
                this.sendDirect(new HasMessage(this.ID, optmsg.getPriority(), Direction.PARENT), this.controller);

            }

        }
    }

    public void sendMsg (OpticalNetMessage optmsg) {
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
    public void nodeInformStep () {
        if (!buffer.isEmpty()) {
            this.currMsg = this.buffer.poll();
            this.informController(this.currMsg);

        }
    }

    @Override
    public void nodeRoutingStep () {
        if (this.allowRouting && this.currMsg == null) {
            Tools.fatalError("Node allowed routing non-existing message");

        }

        if (this.allowRouting && this.currMsg != null) {
            this.sendMsg(this.currMsg);

        } else if (this.currMsg != null) {
            this.buffer.add(this.currMsg);

        }

        this.currMsg = null;
        this.allowRouting = false;
    }

    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if ((msg instanceof OpticalNetMessage)) {
                OpticalNetMessage optmsg = (OpticalNetMessage) msg;
                this.buffer.add(optmsg);

            } else if ((msg instanceof AllowRoutingMessage)) {
                this.allowRouting = true;

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
