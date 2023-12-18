package projects.bstOpticalNet.nodes.models;


/**
 * These edges are used to remove and add switch links before the end of the controller step.
 * They help implementing augmenting path algorithm and are necessary to use for
 * removing redundant link changes while posing no drawbacks for the downward/upward switch
 * representation
 */
public class Edge {
    private InfraNode fromNode;
    private InfraNode toNode;
    private boolean downward;
    private boolean initial;
    private int swtOffset = -1;


    public Edge (InfraNode fromNode, InfraNode toNode, boolean downward, boolean initial) {
        this(fromNode, toNode, downward, initial, -1);
    }

    public Edge (InfraNode fromNode, InfraNode toNode, boolean downward, boolean initial, int swtOffset) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.downward = downward;
        this.initial = initial;
        this.swtOffset = swtOffset;
    }

    public InfraNode getFromNode () {
        return this.fromNode;

    }

    public int getFromNodeId () {
        return this.fromNode.getId();

    }

    public InfraNode getToNode () {
        return this.toNode;

    }

    public int getToNodeId () {
        return this.toNode.getId();

    }

    public boolean isDownward () {
        return this.downward;

    }

    public boolean isInitial () {
        return this.initial;

    }

    public int getSwtOffset () {
        return this.swtOffset;

    }

    public void setSwtOffset (int swtOffset) {
        this.swtOffset = swtOffset;

    }

    public void debugEdge () {

        System.out.println("----------------------------------------");
        this.getFromNode().debugNode();
        this.getToNode().debugNode();

        System.out.println(
            "FronNode: " + this.getFromNodeId()
            + " ToNode: " + this.getToNodeId() + " swtIdx: " + this.getSwtOffset()
            + " downward: " + this.isDownward()
        );
        System.out.println("----------------------------------------");
    }
};