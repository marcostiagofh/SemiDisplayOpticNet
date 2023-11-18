package projects.bstOpticalNet.nodes.nodeImplementations;

import java.awt.Graphics;
import java.util.Deque;
import java.util.Stack;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.PriorityQueue;

import projects.bstOpticalNet.nodes.infrastructureImplementations.LoggerLayer;
import projects.bstOpticalNet.nodes.messages.HasMessage;
import projects.bstOpticalNet.nodes.messages.NewMessage;
import projects.bstOpticalNet.nodes.messages.OpticalNetMessage;
import projects.bstOpticalNet.nodes.messages.RoutingInfoMessage;
import projects.bstOpticalNet.nodes.models.Edge;
import projects.bstOpticalNet.nodes.models.Rotation;
import projects.bstOpticalNet.nodes.models.Direction;
import projects.bstOpticalNet.nodes.models.InfraNode;
import projects.bstOpticalNet.nodes.models.AvailablePorts;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

/**
 * The NetworkController is the abstract class of the controller node that will act
 * as the intelligence of our algorithm. It initializes the simulation switches, inputNodes and
 * outputNodes. It has the rotations implementations as well as their alteration methods.
 */
public abstract class NetworkController extends LoggerLayer {
    protected boolean seq = true;

    /* Attributes */
    private ArrayList<Boolean> usedNodes;
    protected PriorityQueue<HasMessage> nodesWithMsg = new PriorityQueue<HasMessage>();
    protected PriorityQueue<RoutingInfoMessage> routingNodes = new PriorityQueue<RoutingInfoMessage>();
    protected Stack<Edge> rmvEdges = new Stack<Edge>();
    protected Stack<Edge> swapEdges = new Stack<Edge>();
    protected Deque<Edge> addEdges = new ArrayDeque<Edge>();

    protected ArrayList<InfraNode> tree;
    protected ArrayList<NetworkNode> netNodes;
    protected ArrayList<ArrayList<NetworkSwitch>> clusters;

    protected int numNodes = 0;
    protected int switchSize = 0;
    protected int numSwitches = 0;
    protected int numClustersType1 = 0;
    protected int numClustersType2 = 0;
    protected int clusterSize;
    protected boolean mirrored;

    protected int rcvMsgs = 0;
    protected int cmpMsgs = 0;

    private int SIZE_CLUSTER_TYPE1;
    private int SIZE_CLUSTER_TYPE2 = 4;

    /* End of Attributes */

    /**
     * Initializes the NetworkController and its switches. The constructor builds the
     * network as a balanced BST.
     * @param numNodes      Number of nudes in the network
     * @param switchSize    Number of input/output ports in the switch
     * @param netNodes      Array with the initialized NetworkNodes
     */
        public NetworkController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, boolean mirrored
    ) {
        this(numNodes, switchSize, netNodes, new ArrayList<Integer>(), mirrored);
    }

    /**
     * Initializes the NetworkController and its switches. If an edgeList is provided the tree
     * topology follow the specified one. If the edge list can't build an BST, the cons'tructor
     * builds a balanced BST instead.
     * @param numNodes      Number of nudes in the network
     * @param switchSize    Number of input/output ports in the switch
     * @param netNodes      Array with the initialized NetworkNodes
     * @param edgeList      Array with the network edges, if provided.
     */
    public NetworkController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes,
        ArrayList<Integer> edgeList, boolean mirrored
    ) {
        this.numNodes = numNodes;
        this.numSwitches = 0;
        this.switchSize = switchSize;
        this.tree = new ArrayList<>();
        this.clusters = new ArrayList<>();
        this.netNodes = netNodes;
        this.mirrored = mirrored;

        if (mirrored) {
            this.SIZE_CLUSTER_TYPE1 = 4;

        } else {
            this.SIZE_CLUSTER_TYPE1 = 3;
        }

        this.clusterSize = this.switchSize / 2;
        this.numClustersType1 = (this.numNodes + this.clusterSize - 1) / this.clusterSize;
        this.numClustersType2 = (
            this.numClustersType1 > 1 ?
            this.unionPos(this.numClustersType1 - 2, this.numClustersType1 - 1) + 1 :
            0
        );

        for (int clsId = 0; clsId < this.numClustersType1; clsId++) {
            this.clusters.add(new ArrayList<>());

            for (int i = 0; i < SIZE_CLUSTER_TYPE1; i++) {
                NetworkSwitch swt = new NetworkSwitch(
                    clsId * this.clusterSize + 1, (clsId + 1) * this.clusterSize, i,  this.netNodes
                );
                swt.setIndex(this.numSwitches++);

                this.clusters.get(clsId).add(swt);
            }
        }

        for (int clsId1 = 0; clsId1 < this.numClustersType1; clsId1++) {
            for (int clsId2 = clsId1 + 1; clsId2 < this.numClustersType1; clsId2++) {
                int clsId = this.clusters.size();
                this.clusters.add(new ArrayList<>());

                for (int swtIdx = 0; swtIdx < SIZE_CLUSTER_TYPE2; swtIdx += 2) {
                    int minIdInput = (swtIdx % 4 == 0 ? clsId2 : clsId1) * this.clusterSize + 1;
                    int minIdOutput = (swtIdx % 4 == 0 ? clsId1 : clsId2) * this.clusterSize + 1;

                    NetworkSwitch swt = new NetworkSwitch(
                        minIdInput, minIdInput + this.clusterSize - 1,
                        minIdOutput, minIdOutput + this.clusterSize - 1, swtIdx, this.netNodes
                    );
                    swt.setIndex(this.numSwitches++);

                    this.clusters.get(clsId).add(swt);

                    swt = new NetworkSwitch(
                        minIdOutput, minIdOutput + this.clusterSize - 1,
                        minIdInput, minIdInput + this.clusterSize - 1, swtIdx + 1, this.netNodes
                    );
                    swt.setIndex(this.numSwitches++);

                    this.clusters.get(clsId).add(swt);
                }
            }
        }

        this.resetRoundInfo();
        this.setupTree(edgeList);
    }

    /**
     *  Initializes the network controller under the sinalgo simulation
     */
    @Override
    public void init () {
        super.init();
    }

    /**
     * Setup the network tree based on the provided edgeList. Building over the
     * edges, if they design a conected BST, or building a balanced BST, if not.
     * @param edgeList  Array with the network edges, if provided
     */
    private void setupTree (ArrayList<Integer> edgeList) {
        this.usedNodes = new ArrayList<>();

        for (int i = 0; i <= this.numNodes; i++) {
            this.usedNodes.add(false);
            this.tree.add(new InfraNode());

        }

        if (edgeList.size() != this.numNodes) {
            this.buildBalancedTree(1, this.numNodes);
            this.setInitialCon(
                this.getInfraNode(this.numNodes + 1),
                this.getInfraNode((1 + this.numNodes) / 2)
            );

        } else {
            for (int i = 1; i <= this.numNodes; i++) {
                this.setInitialCon(
                    this.getInfraNode(edgeList.get(i - 1)),
                    this.getInfraNode(i)
                );
            }

        }

        this.updateLinks();
    }

    /**
     * Recursive method to build a balanced BST, connecting the middle node between min and max,
     * to left with the minMiddle node, between min and the middle, and to the right with the
     * maxMiddle node, between the middle and max.
     * @param min       The minimum id of the subtree
     * @param max       The maximum id of the subtree
     */
    private void buildBalancedTree (int min, int max) {
        if (min >= max)
            return;

        int middle = (min + max) / 2;
        int minMiddle = (min + middle - 1) / 2;
        int maxMiddle = (max + middle + 1) / 2;

        if (minMiddle >= min) {
            this.buildBalancedTree(min, middle - 1);
            this.setInitialCon(this.getInfraNode(middle), this.getInfraNode(minMiddle));
        }

        if (maxMiddle <= max) {
            this.buildBalancedTree(middle + 1, max);
            this.setInitialCon(this.getInfraNode(middle), this.getInfraNode(maxMiddle));

        }

    }

    /* Rotations */

    /**
     * This method selects the nodes involved in alterations over a zig-zig
     * bottom up rotation and checks if they are available to perform this
     * rotation. If they are it calls the zig-zig alteration related method
     * and return true, if they arent return false.
     * @param x         The node with the message
     * @return          True of False wether the rotation is performed
     */
    protected boolean zigZigBottomUp (InfraNode x) {
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

        boolean leftZigZig = (y == z.getLeftChild());
        InfraNode c = (leftZigZig) ? y.getRightChild() : y.getLeftChild();

        if (this.areAvailableNodes(w, x, z, y, c)) {
            this.zigZigAlterations(w, z, y, c);

            return true;
        }

        return false;
    }

    /**
     * This method selects the nodes involved in alterations over a zig-zag
     * bottom up rotation and checks if they are available to perform this
     * rotation. If they are it calls the zig-zag alteration related method
     * and return true, if they arent return false.
     * @param x         The node with the message
     * @return          True of False wether the rotation is performed
     */
    protected boolean zigZagBottomUp (InfraNode x) {
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

        boolean leftZigZag = (y == z.getLeftChild());
        InfraNode b = (leftZigZag) ? x.getLeftChild() : x.getRightChild();
        InfraNode c = (leftZigZag) ? x.getRightChild() : x.getLeftChild();

        if (this.areAvailableNodes(w, z, y, x, b, c)) {
            this.zigZagAlterations(w, z, y, x, b, c);

            return true;
        }

        return false;
    }

    /**
     * This method selects the nodes involved in alterations over a zig-zig
     * left top down rotation and checks if they are available to perform this
     * rotation. If they are it calls the zig-zig alteration related method
     * and return true, if they arent return false.
     * @param z         The node with the message
     * @return          True of False wether the rotation is performed
     */
    protected boolean zigZigLeftTopDown (InfraNode z) {
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

        if (this.areAvailableNodes(w, x, z, y, c)) {
            this.zigZigAlterations(w, z, y, c);

            return true;
        }

        return false;
    }

    /**
     * This method selects the nodes involved in alterations over a zig-zig
     * right top down rotation and checks if they are available to perform this
     * rotation. If they are it calls the zig-zig alteration related method
     * and return true, if they arent return false.
     * @param z         The node with the message
     * @return          True of False wether the rotation is performed
     */
    protected boolean zigZigRightTopDown (InfraNode z) {
        InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode x = y.getRightChild();
        InfraNode c = y.getLeftChild();

        if (this.areAvailableNodes(w, x, z, y, c)) {
            this.zigZigAlterations(w, z, y, c);

            return true;
        }

        return false;
    }

    /**
     * This method selects the nodes involved in alterations over a zig-zag
     * left top down rotation and checks if they are available to perform this
     * rotation. If they are it calls the zig-zag alteration related method
     * and return true, if they arent return false.
     * @param z         The node with the message
     * @return          True of False wether the rotation is performed
     */
    protected boolean zigZagLeftTopDown (InfraNode z) {
        /*
                 *z                      x
                 / \        -->       /    \
                y   d                y      z
               / \                  / \    / \
              a   x                a  *b *c  d
                 / \
                b   c
        */
        InfraNode w = z.getParent();
        InfraNode y = z.getLeftChild();
        InfraNode x = y.getRightChild();
        InfraNode b = x.getLeftChild();
        InfraNode c = x.getRightChild();

        if (this.areAvailableNodes(w, z, y, x, b, c)) {
            this.zigZagAlterations(w, z, y, x, b, c);

            return true;
        }

        return false;
    }

    /**
     * This method selects the nodes involved in alterations over a zig-zag
     * right top down rotation and checks if they are available to perform this
     * rotation. If they are it calls the zig-zag alteration related method
     * and return true, if they arent return false.
     * @param z         The node with the message
     * @return          True of False wether the rotation is performed
     */
    protected boolean zigZagRightTopDown (InfraNode z) {
        InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode x = y.getLeftChild();
        InfraNode b = x.getRightChild();
        InfraNode c = x.getLeftChild();

        if (this.areAvailableNodes(w, z, y, x, b, c)) {
            this.zigZagAlterations(w, z, y, x, b, c);

            return true;
        }

        return false;
    }

    /**
     * This method realizes the alterations necessary on a zig-zig rotation based on the
     * provided nodes. z becomes the new parent of c, y becomes the new parent o z and
     * z becomes the new parent of y
     * @param w         old parent of node z
     * @param z         old parent of node y
     * @param y         old parent of node x
     * @param c         old child of y
     */
    private void zigZigAlterations (InfraNode w, InfraNode z, InfraNode y, InfraNode c) {
        this.logRotation(1);

        {
            this.pushRmvEdge(w, z, true);
            this.pushRmvEdge(z, w, false);

            if (this.mirrored) {
                this.pushRmvEdge(z, y, true);
                this.pushRmvEdge(y, z, false);
            }

            this.pushRmvEdge(y, c, true);
            this.pushRmvEdge(c, y, false);

        }

        this.mapConn(z, c);
        this.mapConn(y, z, this.mirrored);
        this.mapConn(w, y);

    }

    /**
     * This method realizes the alterations necessary on a zig-zag rotation based on the
     * provided nodes. y becomes the new parent of b, x becomes the new parent of y, z
     * becomes the new parent of c, x becomes the new parent of z and w becomes the new
     * parent o x
     * @param w         old parent of node z
     * @param z         old parent of node y
     * @param y         old parent of node x
     * @param x         node x
     * @param b         old child of x
     * @param c         old child of x
     */
    private void zigZagAlterations (
        InfraNode w, InfraNode z, InfraNode y, InfraNode x, InfraNode b, InfraNode c
    ) {
        /*
                 *z                      x
                 / \        -->       /    \
                y   d                y      z
               / \                  / \    / \
              a   x                a  *b *c  d
                 / \
                b   c
        */
        this.logRotation(2);

        {
            this.pushRmvEdge(w, z, true);
            this.pushRmvEdge(z, w, false);

            this.pushRmvEdge(z, y, true);
            this.pushRmvEdge(y, z, false);

            if (this.mirrored) {
                this.pushRmvEdge(y, x, true);
                this.pushRmvEdge(x, y, false);

            }

            this.pushRmvEdge(x, b, true);
            this.pushRmvEdge(b, x, false);

            this.pushRmvEdge(x, c, true);
            this.pushRmvEdge(c, x, false);
        }

        this.mapConn(y, b);
        this.mapConn(x, y, this.mirrored);
        this.mapConn(z, c);
        this.mapConn(x, z);
        this.mapConn(w, x);

    }

    /**
     * This function checks if a set of nodes are available to realize one step. If none of them
     * are already occupied this round it marks all of them as occupied and returns true, if not
     * return false.
     * @param nodes     Set of InfraNodes
     * @return          True if the nodes can be used for this step, false if at
     * least one of them are already occupied
     */
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
    /**
     * Getter for the number of network nodes in the network
     * @return          the number of nodes in the network
     */
    @Override
    public int getNumNodes () {
        return this.numNodes;
    }

    /**
     * Getter for the number of switches, the sum of number switches in clusters of type 1
     * and switches in clusters of type 2
     * @return          the number of switches
     */
    @Override
    public int getNumSwitches () {
        return this.numSwitches;
    }


    /**
     * Getter for the number of switches in the representation of clusters type 1
     * @return          the number of switches in clusters type 1
     */
    @Override
    public int getSwitchesPerClusterType1 () {
        return this.SIZE_CLUSTER_TYPE1;

    }

    /**
     * Getter for the number of switches in the representation of clusters type 2
     * @return          the number of switches in clusters type 2
     */
    public int getSwitchesPerClusterType2 () {
        return this.SIZE_CLUSTER_TYPE2;

    }

    /**
     * Getter for the numbers of clusters type 2. The result of the summation of 1 to
     * the number of i from 1 to the number of clusters type 1 - 1.
     * @return          the number of clusters type 2
     */
    @Override
    public int getNumClustersType2 () {
        return this.numClustersType2;
    }

    /**
     * Getter for the number of clusters type 1. Equals to the floor of (2 * V) / SwitchSize.
     * @return          the number of clusters type 1
     */
    @Override
    public int getNumClustersType1 () {
        return this.numClustersType1;

    }

    /**
     * Getter for the number of clusters type 1 and 2. Equals to the sum of clusters type 1
     * and clusters type 2
     * @return          the number of clusters
     */
    public int getNumClusters () {
        return this.numClustersType1 + this.numClustersType2;

    }

    /**
     * Getter for the number of vertices in a cluster. Equals to the number of ports
     * in a switch divided by 2
     * @return          the cluster size
     */
    public int getClusterSize () {
        return this.clusterSize;
    }

    /**
     * Getter for the number of ports in a clusters. Equals to the number of input ports +
     * output ports, provided by the start of the simulation
     * @return          the switch size
     */
    public int getSwitchSize () {
        return this.switchSize;
    }

    /**
     * Getter for the InfraNode with the specified id. Retrieves the InfraNode for the respective
     * NetworkNode with netNodeId
     * @param netNodeId the id of the network node
     * @return          the respective infra node
     */
    public InfraNode getInfraNode (int netNodeId) {
        return this.tree.get(netNodeId - 1);
    }

    /**
     * Getter for the NetworkNode with the specified id
     * @param netNodeId the id of the network node
     * @return          the respective network node
     */
    public NetworkNode getNetNode (int netNodeId) {
        return this.netNodes.get(netNodeId - 1);
    }

    /**
     * Getter for the NetworkNode equivalent to the InfraNode node.
     * @param node      the InfraNode
     * @return          the equivalent NetworkNode
     */
    public NetworkNode getNetNode (InfraNode node) {
        return this.netNodes.get(node.getId());

    }

    /**
     * Getter for the id of the cluster the edge fromNodeRtoNode and it's mirror
     * belongs to. </br>
     * The clusterId of two nodes in the same cluster is the floor of
     * the division between any of the nodes ids and the cluserSize. The clusterId of
     * two nodes in different clusters is calculated by adding the number of clusters
     * type 1, the maximum cluster type 1 id for fromNode or for toNode and the summation
     * of numberClustersType1 - 1 with i ranging between 1 and the minimum cluster type 1 id
     * for fromNode or for toNode.
     * @param fromNode  the parent node in the edge representation
     * @param toNode    the child node in the edge representation
     * @return          the cluster id
     */
    protected int getClusterId (InfraNode fromNode, InfraNode toNode) {
        if (this.areSameCluster(fromNode, toNode)) {
            return this.getClusterId(fromNode);

        } else {
            return this.numClustersType1 + this.unionPos(
                this.getClusterId(fromNode), this.getClusterId(toNode)
            );
        }
    }

    /**
     * Getter for the cluster id that holds every type 1 edge of the InfraNode node.
     * Given by the floor of the divistion between the node id and the clusterSize
     * @param node      InfraNode
     * @return          the cluster id
     */
    protected int getClusterId (InfraNode node) {
        return node.getId() / this.clusterSize;

    }

    /**
     * Getter for the switch id were the edge between fromNode and toNode can be represented.
     * Equals to the sum between SIZE_CLUSTER_TYPE1 * min(clusterId, numClustersType1)
     * SIZE_CLUSTER_TYPE_2 * max(0, clusterId - numClustersType1) and 1 if it is a mirrored edge,
     * toNode is the real parent of fromNode in the network, or 0 if it is a real edge,
     * fromNode is the parent of toNode.
     * @param fromNode  the parent node in the edge representation
     * @param toNode    the child node in the edge representation
     * @return          the switch id
     */
    @Override
    protected int getRoutingSwitchId (InfraNode fromNode, InfraNode toNode) {
    	int clsId = this.getClusterId(fromNode, toNode);
    	int swtOffset = fromNode.getSwtOffset(toNode);
        return this.clusters.get(clsId).get(swtOffset).getIndex();

    }

    /**
     * Getter for the seq flag.
     * @return  True if there is a message in the network false if there isn't
     */
    public boolean getSeq () {
        return this.seq;
    }

    /**
     * Sets the seq flag as false, called when a message reaches it's destination
     */
    public void setSeq () {
        this.seq = false;
    }

    /**
     * Returns a boolean true if the cluster type 1 id of both nodes are the same and
     * false if are different
     * @param node1 InfraNode x
     * @param node2 InfraNode y
     * @return      boolean
     */
    protected boolean areSameCluster (InfraNode node1, InfraNode node2) {
        return this.getClusterId(node1) == this.getClusterId(node2);
    }

    /**
     * Returns true if the InfraNode Id is between 0, is not a dummy node, and the number
     * of nodes in the network, is not the control root.
     *
     * @return      boolean
     */
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
    /**
     * [OUTDATED?] -> To be replaced the logIncrementation call in the mapConn needs to be replaced.
     */
    private void setInitialCon (InfraNode fromNode, InfraNode toNode) {
        if (toNode.getId() == this.numNodes) {
            Tools.fatalError("Trying to make root node as a child");

        }

        if (fromNode.getId() > toNode.getId()) {
            fromNode.setLeftChild(toNode);

        } else {
            fromNode.setRightChild(toNode);

        }

        toNode.setParent(fromNode);
        if (!this.isValidNode(fromNode))
            return;

        Edge downEdge = new Edge(fromNode, toNode, true, true);
        Edge upEdge = new Edge(toNode, fromNode, false, true);
        this.addEdges.addLast(downEdge);
        this.addEdges.addLast(upEdge);

        return;
    }

    /**
     * Sets the connection between fromNode and toNode. Updating the former connections
     * they shared.
     * @param fromNode  the parent node
     * @param toNode    the child node
     */
    private void mapConn (InfraNode fromNode, InfraNode toNode) {
        this.mapConn(fromNode, toNode, true);
    }

    /**
     * Sets the connection between fromNode and toNode. Updating the former connections
     * they shared.
     * @param fromNode  the parent node
     * @param toNode    the child node
     */
    private void mapConn (InfraNode fromNode, InfraNode toNode, boolean addEdge) {
        if (
            (this.isValidNode(fromNode) || fromNode.getId() == this.getNumNodes()) &&
            this.isValidNode(toNode)
        ) {
            Edge downEdge = new Edge(fromNode, toNode, true, false);
            Edge upEdge = new Edge(toNode, fromNode, false, false);
            boolean left = fromNode.getId() > toNode.getId();

            if (left) {
                fromNode.setLeftChild(toNode);

            } else {
                fromNode.setRightChild(toNode);

            }
            toNode.setParent(fromNode);

            if (addEdge && fromNode.getId() != this.getNumNodes()) {
	            this.addEdges.addLast(downEdge);
	            this.addEdges.addLast(upEdge);

            } else if (fromNode.getId() != this.getNumNodes()) {
            	this.swapEdges.push(downEdge);

            	if (left) {
            		fromNode.setLeftChildSwitchOffset(fromNode.getParentSwitchOffset());
            		toNode.setParentSwitchOffset(toNode.getRightChildSwitchOffset());

            	} else {
            		fromNode.setRightChildSwitchOffset(fromNode.getParentSwitchOffset());
            		toNode.setParentSwitchOffset(toNode.getLeftChildSwitchOffset());

            	}
            	fromNode.resetParent(toNode);
            	toNode.resetChild(fromNode);

            }
        }
    }

    private void updateLinks () {
        while (!this.rmvEdges.isEmpty()) {
            Edge edge = this.rmvEdges.pop();
            int clsId = this.getClusterId(edge.getFromNode(), edge.getToNode());
            NetworkSwitch swt = this.clusters.get(clsId).get(edge.getSwtOffset());

            swt.removeLink(
                edge.getFromNode().getNetId(), edge.getToNode().getNetId()
            );
            this.logDecrementActivePorts(swt.getIndex());

            if (edge.isDownward() && edge.getFromNodeId() > edge.getToNodeId()) {
                this.getNetNode(edge.getFromNode()).removeLeftChild();

            } else if (edge.isDownward()) {
                this.getNetNode(edge.getFromNode()).removeRightChild();

            } else {
                this.getNetNode(edge.getFromNode()).removeParent();

            }
        }

        while (!this.swapEdges.isEmpty()) {
            Edge edge = this.swapEdges.pop();
            InfraNode fromNode = edge.getFromNode();
            InfraNode toNode = edge.getToNode();

        	this.getNetNode(toNode).swapChild(fromNode.getNetId());
        	this.getNetNode(fromNode).swapParent();

        }

        while (!this.addEdges.isEmpty() && this.addEdges.peekFirst().getSwtOffset() == -1) {
            Edge edge = this.addEdges.removeFirst();

            this.addEdge(edge.getFromNode(), edge.getToNode(), edge.isDownward(), edge.isInitial());

        }

        this.logEssentialLinkUpdates();
    }

    private void addEdge (InfraNode fromNode, InfraNode toNode, boolean downward, boolean initial) {
        int clsId = this.getClusterId(fromNode, toNode);
        if (this.mirrored) {
            boolean left = (downward ?
                fromNode.getId() > toNode.getId() :
                toNode.getId() > fromNode.getId()
            );
            int swtOffset = (left ? 0 : 2) + (downward ? 0 : 1);
            Edge edge = new Edge(fromNode, toNode, downward, initial, swtOffset);
            NetworkSwitch swt = this.clusters.get(clsId).get(swtOffset);
            int swtId = swt.getIndex();

            this.logIncrementActivePorts(swtId);

            if (downward) {
                swt.updateChild(fromNode.getNetId(), toNode.getNetId());
                if (left) {
                    fromNode.setLeftChildSwitchOffset(swtOffset);

                } else {
                    fromNode.setRightChildSwitchOffset(swtOffset);

                }

            } else {
                swt.updateParent(fromNode.getNetId(), toNode.getNetId());
                fromNode.setParentSwitchOffset(swtOffset);

            }

            this.addEdges.addLast(edge);

        } else {
            NetworkSwitch inSwitch = null, outSwitch = null;

            for (int swtOff = 0; swtOff < this.clusters.get(clsId).size(); swtOff++) {
                NetworkSwitch swt = this.clusters.get(clsId).get(swtOff);
                AvailablePorts avPorts = swt.getAvailablePorts(
                    fromNode.getNetId(), toNode.getNetId()
                );

                if (avPorts == AvailablePorts.BOTH) {
                    inSwitch = swt;
                    outSwitch = swt;

                    break;

                } else if (avPorts == AvailablePorts.INPUT) {
                    inSwitch = swt;

                } else if (avPorts == AvailablePorts.OUTPUT) {
                    outSwitch = swt;

                }

            }

            if (inSwitch == null || outSwitch == null) {
                Tools.fatalError(
                    "It was not possible to find a switch within" +
                    " the cluster capable of holding this edge"
                );
            }

            this.augmentingPath(fromNode, toNode, inSwitch, outSwitch, true, initial);
        }
    }

    private void logEssentialLinkUpdates () {

        while (!this.addEdges.isEmpty()) {
            Edge link = this.addEdges.removeLast();
            InfraNode fromNode = link.getFromNode();
            int clsId = this.getClusterId(fromNode, link.getToNode());
            NetworkSwitch swt = this.clusters.get(clsId).get(link.getSwtOffset());
            int swtId = swt.getIndex();

            if (!link.isInitial()) {
                this.logIncrementAlterations(swtId, fromNode);

            }
        }
    }

    /**
     * Returns the augmenting path between the two switches
    */
    private void augmentingPath (
        InfraNode fromNode, InfraNode toNode, NetworkSwitch cSwt, NetworkSwitch nSwt, boolean chkOutput, boolean initial
    ) {
        AvailablePorts avPorts = cSwt.getAvailablePorts(fromNode.getNetId(), toNode.getNetId());
        Edge link = new Edge(fromNode, toNode, fromNode.isDownwardEdge(toNode), initial, cSwt.getOffset());
        this.addEdges.addLast(link);

        InfraNode newFromNode = (
        	!chkOutput ? fromNode :
        	this.getInfraNode(cSwt.getConnectedInputNodeId(toNode.getNetId()))
        );
        InfraNode newToNode = (
        	chkOutput ? toNode :
    		this.getInfraNode(cSwt.getConnectedOutputNodeId(fromNode.getNetId()))
		);

        if (fromNode.isDownwardEdge(toNode)) {
        	cSwt.updateChild(fromNode.getNetId(), toNode.getNetId());
            if (fromNode.getId() > toNode.getId()) {
                fromNode.setLeftChildSwitchOffset(cSwt.getOffset());

            } else {
                fromNode.setRightChildSwitchOffset(cSwt.getOffset());

            }

        } else {
        	cSwt.updateParent(fromNode.getNetId(), toNode.getNetId());
        	fromNode.setParentSwitchOffset(cSwt.getOffset());

        }

        int swtId = cSwt.getIndex();
        this.logIncrementActivePorts(swtId);

        if (chkOutput && !avPorts.availableOutput()) {
            swtId = nSwt.getIndex();
            this.logDecrementActivePorts(swtId);
            this.augmentingPath(newFromNode, newToNode, nSwt, cSwt, !chkOutput, initial);

        } else if (!chkOutput && !avPorts.availableInput()) {
            swtId = nSwt.getIndex();
            this.logDecrementActivePorts(swtId);
            this.augmentingPath(newFromNode, newToNode, nSwt, cSwt, !chkOutput, initial);

        }

    }

    private void pushRmvEdge (InfraNode fromNode, InfraNode toNode, boolean downward) {
        if (!this.isValidNode(fromNode) || !this.isValidNode(toNode)) {
            if (downward && fromNode.getId() == this.getNumNodes()) {
                fromNode.removeLeftChild();

            } else if (!downward && toNode.getId() == this.getNumNodes()) {
                fromNode.removeParent();

            }

            return;

        }

        int swtOffset = fromNode.getSwtOffset(toNode);
        if (downward && fromNode.getId() > toNode.getId()) {
            fromNode.removeLeftChild();

        } else if (downward) {
            fromNode.removeRightChild();

        } else {
            fromNode.removeParent();

        }

        Edge edge = new Edge(fromNode, toNode, downward, false, swtOffset);
        this.rmvEdges.push(edge);

    }
    /* End of Setters

    /* Auxiliary Functions */
    /**
     * Retrieves the position for the cluster of type 2. Given by the between the
     * max(clsId1, clsId2) and the sum of terms belonging  to the arithmetic progression
     * AP(n) = ((a0 + an) * n) / 2, such that a0 is the number of clusters type 1 - 1,
     * an the number of clusters type 1 - min(clsId1, clsId2) and n the min(clsId1, clsId2)
     * @param clsId1    the cluster type 1 id for the first node
     * @param clsId2    the cluster type 1 id for the second node
     * @return          the position of the cluster type 2 in the arithmetic progression
     */
    protected int unionPos (int clsId1, int clsId2) {
        /*
            AP(n) = ((a0 + an) * n) / 2
            a0 = NUM_CLUSTERS - 1
            an = NUM_CLUSTER - 1 - clsId1 + 1
            n = clsId1
        */

        if (clsId1 > clsId2) {
            int aux = clsId1;
            clsId1 = clsId2;
            clsId2 = aux;
        }

        int apSum = (
            clsId1 != 0 ?
            ((2 * this.numClustersType1 - 1 - clsId1) * (clsId1)) / 2 :
            0
        );
        return apSum + clsId2 - clsId1 - 1;
    }

    /**
     * This method traverses the nodes that still have messages pending routing and locks them
     * and their remaining routing path. If at least one of this nodes wouldn't be able
     * to finish it's routing the simulation must be ended with an error.
     */
    private void lockRoutingNodes () {
        while (!this.routingNodes.isEmpty()) {
            RoutingInfoMessage routMsg = this.routingNodes.poll();
            int nodeId = routMsg.getNodeId();
            InfraNode node = this.getInfraNode(nodeId);
            InfraNode dstNode = this.getInfraNode(routMsg.getDst());

            if (!this.allowRouting(node, dstNode, routMsg)) {
                Tools.fatalError("Nodes that were supposed to rout are already occupied");

            }
        }

    }

    /**
     * This method locks the routing nodes and then, for every node with a message, if it is
     * possible, performs the rotation specified by the getRotationToPerformed, and if it is not
     * tries to rout the message 2 times. If neither step is possible, due to involved nodes being
     * locked or other issues, the node is not allowed to act in this round.
     */
    private void updateConn () {
        this.lockRoutingNodes();

        while (!this.nodesWithMsg.isEmpty()) {
            HasMessage hasmsg = this.nodesWithMsg.poll();
            int nodeId = hasmsg.getCurrId();

            InfraNode node = this.getInfraNode(nodeId);
            InfraNode dstNode = this.getInfraNode(hasmsg.getDst());

            Rotation op = this.getRotationToPerform(node, dstNode);

            switch (op) {
                case NULL:
                    this.allowRouting(node, dstNode, 1);
                    break;

                case SEMI_ZIGZIGLEFT_BOTTOMUP:
                case SEMI_ZIGZIGRIGHT_BOTTOMUP:
                    if (this.zigZigBottomUp(node)) {
                        System.out.println("semiZigZigBottomUp");
                        this.logIncrementActiveRequests();

                        InfraNode rfrshNode = this.getInfraNode(nodeId);
                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);

                        this.configureRoutingMessage(rfrshNode, nxtNode, new RoutingInfoMessage(1));

                    } else {
                        this.allowRouting(node, dstNode, 2);

                    }

                    break;

                case SEMI_ZIGZAGLEFT_BOTTOMUP:
                case SEMI_ZIGZAGRIGHT_BOTTOMUP:
                    if (this.zigZagBottomUp(node)) {
                        System.out.println("semiZigZagBottomUp");
                        this.logIncrementActiveRequests();

                    } else {
                        this.allowRouting(node, dstNode, 2);

                    }

                    break;

                case SEMI_ZIGZIGLEFT_TOPDOWN:
                    if (this.zigZigLeftTopDown(node)) {
                        System.out.println("semiZigZigLeftTopDown");
                        this.logIncrementActiveRequests();

                        InfraNode rfrshNode = this.getInfraNode(nodeId);
                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);

                        this.configureRoutingMessage(rfrshNode, nxtNode, new RoutingInfoMessage(2));

                    } else {
                        this.allowRouting(node, dstNode, 2);

                    }

                    break;

                case SEMI_ZIGZAGLEFT_TOPDOWN:
                    if (this.zigZagLeftTopDown(node)) {
                        System.out.println("semiZigZagLeftTopDown");
                        this.logIncrementActiveRequests();

                        InfraNode rfrshNode = this.getInfraNode(nodeId);
                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);

                        if (nxtNode == rfrshNode.getParent()) {
                            this.configureRoutingMessage(
                                rfrshNode, nxtNode, new RoutingInfoMessage(3)
                            );

                        } else {
                            this.configureRoutingMessage(
                                rfrshNode, nxtNode, new RoutingInfoMessage(1)
                            );

                        }

                    } else {
                        this.allowRouting(node, dstNode, 2);

                    }

                    break;

                case SEMI_ZIGZIGRIGHT_TOPDOWN:
                    if (this.zigZigRightTopDown(node)) {
                        System.out.println("semiZigZigRightTopDown");
                        this.logIncrementActiveRequests();

                        InfraNode rfrshNode = this.getInfraNode(nodeId);
                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);

                        this.configureRoutingMessage(rfrshNode, nxtNode, new RoutingInfoMessage(2));

                    } else {
                        this.allowRouting(node, dstNode, 2);

                    }

                    break;

                case SEMI_ZIGZAGRIGHT_TOPDOWN:
                    if (this.zigZagRightTopDown(node)) {
                        System.out.println("semiZigZagRightTopDown");
                        this.logIncrementActiveRequests();

                        InfraNode rfrshNode = this.getInfraNode(nodeId);
                        InfraNode nxtNode = rfrshNode.getRoutingNode(dstNode);

                        if (nxtNode == rfrshNode.getParent()) {
                            this.configureRoutingMessage(
                                rfrshNode, nxtNode, new RoutingInfoMessage(3)
                            );

                        } else {
                            this.configureRoutingMessage(
                                rfrshNode, nxtNode, new RoutingInfoMessage(1)
                            );

                        }

                    } else {
                        this.allowRouting(node, dstNode, 2);

                    }

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Creates a RoutingInfoMessage and send it to the NetworkNode equivalent to node
     * if the nodes in the path are available
     * @param node          node with the message
     * @param dstNode       message destination node
     * @param routingTimes  number of times the message should be routed before next step
     * @return              true if message is allowed to rout and false if it isn't
     */
    private boolean allowRouting (InfraNode node, InfraNode dstNode, int routingTimes) {
        return this.allowRouting(node, dstNode, new RoutingInfoMessage(routingTimes));
    }

    /**
     * This method checks if the nodes in the path of length routMsg.getRoutingTimes of the
     * RoutingInfoMessage, are available, if they are, send it to the equivalent NetworkNode and
     * return true, if they are not return false
     *
     * @param node          node with the message
     * @param dstNode       message destination node
     * @param routMsg       the RoutingInfoMessage
     * @return              true if message is allowed to rout and false if it isn't
     */
    private boolean allowRouting (InfraNode node, InfraNode dstNode, RoutingInfoMessage routMsg) {
        ArrayList<InfraNode> routNodes = new ArrayList<>();
        InfraNode currNode = node;

        routNodes.add(currNode);
        for (int i = 1; i <= routMsg.getRoutingTimes(); i++) {
            InfraNode nxtNode = currNode.getRoutingNode(dstNode);
            if (nxtNode.getId() == -1 || nxtNode.getId() == currNode.getId()) {
                break;

            }

            routNodes.add(nxtNode);
            currNode = nxtNode;
        }

        if (this.areAvailableNodes(routNodes.toArray(new InfraNode[0]))) {
            this.logIncrementActiveRequests();
            this.configureRoutingMessage(node, node.getRoutingNode(dstNode), routMsg);

            return true;
        }

        return false;
    }

        /**
     * Getter for the rotation a node should perfomed to rout a message to the destination node.
     * if the message is at one hop away to it's destination or to the LCA between the src node
     * and the dst node the message is simply routed one time. Else, it returns the appropriated
     * rotation based on the direction the message needs to be routed and the network topology
     * surrounding the involved nodes.
     * @param x         InfraNode with the message
     * @param dstNode   destination InfraNode
     * @return          the decided rotation
     */
    protected Rotation getRotationToPerform (InfraNode x, InfraNode dstNode) {
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
                return Rotation.SEMI_ZIGZIGLEFT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getRightChild()) && x == y.getRightChild() &&
                this.isValidNode(z.getRightChild()) && y == z.getRightChild()
            ) {
                return Rotation.SEMI_ZIGZIGRIGHT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getRightChild()) && x == y.getRightChild() &&
                this.isValidNode(z.getLeftChild()) && y == z.getLeftChild()
            ) {
                return Rotation.SEMI_ZIGZAGLEFT_BOTTOMUP;

            } else if (
                this.isValidNode(y.getLeftChild()) && x == y.getLeftChild() &&
                this.isValidNode(z.getRightChild()) && y == z.getRightChild()
            ) {
                return Rotation.SEMI_ZIGZAGRIGHT_BOTTOMUP;

            } else {
                Tools.fatalError("Network topology for BottomUp not expected");

            }

        /* Top-Down - LEFT - BEGIN */
        } else if (direction == Direction.LEFT) {
            InfraNode y = x.getRoutingNode(dstNode);
            InfraNode z = y.getRoutingNode(dstNode);

            if (x.getLeftChild() == y && y.getLeftChild() == z) {
                return Rotation.SEMI_ZIGZIGLEFT_TOPDOWN;

            } else if (x.getLeftChild() == y && y.getRightChild() == z) {
                return Rotation.SEMI_ZIGZAGLEFT_TOPDOWN;

            } else {
                Tools.fatalError("Network topology for Left TopDown not expected");

            }

        /* Top-Down - RIGHT - BEGIN */
        } else if (direction == Direction.RIGHT) {
            InfraNode y = x.getRoutingNode(dstNode);
            InfraNode z = y.getRoutingNode(dstNode);

            if (x.getRightChild() == y && y.getRightChild() == z) {
                return Rotation.SEMI_ZIGZIGRIGHT_TOPDOWN;

            } else if (x.getRightChild() == y && y.getLeftChild() == z) {
                return Rotation.SEMI_ZIGZAGRIGHT_TOPDOWN;

            } else {
                Tools.fatalError("Network topology for Right TopDown not expected");

            }
        }

        Tools.fatalError("Unexpected rotation");

        return null;
    }

    /**
     * Retrieve the equivalent NetworkNode, inform the next node in the path of the message
     * to the routMsg and send the RoutingInfoMessage to it.
     * @param node      InfraNode hodler of the message
     * @param nxtNode   next node in the message path
     * @param routMsg   the RoutingInfoMessage
     */
    private void configureRoutingMessage (
        InfraNode node, InfraNode nxtNode, RoutingInfoMessage routMsg
    ) {
        NetworkNode netNode = this.getNetNode(node);
        routMsg.setRoutNodeId(nxtNode.getNetId());

        this.sendDirect(routMsg, netNode);
    }

    /**
     * This method handles the message a NetworkController receives. If it is a OpticalNetMessage,
     * it means that this message has reached it's destination, so the number of completed
     * messages is incremented, the LoggerLayer reports this message information and the weigth
     * in the path between the src and destination node is updated. If it is a NewMessage the
     * number of received messages is incremented. If it is a HasMessage, the sender node is
     * marked as a nodeWithMessage for the controllerStep. If it is a RoutingInfoMessage, the
     * sender node is marked as a routerNode.
     */
    @Override
    public void handleMessages (Inbox inbox) {
        while (inbox.hasNext()) {
            Message msg = inbox.next();

            if (msg instanceof NewMessage) {
                this.rcvMsgs++;

            } else if (msg instanceof HasMessage) {
                HasMessage hasmsg = (HasMessage) msg;
                this.nodesWithMsg.add(hasmsg);

            } else if (msg instanceof RoutingInfoMessage) {
                RoutingInfoMessage routmsg = (RoutingInfoMessage) msg;
                this.routingNodes.add(routmsg);

            } else if (msg instanceof OpticalNetMessage) {
                OpticalNetMessage optmsg = (OpticalNetMessage) msg;
                this.logIncrementCompletedRequests();
                this.logMessageRouting(optmsg.getRouting());

                this.cmpMsgs++;
                this.seq = true;
            }

        }
    }

    /**
     * Debug method used to print the network topology
     */
    public void debugInfraTree () {
        this.debugInfraTree(this.tree.get(this.numNodes));
    }

    /**
     * Recursive debug method to print the network tree in pre-order traversal
     * @param node  current InfraNode
     */
    public void debugInfraTree (InfraNode node) {
        if (node.getId() == -1)
            return;

        node.debugNode();

        this.debugInfraTree(node.getLeftChild());
        this.debugInfraTree(node.getRightChild());
    }

    /* End of Auxiliary Functions */

    /**
     * Control function that ensures the Infrasctructure stored in the NetworkController
     * matches the one represented by the NetworkNode connections
     */
    private void checkRoundConfiguration () {
        if (!this.validTree()) {
            Tools.fatalError("Invalid infra tree");
        }

        for (int i = 0; i < this.numNodes; i++)  {
            if (!this.equivalentNodes(i)) {
                Tools.fatalError(
                    "InfraNode: " + i + " not equivalent to its network correspondent"
                );

            }
        }
    }

    /**
     * Method with the controller timeslot operations. Checks the network topology
     * and then informs the nodes allowed to perform rotations or routings this round.
     */
    @Override
    public void controllerStep () {
        this.checkRoundConfiguration();

        for (int i = 0; i <= this.numNodes; i++)
            this.usedNodes.set(i, false);

        this.updateConn();
        this.updateLinks();

        int missingMessages = this.rcvMsgs - this.cmpMsgs;
        System.out.println(
            "Round " + this.getCurrentRound() +
            " Messages: " + this.rcvMsgs +
            " Missing Messages: " + missingMessages
        );
    }

    /**
     * This method confirms that the child and parent nodes of a given InfraNode are the
     * equivalent same to the child and parent nodes of it's respective NetworkNode
     * @param nodeId    the id of the nodes in the controller array
     * @return          true if they are equivalent, false if they are not
     */
    private boolean equivalentNodes (int nodeId) {
        InfraNode infraNode = this.tree.get(nodeId);
        NetworkNode netNode = this.netNodes.get(nodeId);

        boolean flag = infraNode.getId() == netNode.getId() - 1;
        flag &= (
                infraNode.getLeftChildId() != -1 ?
                infraNode.getLeftChildId() == netNode.getLeftChildId() - 1 :
                netNode.getLeftChildId() == -1
        );
        flag &= (
                infraNode.getRightChildId() != -1 ?
                infraNode.getRightChildId() == netNode.getRightChildId() - 1 :
                netNode.getRightChildId() == -1
        );
        flag &= (
                (
                    infraNode.getParentId() != -1 &&
                    infraNode.getParentId() != this.numNodes
                ) ?
                infraNode.getParentId() == netNode.getParentId() - 1 :
                netNode.getParentId() == -1
        );

        if (!flag) {
            infraNode.debugNode();
            netNode.debugNode();
        }

        return flag;
    }

    /**
     * This function ensures that the represented tree topology is of a Binary Search Tree
     */
    private boolean validTree () {
        InfraNode rootNode = this.getInfraNode(this.numNodes + 1);
        return this.validSubtree(rootNode.getLeftChild(), 0, this.numNodes - 1);
    }

    /**
     * Returns true if all the nodes inside a ancestor node subtree are inside the range
     * minId and maxId of the node. Should be re-formulated to reduce number of redundant calls.
     * @param x     InfraNode currentNode
     * @param min   Minimum id of the current ancestor
     * @param max   Maximum id of the current ancestor
     * @return      true if all the nodes inside a ancestor node subtree are inside the range
     */
    private boolean validSubtree (InfraNode x, int min, int max) {
        if (x.getId() == -1) {
            return true;

        }

        if (x.getId() < min || x.getId() > max) {
            x.debugNode();
            return false;

        }

        return (
            this.validSubtree(x.getLeftChild(), min, x.getId() - 1) &&
            this.validSubtree(x.getRightChild(), x.getId() + 1, max)
        );
    }

    @Override
    public void draw (Graphics g, PositionTransformation pt, boolean highlight) { }

}
