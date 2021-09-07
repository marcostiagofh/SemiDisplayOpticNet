package projects.opticalNet.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

import projects.opticalNet.nodes.infrastructureImplementations.InfraNode;
import projects.opticalNet.nodes.infrastructureImplementations.AltNetwork;
import projects.opticalNet.nodes.infrastructureImplementations.SynchronizerLayer;
import projects.opticalNet.nodes.messages.ConnectNodesMessage;

import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.tools.Tools;

public abstract class NetworkController extends SynchronizerLayer {

    /* Attributes */

    protected ArrayList<InfraNode> tree;
    protected ArrayList<NetworkSwitch> switches;
    protected ArrayList<NetworkNode> netNodes;

    protected int numNodes = 0;
    protected int switchSize = 0;
    protected int numSwitches = 0;
    protected int numClusters = 0;
    protected int numUnionClusters = 0;
    protected int clusterSize;

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
        for (int i = 0; i <= this.numNodes; i++) {
            this.tree.add(new InfraNode());
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

        w.debugNode();
        z.debugNode();
        y.debugNode();
        x.debugNode();

        boolean leftZigZig = (y.getId() == z.getLeftChild().getId());
        InfraNode c = (leftZigZig) ? y.getRightChild() : y.getLeftChild();

        this.mapConn(z, c, y);
        this.mapConn(y, z);
        this.mapConn(w, y);

        return leftZigZig;
    }

    protected boolean zigZagBottomUp (InfraNode x) {
        /*
                  w               w
                 /               /
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

        w.debugNode();
        z.debugNode();
        y.debugNode();
        x.debugNode();

        this.mapConn(y, b, x);
        this.mapConn(x, y);
        this.mapConn(z, c, x);
        this.mapConn(x, z);
        this.mapConn(w, x);

        return leftZigZag;
    }

    protected void zigZigLeftTopDown (InfraNode z) {
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

        w.debugNode();
        z.debugNode();
        y.debugNode();

        this.mapConn(z, c, y);
        this.mapConn(y, z);
        this.mapConn(w, y);
    }

    protected void zigZigRightTopDown (InfraNode z) {
    	InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode c = y.getLeftChild();

        w.debugNode();
        z.debugNode();
        y.debugNode();

        this.mapConn(z, c, y);
        this.mapConn(y, z);
        this.mapConn(w, y);
    }

    protected void zigZagLeftTopDown (InfraNode z) {
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

        w.debugNode();
        z.debugNode();
        y.debugNode();
        x.debugNode();

        this.mapConn(y, b, x);
        this.mapConn(x, y);
        this.mapConn(z, c, x);
        this.mapConn(x, z);
        this.mapConn(w, x);
    }

    protected void zigZagRightTopDown (InfraNode z) {
    	InfraNode w = z.getParent();
        InfraNode y = z.getRightChild();
        InfraNode x = y.getLeftChild();
        InfraNode b = x.getRightChild();
        InfraNode c = x.getLeftChild();

        w.debugNode();
        z.debugNode();
        y.debugNode();
        x.debugNode();

        this.mapConn(y, b, x);
        this.mapConn(x, y);
        this.mapConn(z, c, x);
        this.mapConn(x, z);
        this.mapConn(w, x);
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

    public ArrayList<AltNetwork> getTreeConfiguration () {
        ArrayList<AltNetwork> ret = new ArrayList<>();

        for (int i = 0; i < this.numNodes; i++) {
            InfraNode node = this.tree.get(i);
            if (node.getLeftChild() != null) {
                InfraNode child = node.getLeftChild();
                ret.add(new AltNetwork(
                    this.getSwitchId(
                        this.tree.get(node.getId()), this.tree.get(child.getId())
                    ), node.getId(), child.getId()
                ));
            }
            if (node.getRightChild() != null) {
                InfraNode child = node.getRightChild();
                ret.add(new AltNetwork(
                    this.getSwitchId(
                        this.tree.get(node.getId()), this.tree.get(child.getId())
                    ), node.getId(), child.getId()
                ));
            }
        }

        return ret;
    }

    public InfraNode getNode (int nodeId) {
        return this.tree.get(nodeId);
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
                we neet to multiply by 4 the numbers of clusters of type 1.
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

    private void mapConn (InfraNode fromNode, InfraNode toNode) {
        this.mapConn(fromNode, toNode, new InfraNode(-1));
    }

    private void mapConn (InfraNode fromNode, InfraNode toNode, InfraNode oldParent) {
        int swtId = this.getSwitchId(fromNode, toNode);
        int subtreeId = fromNode.setChild(toNode, oldParent) + 1;
        System.out.println("From Node: " + fromNode.getId() + " to node: " + toNode.getId() + " in switch: " + swtId);

        if (fromNode.getId() == this.numNodes) {
        	return;
        } else if (toNode.getId() == -1) {
        	return;
        } else if (toNode.getId() == this.numNodes) {
        	Tools.fatalError("Trying to make root node as a child");
        }

        if (swtId >= 8) {
        	System.out.println("UnioPos: " + this.unionPos(
                    this.getClusterId(fromNode), this.getClusterId(toNode)
            ));
            this.switches.get(swtId).debugSwitch();
        }

        this.sendConnectNodesMessage(swtId, fromNode.getId() + 1, toNode.getId() + 1, subtreeId);
        this.sendConnectNodesMessage(swtId + 1, toNode.getId() + 1, fromNode.getId() + 1);
    }

    private void sendConnectNodesMessage (int switchId, int from, int to) {
    	ConnectNodesMessage msg = new ConnectNodesMessage(from, to);
    	this.sendDirect(msg, this.getSwitch(switchId));
    }

    private void sendConnectNodesMessage (int switchId, int from, int to, int subtreeId) {
    	ConnectNodesMessage msg = new ConnectNodesMessage(from, to, subtreeId);
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
                        this.zigZigBottomUp(node);
                        break;
                case 3:
                case 4:
                		System.out.println("zigZagBottomUp");
                        this.zigZagBottomUp(node);
                        break;
                case 5:
                		System.out.println("zigZigLeftTopDown");
                        this.zigZigLeftTopDown(node);
                        break;
                case 6:
                		System.out.println("zigZagLeftTopDown");
                        this.zigZagLeftTopDown(node);
                        break;
                case 7:
                		System.out.println("zigZigRightTopDown");
                        this.zigZigRightTopDown(node);
                        break;
                case 8:
                		System.out.println("zigZagRightTopDown");
                        this.zigZagRightTopDown(node);
                        break;
                default:
                        break;
            }
        }
    }

    public void debugTree () {
        InfraNode root = null;
        for (int i = 0; i < this.numNodes; i++)
            if (this.tree.get(i).getParent().getId() == -1)
                root = this.tree.get(i);

        this.debugTree(root);
    }

    public void debugTree (InfraNode node) {
        if (node.getId() == -1)
            return;

        System.out.println("Node ID: " + node.getId());

        System.out.println("MIN SUBTREE: " + node.getMinId());
        this.debugTree(node.getLeftChild());

        System.out.println("MAX SUBTREE: " + node.getMaxId());
        this.debugTree(node.getRightChild());
    }

    /* End of Auxiliary Functions */

    @Override
    public void controllerStep () {
    	this.updateConn();
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
