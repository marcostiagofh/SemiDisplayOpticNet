package projects.opticalNet.nodes.infrastructureImplementations;

import sinalgo.tools.Tools;

public class InfraNode {

    /* Attributes */
    private InfraNode parent = null;
    private InfraNode leftChild = null;
    private InfraNode rightChild = null;

    private int minId = -1;
    private int maxId = -1;

    private long weight = 0;

    private int id = 0;
    private static int ID = 0;
    /* End of Attributes */

    /* Constructors */
    public InfraNode () {
        this.id = ID++;
        this.parent = new InfraNode(-1);
        this.leftChild = new InfraNode(-1);
        this.rightChild = new InfraNode(-1);
        this.minId = this.id;
        this.maxId = this.id;
    }

    public InfraNode (int dummy) {
        this.id = dummy;
        this.minId = this.id;
        this.maxId = this.id;
    }

    public InfraNode (InfraNode parent, InfraNode leftChild, InfraNode rightChild) {
        this.id = ID++;
        this.parent = parent;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
        this.minId = this.leftChild.getId() == -1 ? this.getId() : this.leftChild.getId();
        this.maxId = this.rightChild.getId() == -1 ? this.getId() : this.rightChild.getId();
    }
    /* End of Constructors */

    /* Getters */
    public int getId () {
        return this.id;
    }

    public InfraNode getParent () {
        return this.parent;
    }

    public InfraNode getLeftChild () {
        return this.leftChild;
    }

    public InfraNode getRightChild () {
        return this.rightChild;
    }

    public int getMinId () {
        return this.minId;
    }

    public int getMaxId () {
        return this.maxId;
    }

    public long getWeight () {
        return this.weight;
    }

    /* End of Getters */

    /* Setters */
    public void setParent (InfraNode parent) {
        if (this.getId() == -1)
            return;

        this.parent.resetChild(this);
        this.parent = parent;
    }

    public int setChild (InfraNode child) {
        if (child.getId() == -1) {
            Tools.fatalError(
                "Trying to add a Dummy node to " + ID + " without specifying its parent"
            );
            return -1;
        } else if (this.getId() > child.getId()) {
            this.getLeftChild().resetParent(this);
            return this.setLeftChild(child);

        } else {
            this.getRightChild().resetParent(this);
            return this.setRightChild(child);

        }
    }

    public int setChild (InfraNode child, InfraNode oldParent) {
        if (child.getId() == -1) {
            if (this.getId() > oldParent.getId())
                return this.setLeftChild(child);
            else
                return this.setRightChild(child);

        } else if (this.getId() > child.getId()) {
            this.getLeftChild().resetParent(this);
            return this.setLeftChild(child);

        } else {
            this.getRightChild().resetParent(this);
            return this.setRightChild(child);

        }
    }

    public int setLeftChild (InfraNode child) {
        if (this.getId() == -1)
            return -1;

        child.setParent(this);
        this.leftChild = child;

        return this.updateMin(child);
    }

    public int setRightChild (InfraNode child) {
        if (this.getId() == -1)
            return -1;

        child.setParent(this);
        this.rightChild = child;

        return this.updateMax(child);
    }

    public void resetChild (InfraNode rstNode) {
        if (this.getId() == -1)
            return;

        if (this.leftChild.getId() != -1 && this.leftChild.getId() == rstNode.getId()) {
            this.updateMin(new InfraNode(-1));
            this.leftChild = new InfraNode(-1);

        } else if (this.rightChild.getId() != -1 && this.rightChild.getId() == rstNode.getId()) {
            this.updateMax(new InfraNode(-1));
            this.rightChild = new InfraNode(-1);

        }
    }

    public void resetParent (InfraNode rstNode) {
        if (this.getId() == -1)
            return;

        if (this.parent.getId() != -1 && this.parent.getId() == rstNode.getId()) {
            this.parent = new InfraNode(-1);

        }
    }

    public int updateMin (InfraNode child) {
        if (child.getId() == -1)
            return this.minId = this.getId();

        return this.minId = child.getMinId();
    }

    public int updateMax (InfraNode child) {
        if (child.getId() == -1)
            return this.maxId = this.getId();

        return this.maxId = child.getMaxId();
    }

    public void setMinId (int minId) {
        this.minId = minId;
    }

    public void setMaxId (int maxId) {
        this.maxId = maxId;
    }

    public void setWeight (long weight) {
        this.weight = weight;
    }

    public void incrementPathWeight (int toId, boolean rooted) {
        this.incrementWeight();

        if (!rooted && this.parent.getId() != -1) {
            this.parent.incrementPathWeight(toId, false);

        } else if (this.getId() == toId) {
            return;

        } else if (this.getId() < toId && toId <= this.maxId) {
            this.rightChild.incrementPathWeight(toId, true);

        } else if (this.minId <= toId && toId < this.getId()) {
            this.leftChild.incrementPathWeight(toId, true);

        } else {
            this.parent.incrementPathWeight(toId, true);

        }
    }

    public void incrementWeight () {
        this.weight++;
    }
    /* End of Setters */

    /* Auxiliary Functions */
    public void debugNode () {
        if (this.id == -1) {
            System.out.println("Dummy Node");

        } else {
            System.out.println(
                "INFRAID: " + this.getId()
                + " lftID: " + this.getLeftChild().getId()
                + " rgtID: " + this.getRightChild().getId()
                + " parentId: " + this.getParent().getId()
                + " leftSUB: " + this.getMinId() + " rightSUB: " + this.getMaxId()
            );

        }
    }
    /* End of Auxiliary Functions */
}
