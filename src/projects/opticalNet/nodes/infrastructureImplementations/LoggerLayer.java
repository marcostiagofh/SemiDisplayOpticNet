package projects.opticalNet.nodes.infrastructureImplementations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.DataSeries;
import projects.opticalNet.nodes.models.InfraNode;

public abstract class LoggerLayer extends SynchronizerLayer {

    private DataSeries rotationCounter = new DataSeries();
    private DataSeries alterationCounter = new DataSeries();
    private DataSeries messageRoutingCounter = new DataSeries();

    private ArrayList<Long> routingsPerSwitchRound;
    private ArrayList<Long> alterationsPerSwitchRound;
    private ArrayList<Long> routingsPerNodeRound;
    private ArrayList<Long> rotationsPerNodeRound;
    private ArrayList<Long> alterationsPerNodeRound;

    private long activeRequests = 0;
    private long currentRoundRotations = 0;
    private long roundCompletedRequests = 0;

    private long completedRequests = 0;

    // LOGS
    private Logging rotationPerRound;
    private Logging routingPerRound;
    private Logging alterationPerRound;

    private Logging nodesRoutingsPerRound;
    private Logging nodesRotationsPerRound;
    private Logging nodesAlterationsPerRound;

    private Logging clustersRoutingsPerRound;
    private Logging switchesRoutingsPerRound;
    private Logging clustersAlterationsPerRound;
    private Logging switchesAlterationsPerRound;

    private Logging routingPerMessage;

    private Logging activeRequestsPerRound;
    private Logging throughputLog;

    private Logging operationsLog;
    private Logging sequenceLog;

    public abstract int getNumNodes ();

    public abstract int getNumSwitches ();

    public abstract int getNumClustersType1 ();

    public abstract int getSwitchesPerClusterType1 ();

    public abstract int getNumClustersType2 ();

    public abstract int getSwitchesPerClusterType2 ();

    protected abstract int getRoutingSwitchId (int fromNodeId, int toNodeId);

    protected abstract boolean isValidNode (InfraNode node);

    /* Logger Functions */

    @Override
    public void logRoundResults () {
        this.rotationPerRound.logln(this.currentRoundRotations + "");
        this.routingPerRound.logln(this.getCurrentRoundRoutings() + "");
        this.alterationPerRound.logln(this.getCurrentRoundAlterations() + "");

        this.nodesRoutingsPerRound.logln(this.stringfyLogArray(this.routingsPerNodeRound));
        this.nodesRotationsPerRound.logln(
            this.stringfyLogArray(this.rotationsPerNodeRound)
        );
        this.nodesAlterationsPerRound.logln(
            this.stringfyLogArray(this.alterationsPerNodeRound)
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

        this.throughputLog.logln(this.roundCompletedRequests + "");
        this.activeRequestsPerRound.logln(this.activeRequests + "");

        this.resetRoundInfo();

    }

    public void logEndOfSimulation () {
        this.printRotationCounter();
        this.printAlterationCounter();
        this.printMessageRoutingCounter();

    }

    /* End of Logger Functions */

    /* Setter Functions */

    public void logIncrementAlterations (int swtId, int fromNodeId, int toNodeId) {
        this.alterationCounter.addSample(1);

        long value = this.alterationsPerSwitchRound.get(swtId);
        this.alterationsPerSwitchRound.set(swtId, value + 1);

        long fromNodeAlterations = this.alterationsPerNodeRound.get(fromNodeId);
        this.alterationsPerNodeRound.set(fromNodeId, fromNodeAlterations + 1);

        long toNodeAlterations = this.alterationsPerNodeRound.get(toNodeId);
        this.alterationsPerNodeRound.set(toNodeId, toNodeAlterations + 1);

    }

    public void logIncrementRouting (int netFromNodeId, int netToNodeId) {
        int fromNodeId = netFromNodeId - 1;
        int toNodeId = netToNodeId - 1;
        int swtId = this.getRoutingSwitchId(fromNodeId, toNodeId);

        long switchRouting = this.routingsPerSwitchRound.get(swtId);
        this.routingsPerSwitchRound.set(swtId, switchRouting + 1);

        long fromNodeRouting = this.routingsPerNodeRound.get(fromNodeId);
        this.routingsPerNodeRound.set(fromNodeId, fromNodeRouting + 1);

        long toNodeRouting = this.routingsPerNodeRound.get(toNodeId);
        this.routingsPerNodeRound.set(toNodeId, toNodeRouting + 1);

    }

    public void logMessageRouting (long num) {
        this.messageRoutingCounter.addSample(num);
        this.routingPerMessage.logln(num + "");

    }

    public void logRotation (long num, InfraNode... nodesInvolved) {
        this.rotationCounter.addSample(num);
        this.currentRoundRotations += num;

        for (InfraNode infNode: nodesInvolved) {
            if (this.isValidNode(infNode)) {
                int nodeId = infNode.getId();
                long value = this.rotationsPerNodeRound.get(nodeId);

                this.rotationsPerNodeRound.set(nodeId, value + num);

            }
        }

    }

    public void logIncrementActiveRequests () {
        this.activeRequests++;

    }

    public void logIncrementCompletedRequests () {
        this.roundCompletedRequests++;
        this.completedRequests++;

    }

    public void addSequence (int src, int dst) {
        this.sequenceLog.logln(src + "," + dst);

    }

    /* End of Setters Functions */

    /* Getters */

    public long getCurrentRoundRoutings () {
        return this.routingsPerSwitchRound.stream().mapToLong(Long::longValue).sum();

    }

    public long getCurrentRoundAlterations () {
        return this.alterationsPerSwitchRound.stream().mapToLong(Long::longValue).sum();

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

    /* End of Getters */

    /* Printer Functions */

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

    public void printMessageRoutingCounter () {
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

    /* End of Printer Functions */

    /* Auxiliary Functions */

    public void setLogPath (String path) {
        this.rotationPerRound = Logging.getLogger(path + "/rotations_per_round.txt");
        this.routingPerRound = Logging.getLogger(path + "/routings_per_round.txt");
        this.alterationPerRound = Logging.getLogger(path + "/alterations_per_round.txt");

        this.nodesRoutingsPerRound = Logging.getLogger(path + "/nodes_routings_per_round.txt");
        this.nodesRotationsPerRound = Logging.getLogger(path + "/nodes_rotations_per_round.txt");
        this.nodesAlterationsPerRound = Logging.getLogger(
            path + "/nodes_alterations_per_round.txt"
        );
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

    private ArrayList<Long> compressToCluster (ArrayList<Long> switchInfo) {
        ArrayList<Long> clusterInfo = new ArrayList<>();
        int beginOfCluster = 0;

        for (int i = 0; i < this.getNumClustersType1(); i++) {
            long clusterValue = 0;
            for (int j = 0; j < this.getSwitchesPerClusterType1(); j++) {
                clusterValue += switchInfo.get(beginOfCluster + j);

            }

            clusterInfo.add(clusterValue);
            beginOfCluster += this.getSwitchesPerClusterType1();
        }

        for (int i = 0; i < this.getNumClustersType2(); i++) {
            long clusterValue = 0;
            for (int j = 0; j < this.getSwitchesPerClusterType2(); j++) {
                clusterValue += switchInfo.get(beginOfCluster + j);

            }

            clusterInfo.add(clusterValue);
            beginOfCluster += this.getSwitchesPerClusterType2();
        }

        return clusterInfo;

    }

    private String stringfyLogArray (ArrayList<Long> logArray) {
        return Stream.of(logArray).map(Object::toString).collect(Collectors.joining(" ", "", ""));

    }

    /* End of Auxiliary Functions */

    /* Reset counter Functions */
    public void resetRoundInfo () {
        this.routingsPerNodeRound = new ArrayList<>(Collections.nCopies(this.getNumNodes(), 0L));
        this.rotationsPerNodeRound = new ArrayList<>(Collections.nCopies(this.getNumNodes(), 0L));
        this.alterationsPerNodeRound = new ArrayList<>(Collections.nCopies(this.getNumNodes(), 0L));
        this.routingsPerSwitchRound = new ArrayList<>(
            Collections.nCopies(this.getNumSwitches(), 0L)
        );
        this.alterationsPerSwitchRound = new ArrayList<>(
            Collections.nCopies(this.getNumSwitches(), 0L)
        );

        this.activeRequests = 0;
        this.currentRoundRotations = 0;
        this.roundCompletedRequests = 0;

    }

    public void resetCollection () {
        this.alterationCounter.reset();
        this.rotationCounter.reset();

    }
    /* End of Reset counter Functions */
}
