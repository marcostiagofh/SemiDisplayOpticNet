package projects.opticalNet.nodes.nodeImplementations;

import java.util.Random;

import java.awt.Graphics;
import java.util.PriorityQueue;

import projects.opticalNet.nodes.messages.NewMessage;
import projects.opticalNet.nodes.messages.RoutingInfoMessage;
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
    private Random rand = Tools.getRandomNumberGenerator();

    public NetworkNode () { }

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

    public void setChild (InputNode node) {
        if (this.ID < node.getOutputNode().getIndex()) {
            this.setRightChild(node);

        } else if (this.ID > node.getOutputNode().getIndex()) {
            this.setLeftChild(node);

        } else {
            Tools.fatalError("Not clear which child to remove from node " + this.ID);
        }
    }

    public void setLeftChild (InputNode node) {
        this.leftChild = node;
    }

    public void setRightChild (InputNode node) {
        this.rightChild = node;
    }

    public void removeLeftChild () {
        this.leftChild = null;
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

    public void removeRightChild () {
        this.rightChild = null;
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
        this.sendDirect(new NewMessage(), this.controller);
    }

    public void informController (OpticalNetMessage optmsg) {
        if (optmsg.getDst() == this.ID) {
            System.out.println(
                "OPT-Message received from node " + optmsg.getSrc() + " to node " + this.ID
            );
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

    public void sendMsg (RoutingInfoMessage routMsg) {
        routMsg.getRoutedMsg().incrementRouting();
        this.controller.logIncrementRouting(
            this.ID, routMsg.getRoutNodeId()
        );

        this.sendToInputNode(routMsg);
    }

    protected void sendToInputNode (RoutingInfoMessage routMsg) {
        this.getRoutingNode(routMsg.getRoutNodeId()).sendToOutputNode(routMsg, this.controller);
    }

    private InputNode getRoutingNode (int routNodeId) {
        if (this.getLeftChildId() == routNodeId) {
            return this.getLeftChild();

        } else if (this.getRightChildId() == routNodeId) {
            return this.getRightChild();

        } else if (this.getParentId() == routNodeId) {
            return this.getParent();

        } else {
            System.out.println("Unclear rout node");
            Tools.fatalError("Unclear rout node");

        }

        return null;
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
                System.out.println("ROUT-Message received from node " + this.routMsg.getSrc() + " to node " + this.ID);
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
