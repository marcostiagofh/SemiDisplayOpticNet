package projects.opticalNet.nodes.infrastructureImplementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import projects.opticalNet.nodes.models.InfraNode;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.DataSeries;

public abstract class LoggerLayer extends SynchronizerLayer {

    private DataSeries routingCounter = new DataSeries();
    private DataSeries rotationCounter = new DataSeries();
    private DataSeries alterationCounter = new DataSeries();
    private DataSeries messageRoutingCounter = new DataSeries();

    private ArrayList<Long> routingsPerSwitchRound;
    private ArrayList<Long> alterationsPerSwitchRound;
    private ArrayList<Long> routingsPerNodeRound;
    private ArrayList<Long> rotationsPerNodeRound;

    private ArrayList<Long> lockedNodes;

    private long activeRequests = 0;
    private long currentRoundRotations = 0;

    private long completedRequests = 0;

    // LOGS
    private Logging rotationPerRound;
    private Logging routingPerRound;
    private Logging alterationPerRound;

    private Logging nodesRoutingsPerRound;
    private Logging nodesRotationsPerRound;

    private Logging clustersRoutingsPerRound;
    private Logging switchesRoutingsPerRound;
    private Logging clustersAlterationsPerRound;
    private Logging switchesAlterationsPerRound;

    private Logging routingPerMessage;

    private Logging activeRequestsPerRound;

    private Logging lockedNodesPerRound;

    private Logging throughputLog;
    private Logging operationsLog;
    private Logging sequenceLog;

    public abstract int getNumNodes ();

    public abstract int getNumSwitches ();

    public abstract int getNumClusters ();

    public abstract int getClusterSize ();

    public void setLogPath (String path) {
        this.rotationPerRound = Logging.getLogger(path + "/rotations_per_round.txt");
        this.routingPerRound = Logging.getLogger(path + "/routings_per_round.txt");
        this.alterationPerRound = Logging.getLogger(path + "/alterations_per_round.txt");

        this.lockedNodesPerRound = Logging.getLogger(path + "/locked_nodes_per_round.txt");

        this.nodesRoutingsPerRound = Logging.getLogger(path + "/nodes_routings_per_round.txt");
        this.nodesRotationsPerRound = Logging.getLogger(path + "/nodes_rotations_per_round.txt");
        this.switchesRoutingsPerRound = Logging.getLogger(
            path + "/switches_routings_per_round.txt"
        );
        this.clustersRoutingsPerRound = Logging.getLogger(
            path + "/clusters_routings_per_round.txt"
        );
        this.switchesAlterationsPerRound = Logging.getLogger(
            path + "/switches_alterations_per_round.txt"
        );
        this.clustersAlterationsPerRound = Logging.getLogger(
            path + "/clusters_alterations_per_round.txt"
        );

        this.activeRequestsPerRound = Logging.getLogger(path + "/active_requests_per_round.txt");

        this.routingPerMessage = Logging.getLogger(path + "/routing_per_message.txt");

        this.throughputLog = Logging.getLogger(path + "/throughput.txt");
        this.operationsLog = Logging.getLogger(path + "/operations.txt");
        this.sequenceLog = Logging.getLogger(path + "/sequence.txt");
    }

    public void resetRoundInfo () {
        this.routingsPerNodeRound = new ArrayList<>(Collections.nCopies(this.getNumNodes(), 0L));
        this.rotationsPerNodeRound = new ArrayList<>(Collections.nCopies(this.getNumNodes(), 0L));
        this.routingsPerSwitchRound = new ArrayList<>(
            Collections.nCopies(this.getNumSwitches(), 0L)
        );
        this.alterationsPerSwitchRound = new ArrayList<>(
            Collections.nCopies(this.getNumSwitches(), 0L)
        );

        this.lockedNodes = new ArrayList<>(Collections.nCopies(this.getNumNodes(), 0L));

        this.activeRequests = 0;
        this.currentRoundRotations = 0;

    }

    public long currentRoundRoutings () {
        return this.routingsPerSwitchRound.stream().mapToLong(Long::longValue).sum();

    }

    public long currentRoundAlterations () {
        return this.alterationsPerSwitchRound.stream().mapToLong(Long::longValue).sum();

    }

    private ArrayList<Long> compressToCluster (ArrayList<Long> switchInfo) {
        ArrayList<Long> clusterInfo = new ArrayList<>();
        int beginOfCluster = 0;

        for (int i = 0; i < this.getNumClusters(); i++) {
            long clusterValue = 0;
            for (int j = 0; j < this.getClusterSize(); j++) {
                clusterValue += switchInfo.get(beginOfCluster + j);

            }

            clusterInfo.add(clusterValue);
            beginOfCluster += this.getClusterSize();
        }

        return clusterInfo;

    }

    private String stringfyLogArray (ArrayList<Long> logArray) {
        return Stream.of(logArray).map(Object::toString).collect(Collectors.joining(" "));

    }

    @Override
    public void logRoundResults () {
        this.rotationPerRound.logln(this.currentRoundRotations + "");
        this.routingPerRound.logln(this.currentRoundRoutings() + "");
        this.alterationPerRound.logln(this.currentRoundAlterations() + "");

        this.lockedNodesPerRound.logln(this.stringfyLogArray(this.lockedNodes));

        this.nodesRoutingsPerRound.logln(this.stringfyLogArray(this.routingsPerNodeRound));
        this.nodesRotationsPerRound.logln(
            this.stringfyLogArray(this.rotationsPerNodeRound)
        );

        this.switchesRoutingsPerRound.logln(this.stringfyLogArray(this.routingsPerSwitchRound));
        this.switchesAlterationsPerRound.logln(
            this.stringfyLogArray(this.alterationsPerSwitchRound)
        );

        this.clustersRoutingsPerRound.logln(
            this.stringfyLogArray(this.compressToCluster(this.routingsPerSwitchRound))
        );
        this.clustersAlterationsPerRound.logln(
            this.stringfyLogArray(this.compressToCluster(this.alterationsPerSwitchRound))
        );

        this.activeRequestsPerRound.logln(this.activeRequests + "");

    }

    public void lockNodes (InfraNode... nodesInvolved) {
        for (InfraNode infNode: nodesInvolved) {
            int nodeId = infNode.getId();
            this.lockedNodes.set(nodeId, 1L);
        }

    }

    public void addRotation (long num, InfraNode... nodesInvolved) {
        this.rotationCounter.addSample(num);
        this.currentRoundRotations += num;

        for (InfraNode infNode: nodesInvolved) {
            int nodeId = infNode.getId();
            long value = this.rotationsPerNodeRound.get(nodeId);
            this.rotationsPerNodeRound.set(nodeId, value + num);
        }

    }

    public void incrementActiveRequests () {
        this.activeRequests++;

    }

    public void incrementAlterations (int swtId) {
        this.alterationCounter.addSample(1);

        long value = this.alterationsPerSwitchRound.get(swtId);
        this.alterationsPerSwitchRound.set(swtId, value + 1);

    }

    public void incrementRouting (int swtId, int fromNodeId, int toNodeId) {
        this.routingCounter.addSample(1);

        long switchRouting = this.routingsPerSwitchRound.get(swtId);
        this.routingsPerSwitchRound.set(swtId, switchRouting + 1);

        long fromNodeRouting = this.routingsPerNodeRound.get(fromNodeId);
        this.routingsPerNodeRound.set(fromNodeId, fromNodeRouting + 1);

        long toNodeRouting = this.routingsPerNodeRound.get(toNodeId);
        this.routingsPerNodeRound.set(toNodeId, toNodeRouting + 1);

    }

    public void addMessageRouting (long num) {
        this.messageRoutingCounter.addSample(num);
        this.routingPerMessage.logln(num + "");

    }

    public void addSequence (int src, int dst) {
        this.sequenceLog.logln(src + "," + dst);

    }

    public void addThroughput (long num) {
        this.throughputLog.logln(num + "");

    }

    public void incrementCompletedRequests () {
        this.completedRequests++;

    }

    public long getCompletedRequests () {
        return completedRequests;

    }

    public DataSeries getAlterationCounterSeries () {
        return this.alterationCounter;

    }

    public DataSeries getRotationCounterSeries () {
        return this.rotationCounter;

    }

    public DataSeries getRoutingCounterSeries () {
        return this.routingCounter;

    }

    public void printAlterationCounter () {
        System.out.println("Alterations:");
        System.out.println("Number of request: " + this.alterationCounter.getNumberOfSamples());
        System.out.println("Mean: " + this.alterationCounter.getMean());
        System.out.println("Standard Deviation: " + this.alterationCounter.getStandardDeviation());
        System.out.println("Min: " + this.alterationCounter.getMinimum());
        System.out.println("Max: " + this.alterationCounter.getMaximum());

        this.operationsLog.logln(
            "alteration," +
            this.alterationCounter.getSum() + "," +
            this.alterationCounter.getMean() + "," +
            this.alterationCounter.getStandardDeviation() + "," +
            this.alterationCounter.getMinimum() + "," +
            this.alterationCounter.getMaximum()
        );
    }

    public void printRotationCounter () {
        System.out.println("Rotations:");
        System.out.println("Number of request: " + this.rotationCounter.getNumberOfSamples());
        System.out.println("Mean: " + this.rotationCounter.getMean());
        System.out.println("Standard Deviation: " + this.rotationCounter.getStandardDeviation());
        System.out.println("Min: " + this.rotationCounter.getMinimum());
        System.out.println("Max: " + this.rotationCounter.getMaximum());

        this.operationsLog.logln(
            "rotation," +
            this.rotationCounter.getSum() + "," +
            this.rotationCounter.getMean() + "," +
            this.rotationCounter.getStandardDeviation() + "," +
            this.rotationCounter.getMinimum() + "," +
            this.rotationCounter.getMaximum()
        );
    }

    public void printRoutingConter () {
        System.out.println("Routing:");
        System.out.println("Number of request " + this.routingCounter.getNumberOfSamples());
        System.out.println("Mean: " + this.routingCounter.getMean());
        System.out.println("Standard Deviation: " + this.routingCounter.getStandardDeviation());
        System.out.println("Min: " + this.routingCounter.getMinimum());
        System.out.println("Max: " + this.routingCounter.getMaximum());

        this.operationsLog.logln(
            "routing," +
            this.routingCounter.getSum() + "," +
            this.routingCounter.getMean() + "," +
            this.routingCounter.getStandardDeviation() + "," +
            this.routingCounter.getMinimum() + "," +
            this.routingCounter.getMaximum()
        );
    }

    public void printMessageRoutingConter () {
        System.out.println("Message Routing:");
        System.out.println("Number of messages " + this.messageRoutingCounter.getNumberOfSamples());
        System.out.println("Mean: " + this.messageRoutingCounter.getMean());
        System.out.println(
            "Standard Deviation: " + this.messageRoutingCounter.getStandardDeviation()
        );
        System.out.println("Min: " + this.messageRoutingCounter.getMinimum());
        System.out.println("Max: " + this.messageRoutingCounter.getMaximum());

        this.operationsLog.logln(
            "message-routing," +
            this.messageRoutingCounter.getSum() + "," +
            this.messageRoutingCounter.getMean() + "," +
            this.messageRoutingCounter.getStandardDeviation() + "," +
            this.messageRoutingCounter.getMinimum() + "," +
            this.messageRoutingCounter.getMaximum()
        );
    }

    public void resetCollection () {
        this.alterationCounter.reset();
        this.rotationCounter.reset();
        this.routingCounter.reset();
    }
}
