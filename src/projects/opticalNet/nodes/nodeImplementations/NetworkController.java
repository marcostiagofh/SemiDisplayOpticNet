package projects.opticalNet.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import projects.defaultProject.DataCollection;
import projects.opticalNet.nodes.infrastructureImplementations.InfraNode;
import projects.opticalNet.nodes.infrastructureImplementations.SynchronizerLayer;
import projects.opticalNet.nodes.messages.ConnectNodesMessage;

import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.Tools;

public abstract class NetworkController extends SynchronizerLayer {

    /* Attributes */
	private ArrayList<Boolean> usedNodes;

	protected ArrayList<InfraNode> tree;
    protected ArrayList<NetworkSwitch> switches;
    protected ArrayList<NetworkNode> netNodes;

    protected int numNodes = 0;
    protected int switchSize = 0;
    protected int numSwitches = 0;
    protected int numClusters = 0;
    protected int numUnionClusters = 0;
    protected int clusterSize;

    protected int rcvMsgs = 0;
    protected int rotations = 0;
    protected DataCollection data;
    protected ArrayList<Integer> remainingMessage;

    public int sinceCompleted = 0;

    private static final int SIZE_CLUSTER_TYPE1 = 4;
    private static final int SIZE_CLUSTER_TYPE2 = 4;

    /* End of Attributes */

    public NetworkController (int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, DataCollection data) {
        this(numNodes, switchSize, netNodes, data, new ArrayList<Integer>());
    }

    public NetworkController (
        int numNodes, int switchSize, ArrayList<NetworkNode> netNodes, DataCollection data, ArrayList<Integer> edgeList
    ) {

        this.numNodes = numNodes;
    	this.switchSize = switchSize;
    	this.tree = new ArrayList<>();
    	this.switches = new ArrayList<>();
    	this.netNodes = netNodes;

    	this.data = data;
    	this.remainingMessage = new ArrayList<>();

    	this.clusterSize = this.switchSize / 2;
        this.numClusters = (this.numNodes - this.clusterSize + 1) / this.clusterSize + 1;
        this.numUnionClusters = (
                this.numClusters > 1 ?
                this.unionPos(this.numClusters - 2, this.numClusters - 1) + 1 :
                0
        );
        this.numSwitches = (
                this.numClusters * SIZE_CLUSTER_TYPE1 +
                this.numUnionClusters * SIZE_CLUSTER_TYPE2
        );

        for (int clsId = 0; clsId < this.numClusters; clsId++) {
            for (int i = 0; i < 4; i++) {
                NetworkSwitch swt = new NetworkSwitch(
                    clsId * this.clusterSize + 1, (clsId + 1) * this.clusterSize, this.netNodes
                );
		        swt.finishInitializationWithDefaultModels(true);
                swt.setIndex(this.switches.size() + 1);

                this.switches.add(swt);
            }
        }

        for (int clsId1 = 0; clsId1 < this.numClusters; clsId1++) {
            for (int clsId2 = clsId1 + 1; clsId2 < this.numClusters; clsId2++) {
                NetworkSwitch swt = new NetworkSwitch(
                        clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                        clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                        this.netNodes
                );
		        swt.finishInitializationWithDefaultModels(true);
                swt.setIndex(this.switches.size() + 1);

                this.switches.add(swt);

                NetworkSwitch swt2 = new NetworkSwitch(
                        clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                        clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                        this.netNodes
                );

                swt2.finishInitializationWithDefaultModels(true);
                swt2.setIndex(this.switches.size() + 1);

                this.switches.add(swt2);

                swt2 = new NetworkSwitch(
                        clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                        clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                        this.netNodes
                );

                swt2.finishInitializationWithDefaultModels(true);
                swt2.setIndex(this.switches.size() + 1);

                this.switches.add(swt2);

                swt = new NetworkSwitch(
                        clsId1 * this.clusterSize + 1, (clsId1 + 1) * this.clusterSize,
                        clsId2 * this.clusterSize + 1, (clsId2 + 1) * this.clusterSize,
                        this.netNodes
                );
		        swt.finishInitializationWithDefaultModels(true);
                swt.setIndex(this.switches.size() + 1);

                this.switches.add(swt);
            }
        }

        this.setupTree(edgeList);
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

        boolean leftZigZig = (y.getId() == z.getLeftChild().getId());
        InfraNode c = (leftZigZig) ? y.getRightChild() : y.getLeftChild();

        if (this.areAvailableNodes(w, z, y, c)) {
            this.mapConn(z, c, y, 1);
            this.mapConn(y, z, 2);
            this.mapConn(w, y, 3);

            return true;
        }

        return false;
    }

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

        boolean leftZigZag = (y.getId() == z.getLeftChild().getId());
        InfraNode b = (leftZigZag) ? x.getLeftChild() : x.getRightChild();
        InfraNode c = (leftZigZag) ? x.getRightChild() : x.getLeftChild();

        if (this.areAvailableNodes(w, z, y, x, b, c)) {
            this.mapConn(y, b, x, 1);
            this.mapConn(x, y, 2);
            this.mapConn(z, c, x, 3);
            this.mapConn(x, z, 4);
            this.mapConn(w, x, 5);

            return true;
        }

        return false;
    }

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
        InfraNode c = y.getRightChild();

        if (this.areAvailableNodes(w, z, y, c)) {
            this.mapConn(z, c, y, 1);
            this.mapConn(y, z, 2);
            this.mapConn(w, y, 3);

            return true;
        }

        return false;
    }

    protected boolean zigZigRightTopDown (InfraNode z) {
    	InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode c = y.getLeftChild();

        if (this.areAvailableNodes(w, z, y, c)) {
            this.mapConn(z, c, y, 1);
            this.mapConn(y, z, 2);
            this.mapConn(w, y, 3);

            return true;
        }

        return false;
    }

    protected boolean zigZagLeftTopDown (InfraNode z) {
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

        if (this.areAvailableNodes(w, z, y, x, b, c)) {
            this.mapConn(y, b, x, 1);
            this.mapConn(x, y, 2);
            this.mapConn(z, c, x, 3);
            this.mapConn(x, z, 4);
            this.mapConn(w, x, 5);

            return true;
        }

        return false;
    }

    protected boolean zigZagRightTopDown (InfraNode z) {
    	InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode x = y.getLeftChild();
        InfraNode b = x.getRightChild();
        InfraNode c = x.getLeftChild();

        if (this.areAvailableNodes(w, z, y, x, b, c)) {
            this.mapConn(y, b, x, 1);
            this.mapConn(x, y, 2);
            this.mapConn(z, c, x, 3);
            this.mapConn(x, z, 4);
            this.mapConn(w, x, 5);

            return true;
        }

        return false;
    }

    private boolean areAvailableNodes (InfraNode... nodes) {
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
    public int getNumNodes () {
        return this.numNodes;
    }

    public int getNumSwitches () {
        return this.numSwitches;
    }

    public int getNumClusters () {
        return this.numClusters;
    }

    public int getNumUnionClusters () {
        return this.numUnionClusters;
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
                return this.numClusters + this.unionPos(
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
                this.numClusters * SIZE_CLUSTER_TYPE1 + this.unionPos(
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

        this.getSwitch(swtId).updateSwitch(fromNode.getId() + 1, toNode.getId() + 1, subtreeId);
		this.getSwitch(swtId + 1).updateSwitch(toNode.getId() + 1, fromNode.getId() + 1);

        return;
    }

    private void mapConn (InfraNode fromNode, InfraNode toNode, int priority) {
        this.mapConn(fromNode, toNode, new InfraNode(-1), priority);
    }

    private void mapConn (InfraNode fromNode, InfraNode toNode, InfraNode oldParent, int priority) {
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
	    
	    this.sendConnectNodesMessage(swtId, fromNode.getId() + 1, toNode.getId() + 1, subtreeId, priority);
        this.sendConnectNodesMessage(swtId + 1, toNode.getId() + 1, fromNode.getId() + 1, priority);
    }

    private void sendConnectNodesMessage (int switchId, int from, int to, int priority) {
    	ConnectNodesMessage msg = new ConnectNodesMessage(from, to, priority);
    	this.sendDirect(msg, this.getSwitch(switchId));
    }

    private void sendConnectNodesMessage (int switchId, int from, int to, int subtreeId, int priority) {
    	ConnectNodesMessage msg = new ConnectNodesMessage(from, to, subtreeId, priority);
    	this.sendDirect(msg, this.getSwitch(switchId));
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
            ((2 * this.numClusters - 1 - clsId1) * (clsId1)) / 2 :
            0
        );
        return apSum + clsId2 - clsId1 - 1;
    }

    public abstract int getRotationToPerform (InfraNode x);

    public void updateConn () {
        for (int i = 0; i < this.numNodes; i++) {
        	InfraNode node = this.tree.get(i);
        	int op = this.getRotationToPerform(node);

        	if (op == -1) continue;

            switch (op) {
                case 1:
                case 2:
                		System.out.println("zigZigBottomUp");
                        if (this.zigZigBottomUp(node))
                            this.data.addRotations(1);

                        break;
                case 3:
                case 4:
                		System.out.println("zigZagBottomUp");
                        if (this.zigZagBottomUp(node))
                            this.data.addRotations(1);

                        break;
                case 5:
                		System.out.println("zigZigLeftTopDown");
                        if (this.zigZigLeftTopDown(node))
                            this.data.addRotations(1);

                        break;
                case 6:
                		System.out.println("zigZagLeftTopDown");
                        if (this.zigZagLeftTopDown(node))
                            this.data.addRotations(1);

                        break;
                case 7:
                		System.out.println("zigZigRightTopDown");
                        if (this.zigZigRightTopDown(node))
                            this.data.addRotations(1);

                        break;
                case 8:
                		System.out.println("zigZagRightTopDown");
                        if (this.zigZagRightTopDown(node))
                            this.data.addRotations(1);

                        break;
                case 9:
                    
                default:
                        break;
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

    @Override
    public void controllerStep () {
    	if (!this.validTree()) {
        	Tools.fatalError("Invalid infra tree");
    	}

    	for (int i = 0; i < this.numNodes; i++)  {
    		if (!this.equivalentNodes(i)) {
            	Tools.fatalError("InfraNode: " + i + " not equivalent to its network correspondent");

    		}
    	}

        this.sinceCompleted++;
    	for (int i = 0; i <= this.numNodes; i++)
    		this.usedNodes.set(i, false);

    	this.updateConn();

    	int missingMessages = 0;
    	for (int i = 0; i <= this.numNodes; i++) {
    		if (this.remainingMessage.get(i) >= 1) {
    			// System.out.println(
                //     "Node: " + i + " still has " +
                //     this.remainingMessage.get(i) + " messages on the network"
                // );
    		}
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
    			infraNode.getParent().getId() != -1 && infraNode.getParent().getId() != 128 ?
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
    	if (x.getId() != -1) {
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
    public void draw (Graphics g, PositionTransformation pt, boolean highlight) {
        String text = "" + ID;
        // draw the node as a circle with the text inside
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 12, Color.YELLOW);
    }

    public void renderTopology (int width, int height) {
		// set network nodes position
		double x_space = width / 4.0;
		double y_space = height / (double) (this.numNodes + 1);
		for (int i = 0; i < this.numNodes; ++i) {
			NetworkNode n = this.netNodes.get(i);
			n.setPosition(x_space, y_space * (i+1), 0);
		}

		//set network switches position
		double unit = height / (double)((7 * this.numSwitches) + 1);
		double switch_height = 5 * unit;

		for (int i = 0; i < this.numSwitches; ++i) {
			NetworkSwitch n = this.switches.get(i);
			n.setPosition(3 * x_space, 2 * unit + switch_height/2 + (i * 7 * unit), 0);
			n.setSwitchDimension((int)switch_height/3, (int)switch_height);
		}

		// set controller node position
		this.setPosition(4 * x_space, height / 2, 0);
	}

}
