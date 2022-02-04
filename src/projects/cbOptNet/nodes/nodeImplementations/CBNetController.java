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
    protected boolean zigZigBottomUp (InfraNode x, Direction direction) {
        InfraNode y = x.getParent();
        InfraNode z = y.getParent();

        boolean leftZigZig = (y.getId() == z.getLeftChild().getId());
        InfraNode b = (leftZigZig ? y.getRightChild() : y.getLeftChild());

        if (super.zigZigBottomUp(x, direction)) {
            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long bWeight = (b.getId() != -1) ? b.getWeight() : 0;

            long zNewWeight = zOldWeight - yOldWeight + bWeight;
            long yNewWeight = yOldWeight - bWeight + zNewWeight;

            z.setWeight(zNewWeight);
            y.setWeight(yNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZagBottomUp (InfraNode x, Direction direction) {
        InfraNode y = x.getParent();
        InfraNode z = y.getParent();

        boolean leftZigZag = (y.getId() == z.getLeftChild().getId());
        InfraNode b = (leftZigZag) ? x.getLeftChild() : x.getRightChild();
        InfraNode c = (leftZigZag) ? x.getRightChild() : x.getLeftChild();

        if (super.zigZagBottomUp(x, direction)) {
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
    protected boolean zigZigLeftTopDown (InfraNode z, Direction direction) {
        InfraNode y = z.getLeftChild();
        InfraNode b = y.getRightChild();

        if (super.zigZigLeftTopDown(z, direction)) {

            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long bWeight = (b.getId() != -1) ? b.getWeight() : 0;

            long zNewWeight = zOldWeight - yOldWeight + bWeight;
            long yNewWeight = yOldWeight - bWeight + zNewWeight;

            z.setWeight(zNewWeight);
            y.setWeight(yNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZigRightTopDown (InfraNode z, Direction direction) {
        InfraNode y = z.getRightChild();
        InfraNode b = y.getLeftChild();

        if (super.zigZigRightTopDown(z, direction)) {

            long yOldWeight = y.getWeight();
            long zOldWeight = z.getWeight();

            long bWeight = (b.getId() != -1) ? b.getWeight() : 0;

            long zNewWeight = zOldWeight - yOldWeight + bWeight;
            long yNewWeight = yOldWeight - bWeight + zNewWeight;

            z.setWeight(zNewWeight);
            y.setWeight(yNewWeight);

            return true;
        }

        return false;
    }

    @Override
    protected boolean zigZagLeftTopDown (InfraNode z, Direction direction) {
        InfraNode y = z.getLeftChild();
        InfraNode x = y.getRightChild();
        InfraNode b = x.getLeftChild();
        InfraNode c = x.getRightChild();

        if (super.zigZagLeftTopDown(z, direction)) {
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
    protected boolean zigZagRightTopDown (InfraNode z, Direction direction) {
        InfraNode y = z.getRightChild();
        InfraNode x = y.getLeftChild();
        InfraNode b = x.getRightChild();
        InfraNode c = x.getLeftChild();

        if (super.zigZagRightTopDown(z, direction)) {
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
        return Math.log(value) / Math.log(2);
    }

    private double zigDiffRank (InfraNode x, InfraNode y) {
        /*
                     y                   x
                   /   \               /   \
                  x     c     -->     a     y
                 / \                       / \
                a   b                     b    c
        */
        boolean leftZig = (x.getId() == y.getLeftChild().getId());

        InfraNode b = (leftZig) ? x.getRightChild() : x.getLeftChild();

        long xOldWeight = x.getWeight();
        long yOldWeight = y.getWeight();

        long bWeight = (b.getId() != -1) ? b.getWeight() : 0;

        long yNewWeight = yOldWeight - xOldWeight + bWeight;
        long xNewWeight = xOldWeight - bWeight + yNewWeight;

        double xOldRank = (xOldWeight == 0) ? 0 : log2(xOldWeight);
        double yOldRank = (yOldWeight == 0) ? 0 : log2(yOldWeight);
        double xNewRank = (xNewWeight == 0) ? 0 : log2(xNewWeight);
        double yNewRank = (yNewWeight == 0) ? 0 : log2(yNewWeight);

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
        boolean lefZigZag = (y.getId() == z.getLeftChild().getId());

        InfraNode b = lefZigZag ? x.getLeftChild() : x.getRightChild();
        InfraNode c = lefZigZag ? x.getRightChild() : x.getLeftChild();

        long xOldWeight = x.getWeight();
        long yOldWeight = y.getWeight();
        long zOldWeight = z.getWeight();

        long bWeight = (b.getId() != -1) ? b.getWeight() : 0;
        long cWeight = (c.getId() != -1) ? c.getWeight() : 0;

        long yNewWeight = yOldWeight - xOldWeight + bWeight;
        long zNewWeight = zOldWeight - yOldWeight + cWeight;
        long xNewWeight = xOldWeight - bWeight - cWeight + yNewWeight + zNewWeight;

        double xOldRank = (xOldWeight == 0) ? 0 : log2(xOldWeight);
        double yOldRank = (yOldWeight == 0) ? 0 : log2(yOldWeight);
        double zOldRank = (zOldWeight == 0) ? 0 : log2(zOldWeight);
        double xNewRank = (xNewWeight == 0) ? 0 : log2(xNewWeight);
        double yNewRank = (yNewWeight == 0) ? 0 : log2(yNewWeight);
        double zNewRank = (zNewWeight == 0) ? 0 : log2(zNewWeight);

        double deltaRank = xNewRank + yNewRank + zNewRank - xOldRank - yOldRank - zOldRank;

        return deltaRank;
    }

    @Override
    public Rotation getRotationToPerform (InfraNode x, Direction direction) {
        double maxDelta = 0;
        Rotation operation = Rotation.NULL;

        if (direction == Direction.PARENTROUT) {
            return Rotation.NULL;

        } else if (direction == Direction.LEFTROUT) {
            return Rotation.NULL;

        } else if (direction == Direction.LEFTROUT) {
            return Rotation.NULL;

        }

        /*bottom-up - BEGIN*/
        if (this.isValidNode(x.getParent()) && this.isValidNode(x.getParent().getParent())) {
            InfraNode y = x.getParent();
            InfraNode z = y.getParent();
            if (
                this.isValidNode(y.getLeftChild()) && x.getId() == y.getLeftChild().getId() &&
                this.isValidNode(z.getLeftChild()) && y.getId() == z.getLeftChild().getId()
            ) {
                    double aux = zigDiffRank(y, z);

                    if (aux < maxDelta) {
                            maxDelta = aux;
                            operation = Rotation.ZIGZIGLEFT_BOTTOMUP;
                    }
            } else if (
                this.isValidNode(y.getRightChild()) && x.getId() == y.getRightChild().getId() &&
                this.isValidNode(z.getRightChild()) && y.getId() == z.getRightChild().getId()
            ) {
                    double aux = zigDiffRank(y, z);
                    if (aux < maxDelta) {
                            maxDelta = aux;
                            operation = Rotation.ZIGZIGRIGHT_BOTTOMUP;
                    }
            } else if (
                this.isValidNode(y.getRightChild()) && x.getId() == y.getRightChild().getId() &&
                this.isValidNode(z.getLeftChild()) && y.getId() == z.getLeftChild().getId()
            ) {
                    double aux = zigZagDiffRank(x, y, z);
                    if (aux < maxDelta) {
                            maxDelta = aux;
                            operation = Rotation.ZIGZAGLEFT_BOTTOMUP;
                    }
            } else if (
                this.isValidNode(y.getLeftChild()) && x.getId() == y.getLeftChild().getId() &&
                this.isValidNode(z.getRightChild()) && y.getId() == z.getRightChild().getId()
            ) {
                    double aux = zigZagDiffRank(x, y, z);
                    if (aux < maxDelta) {
                            maxDelta = aux;
                            operation = Rotation.ZIGZAGRIGHT_BOTTOMUP;
                    }
            }
        }

        /*top-down - BEGIN*/
        if (this.isValidNode(x.getLeftChild())) {
                InfraNode y = x.getLeftChild();

                if (this.isValidNode(y.getLeftChild())) {
                        InfraNode z = y.getLeftChild();
                        double aux = zigDiffRank(y, z);
                        if (aux < maxDelta) {
                                maxDelta = aux;
                                operation = Rotation.ZIGZIGLEFT_TOPDOWN;
                        }
                }

                if (this.isValidNode(y.getRightChild())) {
                        InfraNode z = y.getRightChild();
                        double aux = zigZagDiffRank(x, y, z);
                        if (aux < maxDelta) {
                                maxDelta = aux;
                                operation = Rotation.ZIGZAGLEFT_TOPDOWN;
                        }
                }
        }

        if (this.isValidNode(x.getRightChild())) {
                InfraNode y = x.getRightChild();

                if (this.isValidNode(y.getRightChild())) {
                        InfraNode z = y.getRightChild();
                        double aux = zigDiffRank(y, z);
                        if (aux < maxDelta) {
                                maxDelta = aux;
                                operation = Rotation.ZIGZIGRIGHT_TOPDOWN;
                        }
                }

                if (this.isValidNode(y.getLeftChild())) {
                        InfraNode z = y.getLeftChild();
                        double aux = zigZagDiffRank(x, y, z);
                        if (aux < maxDelta) {
                                maxDelta = aux;
                                operation = Rotation.ZIGZAGRIGHT_TOPDOWN;
                        }
                }
        }

        if (maxDelta < this.epsilon) {
            return operation;

        } else {
            return Rotation.NULL;

        }
    }

    private void incrementPathWeight (int from, int to) {
        this.tree.get(from - 1).incrementPathWeight(to - 1, false);
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

                this.remainingMessage.set(
                    optmsg.getSrc(), this.remainingMessage.get(optmsg.getSrc()) - 1
                );

                this.seq = true;

            } else if (msg instanceof NewMessage) {
                NewMessage newmsg = (NewMessage) msg;

                this.rcvMsgs++;
                this.remainingMessage.set(
                    newmsg.getSrc(), this.remainingMessage.get(newmsg.getSrc()) + 1
                );

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
