package projects.opticalNet.nodes.nodeImplementations;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.PriorityQueue;

import projects.opticalNet.nodes.infrastructureImplementations.LoggerLayer;
import projects.opticalNet.nodes.messages.HasMessage;
import projects.opticalNet.nodes.messages.RoutingInfoMessage;
import projects.opticalNet.nodes.models.Direction;
import projects.opticalNet.nodes.models.InfraNode;
import projects.opticalNet.nodes.models.Rotation;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.Tools;

public abstract class NetworkController extends LoggerLayer {

    /* Attributes */
    private ArrayList<Boolean> usedNodes;
    protected PriorityQueue<HasMessage> nodesWithMsg = new PriorityQueue<HasMessage>();
    protected PriorityQueue<RoutingInfoMessage> routingNodes = new PriorityQueue<RoutingInfoMessage>();

    protected ArrayList<InfraNode> tree;
    protected ArrayList<NetworkSwitch> switches;
    protected ArrayList<NetworkNode> netNodes;

    protected int numNodes = 0;
    protected int switchSize = 0;
    protected int numSwitches = 0;
    protected int numClustersType1 = 0;
    protected int numClustersType2 = 0;
    protected int clusterSize;

    protected int rcvMsgs = 0;
    protected ArrayList<Integer> remainingMessage;

    private static final int SIZE_CLUSTER_TYPE1 = 4;
    private static final int SIZE_CLUSTER_TYPE2 = 4;

    /* End of Attributes */

    public NetworkController (int numNodes, int switchSize, ArrayList<NetworkNode> netNodes) {
        this(numNodes, switchSize, netNodes, new ArrayList<Integer>());
    }

    public NetworkController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, ArrayList<Integer> edgeList
    ) {
        this.numNodes = numNodes;
        this.switchSize = switchSize;
        this.tree = new ArrayList<>();
        this.switches = new ArrayList<>();
        this.netNodes = netNodes;

        this.remainingMessage = new ArrayList<>();

        this.clusterSize = this.switchSize / 2;
        this.numClustersType1 = (this.numNodes + this.clusterSize - 1) / this.clusterSize;
        this.numClustersType2 = (
            this.numClustersType1 > 1 ?
            this.unionPos(this.numClustersType1 - 2, this.numClustersType1 - 1) + 1 :
            0
        );
        this.numSwitches = (
            this.numClustersType1 * SIZE_CLUSTER_TYPE1 +
            this.numClustersType2 * SIZE_CLUSTER_TYPE2
        );

        for (int clsId = 0; clsId < this.numClustersType1; clsId++) {
            for (int i = 0; i < 4; i++) {
                NetworkSwitch swt = new NetworkSwitch(
                    clsId * this.clusterSize + 1, (clsId + 1) * this.clusterSize, this.netNodes
                );
                swt.setIndex(this.switches.size() + 1);

                this.switches.add(swt);
            }
        }

        for (int clsId1 = 0; clsId1 < this.numClustersType1; clsId1++) {
            for (int clsId2 = clsId1 + 1; clsId2 < this.numClustersType1; clsId2++) {
                NetworkSwitch swt = new NetworkSwitch(
                    clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                    clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                    this.netNodes
                );
                swt.setIndex(this.switches.size() + 1);

                this.switches.add(swt);

                NetworkSwitch swt2 = new NetworkSwitch(
                    clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                    clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                    this.netNodes
                );
                swt2.setIndex(this.switches.size() + 1);

                this.switches.add(swt2);

                swt2 = new NetworkSwitch(
                    clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                    clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                    this.netNodes
                );
                swt2.setIndex(this.switches.size() + 1);

                this.switches.add(swt2);

                swt = new NetworkSwitch(
                    clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                    clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                    this.netNodes
                );
                swt.setIndex(this.switches.size() + 1);

                this.switches.add(swt);
            }
        }

        this.setupTree(edgeList);
        this.resetRoundInfo();
    }

    @Override
    public void init () {
        super.init();
    }

    private void setupTree (ArrayList<Integer> edgeList) {
        this.usedNodes = new ArrayList<>();

        for (int i = 0; i <= this.numNodes; i++) {
            this.usedNodes.add(false);
            this.tree.add(new InfraNode());

            this.remainingMessage.add(0);

            if (edgeList.size() < this.numNodes) {
                edgeList.add(i + 1);
            }
        }

        for (int i = 0; i < this.numNodes; i++) {
            this.setInitialCon(this.tree.get(edgeList.get(i)), this.tree.get(i));
        }
    }

    /* Rotations */
    protected boolean zigZigBottomUp (InfraNode x, Direction direction) {
        /*
                 z                 *y
                / \               /   \
               y   d             x     z
              / \      -->      / \   / \
            *x   c             a   b c   d
            / \
           a   b
        */

        InfraNode y = x.getParent();
        InfraNode z = y.getParent();
        InfraNode w = z.getParent();

        boolean leftZigZig = (y.getId() == z.getLeftChild().getId());
        InfraNode c = (leftZigZig) ? y.getRightChild() : y.getLeftChild();

        if (direction == Direction.PARENT && this.areAvailableNodes(w, x, z, y, c)) {
            this.logRotation(1, w, z, y, c);

            this.mapConn(z, c, y);
            this.mapConn(y, z);
            this.mapConn(w, y);

            return true;
        }

        return false;
    }

    protected boolean zigZagBottomUp (InfraNode x, Direction direction) {
        /*
                  w              w
                 /              /
                z		      *x
               / \            /   \
              y   d          y     z
             / \	   -->  / \   / \
            a   x*         a   b c   d
               / \
              b   c
        */

        InfraNode y = x.getParent();
        InfraNode z = y.getParent();
        InfraNode w = z.getParent();

        boolean leftZigZag = (y.getId() == z.getLeftChild().getId());
        InfraNode b = (leftZigZag) ? x.getLeftChild() : x.getRightChild();
        InfraNode c = (leftZigZag) ? x.getRightChild() : x.getLeftChild();

        if (direction == Direction.PARENT && this.areAvailableNodes(w, z, y, x, b, c)) {
            this.logRotation(2, w, z, y, x, b, c);

            this.mapConn(y, b, x);
            this.mapConn(x, y);
            this.mapConn(z, c, x);
            this.mapConn(x, z);
            this.mapConn(w, x);

            return true;
        }

        return false;
    }

    protected boolean zigZigLeftTopDown (InfraNode z, Direction direction) {
        /*
                 *z                    y
                 / \                 /   \
                y   d     -->      *x     z
               / \                 / \   / \
              x   c               a   b c   d
             / \
            a   b
        */
        InfraNode w = z.getParent();
        InfraNode y = z.getLeftChild();
        InfraNode x = y.getLeftChild();
        InfraNode c = y.getRightChild();

        if (direction == Direction.LEFT && this.areAvailableNodes(w, x, z, y, c)) {
            this.logRotation(1, w, z, y, c);

            this.mapConn(z, c, y);
            this.mapConn(y, z);
            this.mapConn(w, y);

            return true;
        }

        return false;
    }

    protected boolean zigZigRightTopDown (InfraNode z, Direction direction) {
        InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode x = y.getRightChild();
        InfraNode c = y.getLeftChild();

        if (direction == Direction.RIGHT && this.areAvailableNodes(w, x, z, y, c)) {
            this.logRotation(1, w, z, y, c);

            this.mapConn(z, c, y);
            this.mapConn(y, z);
            this.mapConn(w, y);

            return true;
        }

        return false;
    }

    protected boolean zigZagLeftTopDown (InfraNode z, Direction direction) {
        /*
                 *z                     x
                 / \        -->       /   \
                y   d                y     z
               / \                  / \   / \
              a   x                a  *b *c  d
                 / \
                b   c
        */
        InfraNode w = z.getParent();
        InfraNode y = z.getLeftChild();
        InfraNode x = y.getRightChild();
        InfraNode b = x.getLeftChild();
        InfraNode c = x.getRightChild();

        if (direction == Direction.LEFT && this.areAvailableNodes(w, z, y, x, b, c)) {
            this.logRotation(2, w, z, y, x, b, c);

            this.mapConn(y, b, x);
            this.mapConn(x, y);
            this.mapConn(z, c, x);
            this.mapConn(x, z);
            this.mapConn(w, x);

            return true;
        }

        return false;
    }

    protected boolean zigZagRightTopDown (InfraNode z, Direction direction) {
        InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode x = y.getLeftChild();
        InfraNode b = x.getRightChild();
        InfraNode c = x.getLeftChild();

        if (direction == Direction.RIGHT && this.areAvailableNodes(w, z, y, x, b, c)) {
            this.logRotation(2, w, z, y, x, b, c);

            this.mapConn(y, b, x);
            this.mapConn(x, y);
            this.mapConn(z, c, x);
            this.mapConn(x, z);
            this.mapConn(w, x);

            return true;
        }

        return false;
    }

    protected boolean areAvailableNodes (InfraNode... nodes) {
        for (InfraNode infNode: nodes) {
            if (infNode.getId() != -1 && this.usedNodes.get(infNode.getId())) {
                return false;

            }
        }

        for (InfraNode infNode: nodes) {
            if (infNode.getId() != -1) {
                this.usedNodes.set(infNode.getId(), true);

            }
        }

        return true;
    }
    /* End of Rotations */

    /* Getters */
    @Override
    public int getNumNodes () {
        return this.numNodes;
    }

    @Override
    public int getNumSwitches () {
        return this.numSwitches;
    }



    @Override
    public int getSwitchesPerClusterType1 () {
    	return NetworkController.SIZE_CLUSTER_TYPE1;

    }

    public int getSwitchesPerClusterType2 () {
    	return NetworkController.SIZE_CLUSTER_TYPE2;

    }

    @Override
    public int getNumClustersType2 () {
        return this.numClustersType2;
    }

    @Override
    public int getNumClustersType1 () {
        return this.numClustersType1;

    }

    public int getNumClusters () {
        return this.numClustersType1 + this.numClustersType2;

    }

    public int getClusterSize () {
        return this.clusterSize;
    }

    public int getSwitchSize () {
        return this.switchSize;
    }

    public InfraNode getInfraNode (int nodeId) {
        return this.tree.get(nodeId);
    }

    public NetworkNode getNetNode (int nodeId) {
        return this.netNodes.get(nodeId - 1);
    }

    protected int getClusterId (InfraNode fromNode, InfraNode toNode) {
        /*
                The clusterId of two nodes in the same cluster is calculated
                by the floor of the division between any of the nodes ids and the
                size of the clusters of the system.

                The clusterId of two nodes in different clusters is calculated
                by adding the number of clusters and the position of the UnionCluster
                from unionPos.
        */
        if (this.areSameCluster(fromNode, toNode)) {
            return this.getClusterId(fromNode);

        } else {
            return this.numClustersType1 + this.unionPos(
                this.getClusterId(fromNode), this.getClusterId(toNode)
            );
        }
    }

    protected int getClusterId (InfraNode node) {
        /*
                The clusterId of a given node is calculated by the floor of the
                division between the Node Id and the size of the clusters of the
                system.
        */
        return node.getId() / this.clusterSize;
    }

    @Override
    protected int getRoutingSwitchId (int fromNodeId, int toNodeId) {
        InfraNode fromNode = tree.get(fromNodeId);
        InfraNode toNode = tree.get(toNodeId);

        return this.getSwitchId(fromNode, toNode) + (toNode == fromNode.getParent() ? 1 : 0);
    }

    protected int getSwitchId (InfraNode fromNode, InfraNode toNode) {
        /*
                To find the switchId between two nodes from the same cluster
                we need to multiply by 4 the numbers of clusters of type 1
                prior to our. Then adding 2 to the result if it is a right edge.

                To find the switchId between two nodes from different clusters
                we need to multiply by 4 the numbers of clusters of type 1.
                Then we add 4 times the number clusters of type 2 prior to our.
        */
        int previousSwitches = (
                this.areSameCluster(fromNode, toNode) ?
                this.getClusterId(fromNode, toNode) * SIZE_CLUSTER_TYPE1 :
                this.numClustersType1 * SIZE_CLUSTER_TYPE1 + this.unionPos(
                        this.getClusterId(fromNode), this.getClusterId(toNode)
                ) * SIZE_CLUSTER_TYPE2
        );
        return previousSwitches + 2 * (fromNode.getId() > toNode.getId() ? 1 : 0);
    }

    public NetworkSwitch getSwitch (InfraNode fromNode, InfraNode toNode) {
        return this.switches.get(this.getSwitchId(fromNode, toNode));
    }

    public NetworkSwitch getSwitch (int switchId) {
        return this.switches.get(switchId);
    }

    protected boolean areSameCluster (InfraNode node1, InfraNode node2) {
        return this.getClusterId(node1) == this.getClusterId(node2);
    }

    @Override
    protected boolean isValidNode (InfraNode node) {
        if (node.getId() == -1) {
            return false;

        } else if (node.getId() == this.numNodes) {
            return false;

        }

        return true;
    }

    /* End of Getters */

    /* Setters */
    private void setInitialCon (InfraNode fromNode, InfraNode toNode) {
        int swtId = this.getSwitchId(fromNode, toNode);
        int subtreeId = fromNode.setChild(toNode) + 1;

        if (fromNode.getId() == this.numNodes) {
            return;

        } else if (toNode.getId() == this.numNodes) {
            Tools.fatalError("Trying to make root node as a child");

        }

        this.getSwitch(swtId + 1).updateSwitch(toNode.getId() + 1, fromNode.getId() + 1);
        this.getSwitch(swtId).updateSwitch(fromNode.getId() + 1, toNode.getId() + 1, subtreeId);

        return;
    }

    private void mapConn (InfraNode fromNode, InfraNode toNode) {
        this.mapConn(fromNode, toNode, new InfraNode(-1));
    }

    private void mapConn (InfraNode fromNode, InfraNode toNode, InfraNode oldParent) {
        int swtId = this.getSwitchId(fromNode, toNode);
        int subtreeId = fromNode.setChild(toNode, oldParent) + 1;

        if (fromNode.getId() == this.numNodes) {
            this.getNetNode(toNode.getId() + 1).removeParent();
            return;

        } else if (toNode.getId() == -1 && fromNode.getId() > oldParent.getId()) {
            this.getNetNode(fromNode.getId() + 1).removeLeftChild();
            return;

        } else if (toNode.getId() == -1 && fromNode.getId() < oldParent.getId()) {
            this.getNetNode(fromNode.getId() + 1).removeRightChild();
            return;

        } else if (toNode.getId() == this.numNodes) {
            Tools.fatalError("Trying to make root node as a child");

        }

        this.logIncrementAlterations(swtId, fromNode.getId(), toNode.getId());

        this.getSwitch(swtId + 1).updateSwitch(toNode.getId() + 1, fromNode.getId() + 1);
        this.getSwitch(swtId).updateSwitch(fromNode.getId() + 1, toNode.getId() + 1, subtreeId);
    }

    /* End of Setters

    /* Auxiliary Functions */
    protected int unionPos (int clsId1, int clsId2) {
        /*
            To calculate the postion of a UnionCluster we compute the
            summation from (NUM_CLUSTERS - 1) to (NUM_CLUSTER - minimum(clsId1, clsId2))
            and add to the result the distance between clsId1 to clsId2.
        */
        if (clsId1 > clsId2) {
            int aux = clsId1;
            clsId1 = clsId2;
            clsId2 = aux;
        }

        /*
            AP(n) = ((a0 + an) * n) / 2
            a0 = NUM_CLUSTERS - 1
            an = NUM_CLUSTER - 1 - clsId1 + 1
            n = clsId1
        */

        int apSum = (
            clsId1 != 0 ?
            ((2 * this.numClustersType1 - 1 - clsId1) * (clsId1)) / 2 :
            0
        );
        return apSum + clsId2 - clsId1 - 1;
    }

    public abstract Rotation getRotationToPerform (InfraNode x, Direction direction);

    private void lockRoutingNodes () {
        while (!this.routingNodes.isEmpty()) {
            RoutingInfoMessage routmsg = this.routingNodes.poll();
            int nodeId = routmsg.getNodeId();

            ArrayList<InfraNode> lockNodes = new ArrayList<>();
            InfraNode node = this.tree.get(nodeId - 1);
            lockNodes.add(node);

            for (int i = 1; i <= routmsg.getRoutingTimes(); i++) {
                InfraNode nxtNode = node.getRoutingNode(routmsg.getDst() - 1);
                if (nxtNode.getId() == -1 || nxtNode.getId() == node.getId()) {
                    break;

                }

                lockNodes.add(nxtNode);
                node = nxtNode;
            }

            if (this.areAvailableNodes(lockNodes.toArray(new InfraNode[0]))) {
                this.sendDirect(routmsg, this.getNetNode(nodeId));

            } else {
                Tools.fatalError("Nodes that were supposed to rout are already occupied");

            }
        }

    }

    public void updateConn () {
        this.lockRoutingNodes();

        while (!this.nodesWithMsg.isEmpty()) {
            HasMessage hasmsg = this.nodesWithMsg.poll();
            int nodeId = hasmsg.getCurrId();
            InfraNode node = this.tree.get(nodeId - 1);
            Direction direction = node.getRoutingDirection(hasmsg.getDst() - 1);

            Rotation op = this.getRotationToPerform(node, direction);
            boolean allowRouting = (op == Rotation.NULL);

            switch (op) {
                case ZIGZIGLEFT_BOTTOMUP:
                case ZIGZIGRIGHT_BOTTOMUP:
                    if (this.zigZigBottomUp(node, direction)) {
                        System.out.println("zigZigBottomUp");
                        this.logIncrementActiveRequests();

                        this.sendDirect(new RoutingInfoMessage(1), this.getNetNode(nodeId));

                    } else {
                        allowRouting = true;

                    }

                    break;

                case ZIGZAGLEFT_BOTTOMUP:
                case ZIGZAGRIGHT_BOTTOMUP:
                    if (this.zigZagBottomUp(node, direction)) {
                        System.out.println("zigZagBottomUp");
                        this.logIncrementActiveRequests();

                    } else {
                        allowRouting = true;

                    }

                    break;

                case ZIGZIGLEFT_TOPDOWN:
                    if (this.zigZigLeftTopDown(node, direction)) {
                        System.out.println("zigZigLeftTopDown");
                        this.logIncrementActiveRequests();

                        this.sendDirect(new RoutingInfoMessage(2), this.getNetNode(nodeId));

                    } else {
                        allowRouting = true;

                    }

                    break;

                case ZIGZAGLEFT_TOPDOWN:
                    if (this.zigZagLeftTopDown(node, direction)) {
                        System.out.println("zigZagLeftTopDown");
                        this.logIncrementActiveRequests();

                        InfraNode rfrshNode = this.tree.get(nodeId - 1);
                        InfraNode nxtNode = rfrshNode.getRoutingNode(hasmsg.getDst() - 1);

                        if (nxtNode == rfrshNode.getParent()) {
                            this.sendDirect(new RoutingInfoMessage(3), this.getNetNode(nodeId));

                        } else {
                            this.sendDirect(new RoutingInfoMessage(1), this.getNetNode(nodeId));

                        }

                    } else {
                        allowRouting = true;

                    }

                    break;

                case ZIGZIGRIGHT_TOPDOWN:
                    if (this.zigZigRightTopDown(node, direction)) {
                        System.out.println("zigZigRightTopDown");
                        this.logIncrementActiveRequests();

                        this.sendDirect(new RoutingInfoMessage(2), this.getNetNode(nodeId));

                    } else {
                        allowRouting = true;

                    }

                    break;

                case ZIGZAGRIGHT_TOPDOWN:
                    if (this.zigZagRightTopDown(node, direction)) {
                        System.out.println("zigZagRightTopDown");
                        this.logIncrementActiveRequests();

                        InfraNode rfrshNode = this.tree.get(nodeId - 1);
                        InfraNode nxtNode = rfrshNode.getRoutingNode(hasmsg.getDst() - 1);

                        if (nxtNode == rfrshNode.getParent()) {
                            this.sendDirect(new RoutingInfoMessage(3), this.getNetNode(nodeId));

                        } else {
                            this.sendDirect(new RoutingInfoMessage(1), this.getNetNode(nodeId));

                        }

                    } else {
                        allowRouting = true;

                    }

                    break;

                default:
                    break;
            }

            if (allowRouting) {
                InfraNode x = this.tree.get(nodeId - 1);
                InfraNode y = x.getRoutingNode(direction);

                if (this.areAvailableNodes(x, y)) {
                    this.logIncrementActiveRequests();
                    this.sendDirect(new RoutingInfoMessage(1), this.getNetNode(nodeId));

                }
            }

        }
    }

    public void debugInfraTree () {
        this.debugInfraTree(this.tree.get(this.numNodes));
    }

    public void debugInfraTree (InfraNode node) {
        if (node.getId() == -1)
            return;

        node.debugNode();

        System.out.println("Left");
        this.debugInfraTree(node.getLeftChild());

        System.out.println("Right");
        this.debugInfraTree(node.getRightChild());
    }

    /* End of Auxiliary Functions */

    public void checkRoundConfiguration () {
        if (!this.validTree()) {
            Tools.fatalError("Invalid infra tree");
        }

        for (int i = 0; i < this.numNodes; i++)  {
            if (!this.equivalentNodes(i)) {
                Tools.fatalError("InfraNode: " + i + " not equivalent to its network correspondent");

            }
        }
    }

    @Override
    public void controllerStep () {
        this.checkRoundConfiguration();

        for (int i = 0; i <= this.numNodes; i++)
            this.usedNodes.set(i, false);

        this.updateConn();

        int missingMessages = 0;
        for (int i = 0; i <= this.numNodes; i++) {
            missingMessages += this.remainingMessage.get(i);

        }

        System.out.println("Received Messages: " + this.rcvMsgs + " Missing Messages: " + missingMessages);
    }

    private boolean equivalentNodes (int nodeId) {
        InfraNode infraNode = this.tree.get(nodeId);
        NetworkNode netNode = this.netNodes.get(nodeId);

        boolean flag = infraNode.getId() == netNode.getId() - 1;
        flag &= (
                infraNode.getLeftChild().getId() != -1 ?
                infraNode.getLeftChild().getId() == netNode.getLeftChildId() - 1 :
                netNode.getLeftChildId() == -1
        );
        flag &= (
                infraNode.getRightChild().getId() != -1 ?
                infraNode.getRightChild().getId() == netNode.getRightChildId() - 1 :
                netNode.getRightChildId() == -1
        );
        flag &= (
                (
                    infraNode.getParent().getId() != -1 &&
                    infraNode.getParent().getId() != this.numNodes
                ) ?
                infraNode.getParent().getId() == netNode.getParentId() - 1 :
                netNode.getParentId() == -1
        );
        flag &= infraNode.getMinId() == netNode.getMinIdInSubtree() - 1;
        flag &= infraNode.getMaxId() == netNode.getMaxIdInSubtree() - 1;

        if (!flag) {
            infraNode.debugNode();
            netNode.debugNode();
        }

        return flag;
    }

    private boolean validTree () {
        return this.validSubtree(this.tree.get(this.numNodes).getLeftChild(), 0, this.numNodes - 1);
    }

    private boolean validSubtree (InfraNode x, int min, int max) {
        if (x.getId() == -1) {
            return true;

        }

        if (x.getId() < min || x.getId() > max) {
            x.debugNode();
            return false;

        }

        if (this.isValidNode(x) && this.isValidNode(x.getParent())) {
            int swtId = this.getRoutingSwitchId(x.getParent().getId(), x.getId());
            this.logIncrementActivePorts(swtId);

        }

        return (
            this.validSubtree(x.getLeftChild(), min, x.getId() - 1) &&
            this.validSubtree(x.getRightChild(), x.getId() + 1, max)
        );
    }

    @Override
    public void draw (Graphics g, PositionTransformation pt, boolean highlight) { }

}
