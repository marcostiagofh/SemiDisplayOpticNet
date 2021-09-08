package projects.defaultProject.nodes.messages;

public class Request implements Comparable<Request> {

    private int srcId;
    private int dstId;
    private double priority = Double.MAX_VALUE;
    private boolean isMaster = false;

    public long numOfRotations = 0;
    public long numOfRouting = 0;
    public long numOfBypass = 0;
    public long numOfPauses = 0;

    public long initialTime = 0;
    public long finalTime = 0;

    public Request (int srcId, int dstId, boolean master) {
        this.srcId = srcId;
        this.dstId = dstId;
        this.isMaster = master;
    }

    public Request (int srcId, int dstId, double priority, boolean master) {
        this.srcId = srcId;
        this.dstId = dstId;
        this.priority = priority;
        this.isMaster = master;
    }

    public Request (Request o) {
        this.srcId = o.getSrcId();
        this.dstId = o.getDstId();
        this.priority = o.getPriority();
        this.isMaster = o.isMaster();
    }

    public int getSrcId () {
        return srcId;
    }

    public int getDstId () {
        return dstId;
    }

    public boolean isMaster () {
        return isMaster;
    }

    public void setPriority (double priority) {
        this.priority = priority;
    }

    public double getPriority () {
        return priority;
    }

    public int getRequesterNode () {
        return isMaster ? srcId : dstId;
    }

    public int getTargetNode () {
        return isMaster ? dstId : srcId;
    }

    @Override
    public int compareTo (Request o) {
        return Double.compare(this.getPriority(), o.getPriority()); // compare the priorities
    }
}
