package projects.opticalNet.nodes.infrastructureImplementations;

import java.util.ArrayList;
import java.util.Collections;

import sinalgo.tools.logging.Logging;
import sinalgo.tools.statistics.DataSeries;
import projects.opticalNet.nodes.models.InfraNode;

/**
 * This abstract class is responsible to log the simulation results
 */
public abstract class LoggerLayer extends SynchronizerLayer {
    protected String projectName;

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

    protected abstract int getRoutingSwitchId (InfraNode fromNode, InfraNode toNode);

    protected abstract int getSwitchId (InfraNode fromNode, InfraNode toNode);

    protected abstract boolean isValidNode (InfraNode node);

    /* Logger Functions */

    /**
     * This functions logs the round results. It logs the rotation, alteration and
     * routings performed this round, as well as this metrics distributed over the
     * NetworkNode's and Switches.
     */
    @Override
    public void logRoundResults () {
        this.rotationPerRound.logln(this.projectName + "," + this.currentRoundRotations);
        this.routingPerRound.logln(this.projectName + "," + this.getCurrentRoundRoutings());
        this.alterationPerRound.logln(this.projectName + "," + this.getCurrentRoundAlterations());
        this.activeRequestsPerRound.logln(this.projectName + "," + this.activeRequests);
        this.activeRequestsCounter.addSample(this.activeRequests);

        this.logSparceLogger(this.nodesRoutingsPerRound, this.routingsPerNodeRound);
        this.logSparceLogger(this.nodesRotationsPerRound, this.rotationsPerNodeRound);
        this.logSparceLogger(this.nodesAlterationsPerRound, this.alterationsPerNodeRound);

        this.logSparceLogger(this.switchesRoutingsPerRound, this.routingsPerSwitchRound);
        this.logSparceLogger(this.switchesActivePortsPerRound, this.activePortsPerSwitchRound);
        this.logSparceLogger(this.switchesAlterationsPerRound, this.alterationsPerSwitchRound);

        this.throughputLog.logln(this.roundCompletedRequests + "");

        this.resetRoundInfo();

    }

    /**
     * Logs the results stored on the rotation, alteration, routing and
     * active requests counter on the operations.txt file.
     */
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

    /**
     * Increment round actions per node or switch as a sparse matrix to save space.
     * @param logger        Logging object for the parameter
     * @param logArray      Array containing information about the round
     */
    private void logSparceLogger (Logging logger, ArrayList<Long> logArray) {
        for (int indx = 0; indx < logArray.size(); indx++) {
            Long value = logArray.get(indx);
            if (value != 0) {
                logger.logln(
                    this.projectName + "," + this.getCurrentRound() + "," + indx + "," + value
                );
            }

        }

    }

    public void logZigZigUpdateActivePorts (InfraNode w, InfraNode z, InfraNode y, InfraNode c) {
        this.logDecrementActivePorts(w, z);
        this.logDecrementActivePorts(z, y);
        this.logDecrementActivePorts(y, c);
        this.logIncrementActivePorts(w, y);
        this.logIncrementActivePorts(y, z);
        this.logIncrementActivePorts(z, c);
    }

    public void logZigZagUpdateActivePorts (
        InfraNode w, InfraNode z, InfraNode y, InfraNode x, InfraNode b, InfraNode c
    ) {
        this.logDecrementActivePorts(w, z);
        this.logDecrementActivePorts(z, y);
        this.logDecrementActivePorts(y, x);
        this.logDecrementActivePorts(x, b);
        this.logDecrementActivePorts(x, c);
        this.logIncrementActivePorts(w, x);
        this.logIncrementActivePorts(x, y);
        this.logIncrementActivePorts(x, z);
        this.logIncrementActivePorts(y, b);
        this.logIncrementActivePorts(z, c);

    }

    /**
     * Increment the number of active ports on the switch_swtid. Active ports
     * are ports where the connection InputNode -> OutputNode are representatives
     * of a real edge on our graph.
     * @param swtId         the switch id
     */
    public void logIncrementActivePorts (InfraNode fromNode, InfraNode toNode) {
        if (!this.isValidNode(fromNode) || !this.isValidNode(toNode)) {
            return;

        }

        int swtId = this.getSwitchId(fromNode, toNode);
        long value = this.activePortsPerSwitchRound.get(swtId);
        this.activePortsPerSwitchRound.set(swtId, value + 1);

    }

    /**
     * Decrement the number of active ports on the switch_swtid. Active ports
     * are ports where the connection InputNode -> OutputNode are representatives
     * of a real edge on our graph.
     * @param swtId         the switch id
     */
    public void logDecrementActivePorts (InfraNode fromNode, InfraNode toNode) {
        if (!this.isValidNode(fromNode) || !this.isValidNode(toNode)) {
            return;

        }

        int swtId = this.getSwitchId(fromNode, toNode);
        long value = this.activePortsPerSwitchRound.get(swtId);
        this.activePortsPerSwitchRound.set(swtId, value - 1);

    }


    /**
     * Increment the number of alterations performed on a given round, updating this counter
     * for the parent node, child node and switch involved. An alteration occurs when you remove
     * one edge on our graph and add another.
     * @param swtId         the switch id
     * @param fromNode      the parent node
     * @param toNode        the child node
     */
    public void logIncrementAlterations (InfraNode fromNode, InfraNode toNode) {
        this.alterationCounter.addSample(1);
        int swtId = this.getRoutingSwitchId(fromNode, toNode);

        long value = this.alterationsPerSwitchRound.get(swtId);
        this.alterationsPerSwitchRound.set(swtId, value + 1);

        long fromNodeAlterations = this.alterationsPerNodeRound.get(fromNode.getId());
        this.alterationsPerNodeRound.set(fromNode.getId(), fromNodeAlterations + 1);

        long toNodeAlterations = this.alterationsPerNodeRound.get(toNode.getId());
        this.alterationsPerNodeRound.set(toNode.getId(), toNodeAlterations + 1);

    }

    /**
     * Increment the number of routings that the nodes fromNode, toNode and the switch that
     * implements their edge are involed in this round.
     * @param netFromNodeId the node that started the routing
     * @param netToNodeId   the node that received the message
     */
    public void logIncrementRouting (InfraNode fromNode, InfraNode toNode) {
        int swtId = this.getRoutingSwitchId(fromNode, toNode);

        long switchRouting = this.routingsPerSwitchRound.get(swtId);
        this.routingsPerSwitchRound.set(swtId, switchRouting + 1);

        long fromNodeRouting = this.routingsPerNodeRound.get(fromNode.getId());
        this.routingsPerNodeRound.set(fromNode.getId(), fromNodeRouting + 1);

        long toNodeRouting = this.routingsPerNodeRound.get(toNode.getId());
        this.routingsPerNodeRound.set(toNode.getId(), toNodeRouting + 1);

    }

    /**
     * Update the message-routing counter with the number of times a message has been
     * routed.
     * @param num   how many times the message was routed
     */
    public void logMessageRouting (long num) {
        this.messageRoutingCounter.addSample(num);
        this.routingPerMessage.logln(this.projectName + "," + num);

    }

    /**
     * Update rotation counter and the number of rotations the nodesInvolved performed
     * @param num           how many rotations this call represents
     * @param nodesInvolved which nodes are involved in it
     */
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

    /**
     * Increment the number of active requests for the round
     */
    public void logIncrementActiveRequests () {
        this.activeRequests++;

    }

    /**
     * Increment the number of completed requests for the round
     */
    public void logIncrementCompletedRequests () {
        this.roundCompletedRequests++;
        this.completedRequests++;

    }

    public void addSequence (int src, int dst) {
        this.sequenceLog.logln(src + "," + dst);

    }

    /* End of Setters Functions */

    /* Getters */

    /**
     * Sum the number of routings each switch performed to get the number of
     * routings performed in the simulation on this round.
     * @return the number of routings performed this round
     */
    public long getCurrentRoundRoutings () {
        return this.routingsPerSwitchRound.stream().mapToLong(Long::longValue).sum();

    }

    /**
     * Sum the number of alterations each switch performed to get the number of
     * alterations performed in the simulation on this round.
     * @return the number of alterations performed this round
     */
    public long getCurrentRoundAlterations () {
        return this.alterationsPerSwitchRound.stream().mapToLong(Long::longValue).sum();

    }

    /**
     * Getter for the number of completed requests
     * @return The number of completed requests so far
     */
    public long getCompletedRequests () {
        return completedRequests;

    }

    /**
     * Getter for the alteration counter
     * @return The alteration Counter
     */
    public DataSeries getAlterationCounterSeries () {
        return this.alterationCounter;

    }

    /**
     * Getter for the rotation counter
     * @return The rotation Counter
     */
    public DataSeries getRotationCounterSeries () {
        return this.rotationCounter;

    }

    /* End of Getters */

    /* Printer Functions */
    /**
     * Print Active Requests counter informations.
     */
    public void printActiveRequestsCounter () {
        this.printCounter(this.activeRequestsCounter, "active-requests");

    }

    /**
     * Print Alteration counter informations.
     */
    public void printAlterationCounter () {
        this.printCounter(this.alterationCounter, "alteration");
    }

    /**
     * Print Rotation counter informations.
     */
    public void printRotationCounter () {
        this.printCounter(this.rotationCounter, "rotation");

    }

    /**
     * Print MessageRouting counter informations.
     */
    public void printMessageRoutingCounter () {
        this.printCounter(this.messageRoutingCounter, "message-routing");

    }

    /**
     * Print the information of counter in the order: (sum, mean, std, min, max).
     * @param counter       the DataSeries counter
     * @param operation     the identification string of the counter
     */
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

    /**
     * Reports the simulation info, the number and size of clusters of type 1 and 2.
     */
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

    /**
     * Configure the path of the logger files with the path provided by the output parameter
     * in the simulation.
     * @param path      path to folder where the logger files are stored
     */
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

        this.initSimulationLog();
    }

    public void initSimulationLog () {

        this.rotationPerRound.logln("project,rotation");
        this.routingPerRound.logln("project,routing");
        this.alterationPerRound.logln("project,alteration");
        this.activeRequestsPerRound.logln("project,active_requests");

        this.nodesRoutingsPerRound.logln("project,round,node,routing");
        this.nodesRotationsPerRound.logln("project,round,node,rotation");
        this.nodesAlterationsPerRound.logln("project,round,node,alteration");

        this.switchesRoutingsPerRound.logln("project,round,switch,routing");
        this.switchesActivePortsPerRound.logln("project,round,switch,active_ports");
        this.switchesAlterationsPerRound.logln("project,round,switch,alteration");

        this.routingPerMessage.logln("project,round,message,times");
    }

    /* End of Auxiliary Functions */

    /* Reset counter Functions */
    /**
     * Called by the end of round to reset round related information.
     */
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
    /* End of Reset counter Functions */
}
