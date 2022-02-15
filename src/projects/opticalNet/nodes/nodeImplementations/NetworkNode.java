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

/**
 * Node implementation responsible to rout the messages along the network.
 */
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

    /**
     * Setter to update NetworkNode parent as the NetworkNode connected to this InputNode
     * @param node  InputNode
     */
    public void setParent (InputNode node) {
        this.parent = node;
    }

    /**
     * Setter to update link to the NetworkController, called after initialization so there aren't
     * any troubles with the nodes ID's regarding Sinalgo.
     * @param controller    NetworkController node
     */
    public void setController (NetworkController controller) {
        this.controller = controller;
    }

    /**
     * Getter for the parent related InputNode.
     * @return  parent related InputNode
     */
    public InputNode getParent () {
        return this.parent;
    }

    /**
     * Setter that resets parent link, if it is removed from the network
     */
    public void removeParent () {
        this.parent = null;
    }

    /**
     * Setter to update the child related InputNode according to the NetworkNode
     * connected to the linked OutputNode.
     * @param node  InputNode.
     */
    public void setChild (InputNode node) {
        if (this.ID < node.getOutputNode().getIndex()) {
            this.setRightChild(node);

        } else if (this.ID > node.getOutputNode().getIndex()) {
            this.setLeftChild(node);

        } else {
            Tools.fatalError("Not clear which child to remove from node " + this.ID);
        }
    }

    /**
     * Setter to update de left child InputNode
     * @param node  the new inputNode
     */
    public void setLeftChild (InputNode node) {
        this.leftChild = node;
    }

    /**
     * Setter to update de right child InputNode
     * @param node  the new inputNode
     */
    public void setRightChild (InputNode node) {
        this.rightChild = node;
    }

    /**
     * Setter that resets left child link, if it is removed from the network
     */
    public void removeLeftChild () {
        this.leftChild = null;
    }

    /**
     * Getter for the ID of the connected left child NetworkNode.
     * Returns -1 if node does not have a left child
     * @return  the connected leftchild id
     */
    public int getLeftChildId() {
        if (this.leftChild == null)
            return -1;

        return this.leftChild.getOutputNode().getIndex();
    }

    /**
     * Getter for the ID of the connected right child NetworkNode.
     * Returns -1 if node does not have a right child
     * @return  the connected rightchild id
     */
    public int getRightChildId() {
        if (this.rightChild == null)
            return -1;

        return this.rightChild.getOutputNode().getIndex();
    }

    /**
     * Getter for the ID of the connected parent NetworkNode.
     * Returns -1 if node does not have a parent
     * @return  the connected parent id
     */
    public int getParentId() {
        if (this.parent == null)
            return -1;

        return this.parent.getOutputNode().getIndex();
    }

    /**
     * Getter for the InputNode linked to the OutputNode connected to this node
     * left child
     * @return  the leftchild input node
     */
    public InputNode getLeftChild () {
        return this.leftChild;
    }

    /**
     * Getter for the InputNode linked to the OutputNode connected to this node
     * right child
     * @return  the rightchild input node
     */
    public InputNode getRightChild () {
        return this.rightChild;
    }

    /**
     * Setter that resets right child link, if it is removed from the network
     */
    public void removeRightChild () {
        this.rightChild = null;
    }

    /**
     * Getter for this NetworkNode id
     * @return  this node id in the network
     */
    public int getId () {
        return this.ID;
    }

    @Override
    public void init () {
        super.init();
    }

    /**
     * Adds a new message to this node buffer and inform it's initialization to the
     * network controller
     * @param to    the destination node id
     */
    public void newMessage (int to) {
        double priority = Global.currentTime + this.rand.nextDouble();
        OpticalNetMessage optmsg = new OpticalNetMessage(this.ID, to, priority);

        this.buffer.add(optmsg);
        this.sendDirect(new NewMessage(), this.controller);
    }

    /**
     * NetworkNode step, where it sends a HasMessage to the NetworkController that it
     * has a OpticalNetMessage that is ready to perform a new operation.
     * @param optmsg    the OpticalNetMessage
     */
    private void informController (OpticalNetMessage optmsg) {
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

    /**
     * This method sends the message to the next node in the routMsg path through the InputNode
     * and OutputNode, up to the corresponding NetworkNode
     * @param routMsg   the RoutingInfoMessage
     */
    public void sendMsg (RoutingInfoMessage routMsg) {
        routMsg.getRoutedMsg().incrementRouting();
        this.controller.logIncrementRouting(
            this.ID, routMsg.getRoutNodeId()
        );

        this.sendToInputNode(routMsg);
    }

    /**
     * This method sends the RoutingInfoMessage to the InputNode informed by the sendMsg method
     * @param routMsg   the RoutingInfoMessage
     */
    protected void sendToInputNode (RoutingInfoMessage routMsg) {
        this.getRoutingNode(routMsg).sendToOutputNode(routMsg, this.controller);
    }

    /**
     * Getter for the next node in the RoutingInfoMessage path
     * @param routMsg   the RoutingInfoMessage
     * @return          the InputNode corresponding to the next node in the path
     */
    private InputNode getRoutingNode (RoutingInfoMessage routMsg) {
        if (this.getLeftChildId() == routMsg.getRoutNodeId()) {
            return this.getLeftChild();

        } else if (this.getRightChildId() == routMsg.getRoutNodeId()) {
            return this.getRightChild();

        } else if (this.getParentId() == routMsg.getRoutNodeId()) {
            return this.getParent();

        } else {
            System.out.println("Unclear rout node");
            Tools.fatalError("Unclear rout node");

        }

        return null;
    }

    /**
     * This method informs the controller whether this NetworkNode has a message
     * that needs to be routed or if it has a message that can perform a new operation.
     * If it has neither it doesn't do anything.
     */
    @Override
    public void nodeInformStep () {
        if (this.routMsg != null) {
            this.sendDirect(this.routMsg, this.controller);

        } else if (!buffer.isEmpty()) {
            this.currMsg = this.buffer.poll();
            this.informController(this.currMsg);

        }
    }

    /**
     * This method sets up the necessary information to rout a RoutingInfoMessage if the node was
     * allowed by the NetworkController.
     * @return  True if the message was configured and false if not
     */
    private boolean configureRoutingMessage () {
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

    /**
     * Sends the RoutingInfoMessage to the next NetworkNode in the path if it was allowed by the
     * NetworkController and correctly configured by the configureRoutingMessage, decrementing the
     * number of necessary routing times assigned to the RoutInfoMessage.
     */
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

    /**
     * This method handles the message a NetworkNode receives. If it is a OpticalNetMessage,
     * simply add it to the buffer [outdated, currently only OpticalNetMessage enclosed
     * RoutingInfoMessage's are routed among NetworkNodes], if it is a RoutingInfoMessage,
     * and it still needs to be routed it is signed as a allowMsg, flagged to inform the
     * Controller if it already has been routed, the enclosed OpticalNetMessage is added to
     * the message buffer.
     */
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

    /**
     * Control debug method to print the NetworkNode information
     */
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
