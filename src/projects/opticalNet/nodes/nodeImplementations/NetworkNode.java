package projects.opticalNet.nodes.nodeImplementations;

import java.util.Random;
import java.awt.Graphics;
import java.util.PriorityQueue;

import projects.opticalNet.nodes.messages.NewMessage;
import projects.opticalNet.nodes.messages.RoutingInfoMessage;
import projects.opticalNet.nodes.models.Direction;
import projects.opticalNet.nodes.messages.HasMessage;
import projects.opticalNet.nodes.messages.OpticalNetMessage;
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
    private RoutingInfoMessage routMsg = null;

    private InputNode parent = null;
    private InputNode leftChild = null;
    private InputNode rightChild = null;

    private NetworkController controller = null;

    private int minIdInSubtree = 0;
    private int maxIdInSubtree = 0;

    private Random rand = Tools.getRandomNumberGenerator();

    public NetworkNode () {
        this.minIdInSubtree = this.ID;
        this.maxIdInSubtree = this.ID;
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

    public int getRoutingNodeId (OpticalNetMessage optmsg) {
        Direction direction = this.getRoutingDirection(optmsg);
        if (direction == Direction.LEFT || direction == Direction.LEFTROUT) {
            return this.getLeftChildId();

        } else if (direction == Direction.RIGHT || direction == Direction.RIGHTROUT) {
            return this.getRightChildId();

        } else {
            return this.getParentId();

        }

    }

    public InputNode getRoutingNode (OpticalNetMessage optmsg) {
        Direction direction = this.getRoutingDirection(optmsg);
        if (direction == Direction.LEFT || direction == Direction.LEFTROUT) {
            return this.leftChild;

        } else if (direction == Direction.RIGHT || direction == Direction.RIGHTROUT) {
            return this.rightChild;

        } else {
            return this.parent;

        }
    }

    public Direction getRoutingDirection (OpticalNetMessage optmsg) {
        if (this.getLeftChildId() == optmsg.getDst()) {
            return Direction.LEFTROUT;

        } else if (this.getRightChildId() == optmsg.getDst()) {
            return Direction.RIGHTROUT;

        } else if (this.getParentId() == optmsg.getDst()) {
            return Direction.PARENTROUT;

        } else if (this.minIdInSubtree <= optmsg.getDst() && optmsg.getDst() < this.ID) {
            return Direction.LEFT;

        } else if (this.ID < optmsg.getDst() && optmsg.getDst() <= this.maxIdInSubtree) {
            return Direction.RIGHT;

        } else {
            return Direction.PARENT;

        }
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
            System.out.println("OPT-Message received from node " + optmsg.getSrc());
            this.sendDirect(optmsg, this.controller);
            this.currMsg = null;

            return;

        }

        this.sendDirect(
            new HasMessage(
            		this.ID, optmsg.getPriority(), optmsg.getDst()
            ), this.controller
        );
    }

    public void sendMsg (RoutingInfoMessage routmsg) {
        routmsg.getRoutedMsg().incrementRouting();
        this.controller.logIncrementRouting(
            this.ID, this.getRoutingNodeId(routmsg.getRoutedMsg())
        );

        this.sendToInputNode(routmsg);
    }

    protected void sendToInputNode (RoutingInfoMessage routmsg) {
        this.getRoutingNode(routmsg.getRoutedMsg()).sendToOutputNode(routmsg, this.controller);
    }

    @Override
    public void nodeInformStep () {
        if (this.routMsg != null) {
            this.sendDirect(this.routMsg, this.controller);

        } else if (!buffer.isEmpty()) {
            this.currMsg = this.buffer.poll();
            this.informController(this.currMsg);

        }
    }

    public boolean configureRoutingMessage () {
        if (this.routMsg == null) {
            if (this.currMsg != null) {
                this.buffer.add(this.currMsg);
                this.currMsg = null;

            }

            return false;

        } else if (this.currMsg == null && this.routMsg.getRoutedMsg() == null) {
            Tools.fatalError("Trying to route non-existing message");

        } else if (this.currMsg != null && this.routMsg.getRoutedMsg() != null) {
            Tools.fatalError("Trying to route more than one message");

        } else if (this.currMsg != null) {
            this.routMsg.setRoutedMsg(this.currMsg);
            this.currMsg = null;

        }

        return true;
    }

    @Override
    public void nodeRoutingStep () {
        if (this.configureRoutingMessage()) {
            if (this.routMsg.getDst() == this.ID) {
                System.out.println("ROUT-Message received from node " + this.routMsg.getSrc());
                this.sendDirect(this.routMsg.getRoutedMsg(), this.controller);
                this.routMsg = null;

                return;

            }

            this.routMsg.decreaseRoutingTimes();
            this.sendMsg(this.routMsg);

            this.routMsg = null;

        }
    }

    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if ((msg instanceof OpticalNetMessage)) {
                OpticalNetMessage optmsg = (OpticalNetMessage) msg;
                this.buffer.add(optmsg);

            } else if ((msg instanceof RoutingInfoMessage)) {
                RoutingInfoMessage allowmsg = (RoutingInfoMessage) msg;
                if (allowmsg.getRoutingTimes() > 0) {
                    this.routMsg = allowmsg;
                    this.routMsg.setNodeId(this.ID);
                } else if (allowmsg.getRoutedMsg() == null) {
                    Tools.fatalError("Empty routed message");

                } else {
                    this.buffer.add(allowmsg.getRoutedMsg());

                }

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
    public void draw (Graphics g, PositionTransformation pt, boolean highlight) { }
}
