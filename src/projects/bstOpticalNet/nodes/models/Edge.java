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
    private int swtOffset = -1;


    public Edge (InfraNode fromNode, InfraNode toNode, boolean downward) {
        this(fromNode, toNode, downward, -1);
    }

    public Edge (InfraNode fromNode, InfraNode toNode, boolean downward, int swtOffset) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.downward = downward;
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

    public int getSwtOffset () {
        return this.swtOffset;

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