package projects.bstOpticalNet.nodes.models;

import sinalgo.tools.Tools;

import java.util.Map;
import java.util.HashMap;
/**
 * InfraNodes are used by the NetworkController to represent the current
 * network topology. They store the left and right child and it's parent,
 * as well as their subtree min and max id.
 */
public class InfraNode implements Comparable<InfraNode> {

    /* Attributes */
    private InfraNode parent = null;
    private int pSwtOffset = -1;
    private InfraNode leftChild = null;
    private int lcSwtOffset = -1;
    private InfraNode rightChild = null;
    private int rcSwtOffset = -1;
    
    public Map<Integer, InfraNode> heuristicNeighbors = new HashMap<>();

    private int minId = -1;
    private int maxId = -1;

    private long weight = 0;

    private int id = 0;
    private static int ID = 0;
    /* End of Attributes */

    /* Constructors */

    /**
     * Create a InfraNode unitialized, without children nor parents.
     */
    public InfraNode () {
        this.id = ID++;
        this.parent = new InfraNode(-1);
        this.pSwtOffset = -1;
        this.leftChild = new InfraNode(-1);
        this.lcSwtOffset = -1;
        this.rightChild = new InfraNode(-1);
        this.rcSwtOffset = -1;
        this.minId = this.id;
        this.maxId = this.id;
    }

    /**
     * Create a dummy node, that should be deemed invalid during the simulation.
     * @param dummy variable to represent dummy id, should be -1
     */
    public InfraNode (int dummy) {
        this.id = dummy;
        this.minId = this.id;
        this.maxId = this.id;
    }

    /**
     * Create a new InfraNode already informing it's parent and children, already initializing
     * it's subtree
     * @param parent        This node parent or a dummy node
     * @param leftChild     This node leftChild or a dummy node
     * @param rightChild    This node rightChild or a dummy node
     */
    public InfraNode (InfraNode parent, InfraNode leftChild, InfraNode rightChild) {
        this.id = ID++;
        this.parent = parent;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.minId = this.leftChild.getId() == -1 ? this.getId() : this.leftChild.getMinId();
        this.maxId = this.rightChild.getId() == -1 ? this.getId() : this.rightChild.getMaxId();
    }
    /* End of Constructors */

    /* Getters */

    /**
     * Getter for this node id in the InfraTree.
     * @return This node id
     */
    public int getId () {
        return this.id;

    }

    /**
     * Getter for the id of this node equivalent NetworkNode
     * @return The equivalent NetworkNode id
     */
    public int getNetId () {
        return this.id + 1;

    }

    /**
     * Getter for this node parent
     * @return This node parent
     */
    public InfraNode getParent () {
        return this.parent;
    }

    /**
     * Getter for this node's parent's id
     * @return This node parent's id
     */
    public int getParentId () {
        return this.parent.getId();
    }

    /**
     * Getter for this node parent switch index
     * @return the switch Offset connected to this node parent
     */
    public int getParentSwitchOffset () {
        return this.pSwtOffset;
    }

    /**
     * Getter for this node left child
     * @return This node left child
     */
    public InfraNode getLeftChild () {
        return this.leftChild;
    }

    /**
     * Getter for this node's left child's id
     * @return This node left child's id
     */
    public int getLeftChildId () {
        return this.leftChild.getId();
    }

    /**
     * Getter for this node left child switch index
     * @return the switch id connected to this node left child
     */
    public int getLeftChildSwitchOffset () {
        return this.lcSwtOffset;
    }

    /**
     * Getter for this node right child
     * @return This node right child
     */
    public InfraNode getRightChild () {
        return this.rightChild;
    }

    /**
     * Getter for this node's right child's id
     * @return This node right child's id
     */
    public int getRightChildId () {
        return this.rightChild.getId();
    }

    /**
     * Getter for this node right child switch index
     * @return the switch id connected to this node right child
     */
    public int getRightChildSwitchOffset () {
        return this.rcSwtOffset;
    }

    /**
     * Gets the switch id for the switch connected to toNode
     * @param toNode  this edge endpoint
     * @return        the switch id
     */
    public int getSwtOffset (InfraNode toNode) {
        if (this.getParentId() == toNode.getId()) {
            return this.getParentSwitchOffset();

        } else if (this.getLeftChildId() == toNode.getId()) {
            return this.getLeftChildSwitchOffset();

        } else if (this.getRightChildId() == toNode.getId()) {
            return this.getRightChildSwitchOffset();

        }

        Tools.fatalError("ToNode is neither parent nor child from this node on get");

        return -1;
    }

    /**
     * Getter for this node's subtree minimum id
     * @return This node's subtree minimum id
     */
    public int getMinId () {
        return this.minId;
    }

    /**
     * Getter for this node's subtree maximum id
     * @return This node's subtree maximum id
     */
    public int getMaxId () {
        return this.maxId;
    }

    /**
     * Getter for this node weight. Obsolete with weight-tree.
     * @return This node's weight
     */
    public long getWeight () {
        return (this.getId() == -1 ? 0 : this.weight);
    }

    /* End of Getters */

    /* Setters */
    /**
     * Sets this node new parent and resets the respective child from this
     * node old parent.
     * @param parent This node new parent
     */
    public void setParent (InfraNode parent) {
        if (this.getId() == -1)
            return;

        this.parent = parent;
    }

    public void removeParent () {
        this.parent = new InfraNode(-1);
        this.pSwtOffset = -1;
    }

    /**
     * Sets the switch id for the switch connected to this node's parent
     * @param swtOffset the switch Offset
     */
    public void setParentSwitchOffset (int swtOffset) {
        this.pSwtOffset = swtOffset;
    }

    /**
     * Set the left child of this node as child and set this child parent as this.
     * After that update the minimum id of the subtree.
     * @param child new child node
     */
    public void setLeftChild (InfraNode child) {
        if (this.getId() != -1) {
            this.leftChild = child;

            this.updateMin(child);

        }
    }

    public void removeLeftChild () {
        this.leftChild = new InfraNode(-1);
        this.minId = this.getId();
        this.lcSwtOffset = -1;
    }

    /**
     * Sets the switch id for the switch connected to this node's left chiuld
     * @param swtOffset the switch Offset
     */
    public void setLeftChildSwitchOffset (int swtOffset) {
        this.lcSwtOffset = swtOffset;
    }

    /**
     * Set the right child of this node as child and set this child parent as this.
     * After that update the maximum id of the subtree.
     * @param child new child node
     */
    public void setRightChild (InfraNode child) {
        if (this.getId() != -1) {
            this.rightChild = child;

            this.updateMax(child);
        }
    }

    public void removeRightChild () {
        this.rightChild = new InfraNode(-1);
        this.maxId = this.getId();
        this.rcSwtOffset = -1;
    }

    /**
     * Sets the switch id for the switch connected to this node's right child
     * @param swtOffset the switch Offset
     */
    public void setRightChildSwitchOffset (int swtOffset) {
        this.rcSwtOffset = swtOffset;
    }

    /**
     * Update the switch id for the switch connected to toNode
     * @param toNode  this edge endpoint
     * @param swtOffset   the switch id
     */
    public void updateSwtOffset (InfraNode toNode, int swtOffset) {
        if (this.getParentId() == toNode.getId()) {
            this.setParentSwitchOffset(swtOffset);

            return;

        } else if (this.getLeftChildId() == toNode.getId()) {
            this.setLeftChildSwitchOffset(swtOffset);

            return;

        } else if (this.getRightChildId() == toNode.getId()) {
            this.setRightChildSwitchOffset(swtOffset);

            return;

        }

        Tools.fatalError("ToNode is neither parent nor child from this node on update");
    }

    /**
     * Remove its node child equals to rstNode, removing its subtree for the
     * respective left or right side.
     * @param rstNode the old child node.
     */
    public void resetChild (InfraNode rstNode) {
        if (this.getId() != -1) {
            if (this.leftChild.getId() != -1 && this.leftChild == rstNode) {
                this.updateMin(new InfraNode(-1));
                this.leftChild = new InfraNode(-1);

            } else if (this.rightChild.getId() != -1 && this.rightChild == rstNode) {
                this.updateMax(new InfraNode(-1));
                this.rightChild = new InfraNode(-1);

            }

        }
    }

    /**
     * Reset this node parent. Setting it as a dummy node.
     * @param rstNode this node old parent.
     */
    public void resetParent (InfraNode rstNode) {
        if (this.getId() == -1) {
            return;

        }

        if (this.parent.getId() != -1 && this.parent.getId() == rstNode.getId()) {
            this.parent = new InfraNode(-1);

        }
    }

    /**
     * Update minimum id from this node subtree. Setting it as this node id if the new child
     * is a dummy node or as the child node minimum id in their subtree.
     * @param child This node new child
     */
    public void updateMin (InfraNode child) {
        if (child.getId() == -1) {
            this.minId = this.getId();

        } else {
            this.minId = child.getMinId();

        }
    }

    /**
     * Update maximum id from this node subtree. Setting it as this node id if the new child
     * is a dummy node or as the child node maximum id in their subtree.
     * @param child This node new child
     */
    public void updateMax (InfraNode child) {
        if (child.getId() == -1) {
            this.maxId = this.getId();

        } else {
            this.maxId = child.getMaxId();

        }

    }

    /**
     * Update this node weight, called after a rotation. Obsolete with weight-tree.
     * @param weight This node new weight
     */
    public void setWeight (long weight) {
        this.weight = weight;

    }

    /**
     * Increment the weight of this node subtree. Obsolete with weight-tree.
     */
    public void incrementWeight () {
        this.weight++;

    }

    /**
     * Increment the weight of the nodes in the path between this node and the destination
     * node, toNode. It traverse the path from the source node to the root and from the
     * root to the destination node. Effectively adding 2 to every node ancestor to the LCA
     * and adding one to every node ancestor to only one of the source and destination nodes.
     * @param toNode    destination node
     * @param rooted    boolean with true if it already has traversed to the root and false if
     *                  it still hasn't
     */
    public void incrementPathWeight (InfraNode toNode, boolean rooted) {
        this.incrementWeight();
        Direction direction = this.getRoutingDirection(toNode);

        if (!rooted && this.parent.getId() != -1) {
            this.parent.incrementPathWeight(toNode, false);

        } else if (this.getId() == toNode.getId()) {
            return;

        } else if (direction == Direction.RIGHT || direction == Direction.RIGHTROUT) {
            this.rightChild.incrementPathWeight(toNode, true);

        } else if (direction == Direction.LEFT || direction == Direction.LEFTROUT) {
            this.leftChild.incrementPathWeight(toNode, true);

        } else {
            Tools.fatalError("Incrementing parent weigth after going to root");

        }
    }
    
    public Map<Integer, InfraNode> getHeuristicNeighbors(){ 
    	return this.heuristicNeighbors;
    	
    }
    

    /**
     * Getter for the next node in the path between this node and the destination node.
     * @param toNode    the destination node
     * @return          the next node in the path.
     */
    public InfraNode getRoutingNode (InfraNode toNode) {
    	return this.getRoutingNode(this.getRoutingDirection(toNode));
    	
    }

    /**
     * Getter for which node is next in the specified direction.
     * Left child for the LEFT and LEFTROUT direction
     * Right child for the RIGHT and RIGHTROUT direction
     * Parent for the PARENT and PARENTROUT direction
     * This for the NULL direction
     * @param direction     the routing direction.
     * @return              the next node in the direction.
     */
    public InfraNode getRoutingNode (Direction direction) {
        if (direction == Direction.NULL) {
            return this;

        } else if (direction == Direction.RIGHT || direction == Direction.RIGHTROUT) {
            return this.rightChild;

        } else if (direction == Direction.LEFT || direction == Direction.LEFTROUT) {
            return this.leftChild;

        } else {
            return this.parent;

        }
    }

    /**
     * Getter for the routing direction the next node in the path between this and the
     * destination node is.
     * @param toNode    destination node
     * @return          the routing the direction the next node in the path is
     */
    public Direction getRoutingDirection (InfraNode toNode) {
        if (toNode.getId() == -1) {
            System.out.println("Invalid toNode");
            Tools.fatalError("Invalid toNode");

        }

        if (this == toNode) {
            return Direction.NULL;

        } else if (this.getLeftChild() == toNode) {
            return Direction.LEFTROUT;

        } else if (this.getRightChild() == toNode) {
            return Direction.RIGHTROUT;

        } else if (this.getParent() == toNode) {
            return Direction.PARENTROUT;

        } else if (this.getId() < toNode.getId() && toNode.getId() <= this.maxId) {
            return Direction.RIGHT;

        } else if (this.minId <= toNode.getId() && toNode.getId() < this.getId()) {
            return Direction.LEFT;

        } else {
            return Direction.PARENT;

        }
    }

    public boolean isDownwardEdge (InfraNode toNode) {
        if (toNode.getId() == -1) {
            System.out.println("Invalid toNode");
            Tools.fatalError("Invalid toNode");

        }

        if (this.getLeftChild() == toNode) {
            return true;

        } else if (this.getRightChild() == toNode) {
            return true;

        } else if (this.getParent() == toNode) {
            return false;

        }

        this.debugNode();
        toNode.debugNode();

        Tools.fatalError("Not a real edge");
        return false;
    }

    /* End of Setters */

    /* Auxiliary Functions */

    /**
     * Method to print this InfraNode informations, such as his left and right child ids,
     * his parent id, and his minimum and maximum id of his subtree.
     */
    public void debugNode () {
        if (this.id == -1) {
            System.out.println("Dummy Node");

        } else {
            System.out.println(
                "INFRAID: " + this.getId()
                + " lftID: " + this.getLeftChildId() + " swtIdx: " + this.getLeftChildSwitchOffset()
                + " rgtID: " + this.getRightChildId() + " swtIdx: " + this.getRightChildSwitchOffset()
                + " parentId: " + this.getParentId() + " swtIdx: " + this.getParentSwitchOffset()
                + " leftSUB: " + this.getMinId() + " rightSUB: " + this.getMaxId()
            );

        }
    }

    /**
     * Comparator between two InfraNodes, returning less than if this node is a dummy node,
     * bigger than if the other node is a dummy node. And comparing their id if both of them
     * are valid nodes.
     */
    @Override
    public int compareTo (InfraNode othNode) {
        if (this.getId() == -1) {
            return -1;

        } else if (othNode.getId() == -1) {
            return 1;

        }

        return Integer.compare(this.getId(), othNode.getId());
    }
    /* End of Auxiliary Functions */
}
