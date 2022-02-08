package projects.cbOptNet.nodes.nodeImplementations;

import java.util.ArrayList;

import projects.opticalNet.nodes.messages.HasMessage;
import projects.opticalNet.nodes.messages.NewMessage;
import projects.opticalNet.nodes.messages.OpticalNetMessage;
import projects.opticalNet.nodes.messages.RoutingInfoMessage;
import projects.opticalNet.nodes.models.Direction;
import projects.opticalNet.nodes.models.InfraNode;
import projects.opticalNet.nodes.models.Rotation;
import projects.opticalNet.nodes.nodeImplementations.NetworkController;
import projects.opticalNet.nodes.nodeImplementations.NetworkNode;

import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class CBNetController extends NetworkController {
    private boolean seq = true;
    private double epsilon = -1.5;

    public CBNetController (int numNodes, int switchSize, ArrayList<NetworkNode> netNodes) {
        super(numNodes, switchSize, netNodes);
    }

    public CBNetController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, ArrayList<Integer> edgeList
    ) {
        super(numNodes, switchSize, netNodes, edgeList);
    }

    @Override
    public void controllerStep () {
        super.controllerStep();
    }

    /* Rotations */

    @Override
    protected boolean zigZigBottomUp (InfraNode x) {
        InfraNode y = x.getParent();
        InfraNode z = y.getParent();

        boolean leftZigZig = (y == z.getLeftChild());
        InfraNode c = (leftZigZig ? y.getRightChild() : y.getLeftChild());

        double deltaRank = this.zigDiffRank(y, z);

        if (deltaRank < this.epsilon && super.zigZigBottomUp(x)) {
            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

            long zNewWeight = zOldWeight - yOldWeight + cWeight;
            long yNewWeight = yOldWeight - cWeight + zNewWeight;

            z.setWeight(zNewWeight);
            y.setWeight(yNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZagBottomUp (InfraNode x) {
        InfraNode y = x.getParent();
        InfraNode z = y.getParent();

        boolean leftZigZag = (y == z.getLeftChild());
        InfraNode b = (leftZigZag) ? x.getLeftChild() : x.getRightChild();
        InfraNode c = (leftZigZag) ? x.getRightChild() : x.getLeftChild();

        double deltaRank = this.zigZagDiffRank(x, y, z);

        if (deltaRank < this.epsilon && super.zigZagBottomUp(x)) {
            long xOldWeight = x.getWeight();
            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long bWeight = (b.getId() != -1) ? b.getWeight() : 0;
            long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

            long yNewWeight = yOldWeight - xOldWeight + bWeight;
            long zNewWeight = zOldWeight - yOldWeight + cWeight;
            long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

            y.setWeight(yNewWeight);
            z.setWeight(zNewWeight);
            x.setWeight(xNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZigLeftTopDown (InfraNode z) {
        InfraNode y = z.getLeftChild();
        InfraNode c = y.getRightChild();

        double deltaRank = this.zigDiffRank(y, z);

        if (deltaRank < this.epsilon && super.zigZigLeftTopDown(z)) {
            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

            long zNewWeight = zOldWeight - yOldWeight + cWeight;
            long yNewWeight = yOldWeight - cWeight + zNewWeight;

            z.setWeight(zNewWeight);
            y.setWeight(yNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZigRightTopDown (InfraNode z) {
        InfraNode y = z.getRightChild();
        InfraNode c = y.getLeftChild();

        double deltaRank = this.zigDiffRank(y, z);

        if (deltaRank < this.epsilon && super.zigZigRightTopDown(z)) {

            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

            long zNewWeight = zOldWeight - yOldWeight + cWeight;
            long yNewWeight = yOldWeight - cWeight + zNewWeight;

            z.setWeight(zNewWeight);
            y.setWeight(yNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZagLeftTopDown (InfraNode z) {
        InfraNode y = z.getLeftChild();
        InfraNode x = y.getRightChild();
        InfraNode b = x.getLeftChild();
        InfraNode c = x.getRightChild();

        double deltaRank = this.zigZagDiffRank(x, y, z);

        if (deltaRank < this.epsilon && super.zigZagLeftTopDown(z)) {
            long xOldWeight = x.getWeight();
            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long bWeight = (b.getId() != -1) ? b.getWeight() : 0;
            long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

            long yNewWeight = yOldWeight - xOldWeight + bWeight;
            long zNewWeight = zOldWeight - yOldWeight + cWeight;
            long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

            y.setWeight(yNewWeight);
            z.setWeight(zNewWeight);
            x.setWeight(xNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZagRightTopDown (InfraNode z) {
        InfraNode y = z.getRightChild();
        InfraNode x = y.getLeftChild();
        InfraNode b = x.getRightChild();
        InfraNode c = x.getLeftChild();

        double deltaRank = this.zigZagDiffRank(x, y, z);

        if (deltaRank < this.epsilon && super.zigZagRightTopDown(z)) {
            long xOldWeight = x.getWeight();
            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long bWeight = (b.getId() != -1) ? b.getWeight() : 0;
            long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

            long yNewWeight = yOldWeight - xOldWeight + bWeight;
            long zNewWeight = zOldWeight - yOldWeight + cWeight;
            long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

            y.setWeight(yNewWeight);
            z.setWeight(zNewWeight);
            x.setWeight(xNewWeight);

            return true;
        }

        return false;
    }
    /* End of Rotations */

    /* Private Getters */
    private double log2 (long value) {
        return (value == 0 ? 0 : Math.log(value) / Math.log(2));
    }

    private double zigDiffRank (InfraNode x, InfraNode y) {
        /*
                     y                   x
                   /   \               /   \
                  x     c     -->     a     y
                 / \                       / \
                a   b                     b    c
        */
        boolean leftZig = (x == y.getLeftChild());

        InfraNode b = (leftZig) ? x.getRightChild() : x.getLeftChild();

        long xOldWeight = x.getWeight();
        long yOldWeight = y.getWeight();

        long bWeight = (b.getId() != -1) ? b.getWeight() : 0;

        long yNewWeight = yOldWeight - xOldWeight + bWeight;
        long xNewWeight = xOldWeight - bWeight + yNewWeight;

        double xOldRank = log2(xOldWeight);
        double yOldRank = log2(yOldWeight);
        double xNewRank = log2(xNewWeight);
        double yNewRank = log2(yNewWeight);

        double deltaRank = yNewRank + xNewRank - yOldRank - xOldRank;

        return deltaRank;
    }

    private double zigZagDiffRank (InfraNode x, InfraNode y, InfraNode z) {
        /*
             z					   *x
            / \                   /   \
           y   d                 y     z
              / \		 -->    / \   / \
             a  *x             a   b c   d
                / \
               b   c
        */
        boolean lefZigZag = (y == z.getLeftChild());

        InfraNode b = lefZigZag ? x.getLeftChild() : x.getRightChild();
        InfraNode c = lefZigZag ? x.getRightChild() : x.getLeftChild();

        long xOldWeight = x.getWeight();
        long yOldWeight = y.getWeight();
        long zOldWeight = z.getWeight();

        long bWeight = b.getWeight();
        long cWeight = c.getWeight();

        long yNewWeight = yOldWeight - xOldWeight + bWeight;
        long zNewWeight = zOldWeight - yOldWeight + cWeight;
        long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

        double xOldRank = log2(xOldWeight);
        double yOldRank = log2(yOldWeight);
        double zOldRank = log2(zOldWeight);
        double xNewRank = log2(xNewWeight);
        double yNewRank = log2(yNewWeight);
        double zNewRank = log2(zNewWeight);

        double deltaRank = xNewRank + yNewRank + zNewRank - xOldRank - yOldRank - zOldRank;

        return deltaRank;
    }

    @Override
    public Rotation getRotationToPerform (InfraNode x, InfraNode dstNode) {
        Direction direction = x.getRoutingDirection(dstNode);

        if (direction == Direction.PARENTROUT) {
            return Rotation.NULL;

        } else if (direction == Direction.LEFTROUT) {
            return Rotation.NULL;

        } else if (direction == Direction.RIGHTROUT) {
            return Rotation.NULL;

        } else if (
            direction == Direction.PARENT &&
            !(this.isValidNode(x.getParent()) && this.isValidNode(x.getParent().getParent()))
        ) {
            return Rotation.NULL;

        }

        /*bottom-up - BEGIN*/
        if (direction == Direction.PARENT) {
            InfraNode y = x.getParent();
            InfraNode z = y.getParent();
            if (
                this.isValidNode(y.getLeftChild()) && x == y.getLeftChild() &&
                this.isValidNode(z.getLeftChild()) && y == z.getLeftChild()
            ) {
                return Rotation.ZIGZIGLEFT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getRightChild()) && x == y.getRightChild() &&
                this.isValidNode(z.getRightChild()) && y == z.getRightChild()
            ) {
                return Rotation.ZIGZIGRIGHT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getRightChild()) && x == y.getRightChild() &&
                this.isValidNode(z.getLeftChild()) && y == z.getLeftChild()
            ) {
                return Rotation.ZIGZAGLEFT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getLeftChild()) && x == y.getLeftChild() &&
                this.isValidNode(z.getRightChild()) && y == z.getRightChild()
            ) {
                return Rotation.ZIGZAGRIGHT_BOTTOMUP;

            } else {
                Tools.fatalError("Network topology for BottomUp not expected");

            }

        /* Top-Down - LEFT - BEGIN */
        } else if (direction == Direction.LEFT) {
            InfraNode y = x.getRoutingNode(dstNode);
            InfraNode z = y.getRoutingNode(dstNode);

            if (x.getLeftChild() == y && y.getLeftChild() == z) {
                return Rotation.ZIGZIGLEFT_TOPDOWN;

            } else if (x.getLeftChild() == y && y.getRightChild() == z) {
                return Rotation.ZIGZAGLEFT_TOPDOWN;

            } else {
                Tools.fatalError("Network topology for Left TopDown not expected");

            }

        /* Top-Down - RIGHT - BEGIN */
        } else if (direction == Direction.RIGHT) {
            InfraNode y = x.getRoutingNode(dstNode);
            InfraNode z = y.getRoutingNode(dstNode);

            if (x.getRightChild() == y && y.getRightChild() == z) {
                return Rotation.ZIGZIGRIGHT_TOPDOWN;

            } else if (x.getRightChild() == y && y.getLeftChild() == z) {
                return Rotation.ZIGZAGRIGHT_TOPDOWN;

            } else {
                Tools.fatalError("Network topology for Right TopDown not expected");

            }
        }

        Tools.fatalError("Unexpected rotation");

        return null;
    }

    private void incrementPathWeight (int from, int to) {
        InfraNode fromNode = this.getInfraNode(from);
        InfraNode toNode = this.getInfraNode(to);

        fromNode.incrementPathWeight(toNode, false);
    }

    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof OpticalNetMessage) {
                OpticalNetMessage optmsg = (OpticalNetMessage) msg;
                this.logIncrementCompletedRequests();
                this.logMessageRouting(optmsg.getRouting());

                this.incrementPathWeight(optmsg.getSrc(), optmsg.getDst());

                this.cmpMsgs++;
                this.seq = true;

            } else if (msg instanceof NewMessage) {
                this.rcvMsgs++;

            } else if (msg instanceof HasMessage) {
                HasMessage hasmsg = (HasMessage) msg;
                this.nodesWithMsg.add(hasmsg);

            } else if (msg instanceof RoutingInfoMessage) {
            	RoutingInfoMessage routmsg = (RoutingInfoMessage) msg;
                this.routingNodes.add(routmsg);

            }

        }
    }

    public boolean getSeq () {
        return this.seq;
    }

    public void setSeq () {
        this.seq = false;
    }

}
