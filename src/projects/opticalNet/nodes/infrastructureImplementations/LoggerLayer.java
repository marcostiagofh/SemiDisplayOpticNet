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
    private DataSeries activeRequestsCounter = new DataSeries();

    private ArrayList<Long> routingsPerSwitchRound;
    private ArrayList<Long> activePortsPerSwitchRound;
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
    private Logging activeRequestsPerRound;

    private Logging nodesRoutingsPerRound;
    private Logging nodesRotationsPerRound;
    private Logging nodesAlterationsPerRound;

    private Logging switchesRoutingsPerRound;
    private Logging switchesActivePortsPerRound;
    private Logging switchesAlterationsPerRound;

    private Logging routingPerMessage;

    private Logging throughputLog;

    private Logging simulationLog;
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
        this.activeRequestsPerRound.logln(this.activeRequests + "");
        this.activeRequestsCounter.addSample(this.activeRequests);

        this.nodesRoutingsPerRound.logln(this.stringfyLogArray(this.routingsPerNodeRound));
        this.nodesRotationsPerRound.logln(
            this.stringfyLogArray(this.rotationsPerNodeRound)
        );
        this.nodesAlterationsPerRound.logln(
            this.stringfyLogArray(this.alterationsPerNodeRound)
        );

        this.switchesRoutingsPerRound.logln(this.stringfyLogArray(this.routingsPerSwitchRound));
        this.switchesActivePortsPerRound.logln(
            this.stringfyLogArray(this.activePortsPerSwitchRound)
        );
        this.switchesAlterationsPerRound.logln(
            this.stringfyLogArray(this.alterationsPerSwitchRound)
        );

        this.throughputLog.logln(this.roundCompletedRequests + "");

        this.resetRoundInfo();

    }

    public void logEndOfSimulation () {
        this.operationsLog.logln("name,sum,mean,std_dvt,min,max");
        this.printRotationCounter();
        this.printAlterationCounter();
        this.printMessageRoutingCounter();
        this.printActiveRequestsCounter();

        this.printSimulationInfo();
    }

    /* End of Logger Functions */

    /* Setter Functions */

    public void logIncrementActivePorts (int swtId) {
        long value = this.activePortsPerSwitchRound.get(swtId);
        this.activePortsPerSwitchRound.set(swtId, value + 1);

    }

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
        int swtId = this.getRoutingSwitchId(netFromNodeId, netToNodeId);

        long switchRouting = this.routingsPerSwitchRound.get(swtId);
        this.routingsPerSwitchRound.set(swtId, switchRouting + 1);

        long fromNodeRouting = this.routingsPerNodeRound.get(netFromNodeId - 1);
        this.routingsPerNodeRound.set(netFromNodeId - 1, fromNodeRouting + 1);

        long toNodeRouting = this.routingsPerNodeRound.get(netToNodeId - 1);
        this.routingsPerNodeRound.set(netToNodeId - 1, toNodeRouting + 1);

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
    public void printActiveRequestsCounter () {
        this.printCounter(this.activeRequestsCounter, "active-requests");

    }

    public void printAlterationCounter () {
        this.printCounter(this.alterationCounter, "alteration");
    }

    public void printRotationCounter () {
        this.printCounter(this.rotationCounter, "rotation");

    }

    public void printMessageRoutingCounter () {
        this.printCounter(this.messageRoutingCounter, "message-routing");

    }

    public void printCounter (DataSeries counter, String operation) {
        System.out.println(operation);
        System.out.println("Number of request " + counter.getNumberOfSamples());
        System.out.println("Number of operations " + counter.getSum());
        System.out.println("Mean: " + counter.getMean());
        System.out.println(
            "Standard Deviation: " + counter.getStandardDeviation()
        );
        System.out.println("Min: " + counter.getMinimum());
        System.out.println("Max: " + counter.getMaximum());

        this.operationsLog.logln(
            operation + "," +
            counter.getSum() + "," +
            counter.getMean() + "," +
            counter.getStandardDeviation() + "," +
            counter.getMinimum() + "," +
            counter.getMaximum()
        );

    }

    public void printSimulationInfo () {
        this.simulationLog.logln(
            "num-cluster-1 " + this.getNumClustersType1() +
            " size-cluster-1 " + this.getSwitchesPerClusterType1()
        );
        this.simulationLog.logln(
            "num-cluster-2 " + this.getNumClustersType2() +
            " size-cluster-2 " + this.getSwitchesPerClusterType2()
        );
    }

    /* End of Printer Functions */

    /* Auxiliary Functions */

    public void setLogPath (String path) {
        this.rotationPerRound = Logging.getLogger(path + "/rotations_per_round.txt");
        this.routingPerRound = Logging.getLogger(path + "/routings_per_round.txt");
        this.alterationPerRound = Logging.getLogger(path + "/alterations_per_round.txt");
        this.activeRequestsPerRound = Logging.getLogger(path + "/active_requests_per_round.txt");

        this.nodesRoutingsPerRound = Logging.getLogger(path + "/nodes_routings_per_round.txt");
        this.nodesRotationsPerRound = Logging.getLogger(path + "/nodes_rotations_per_round.txt");
        this.nodesAlterationsPerRound = Logging.getLogger(
            path + "/nodes_alterations_per_round.txt"
        );
        this.switchesRoutingsPerRound = Logging.getLogger(
            path + "/switches_routings_per_round.txt"
        );
        this.switchesActivePortsPerRound = Logging.getLogger(
            path + "/switches_active_ports_per_round.txt"
        );
        this.switchesAlterationsPerRound = Logging.getLogger(
            path + "/switches_alterations_per_round.txt"
        );


        this.routingPerMessage = Logging.getLogger(path + "/routing_per_message.txt");

        this.simulationLog = Logging.getLogger(path + "/simulation_info.txt");
        this.throughputLog = Logging.getLogger(path + "/throughput.txt");
        this.operationsLog = Logging.getLogger(path + "/operations.txt");
        this.sequenceLog = Logging.getLogger(path + "/sequence.txt");
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
        this.activePortsPerSwitchRound = new ArrayList<>(
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
