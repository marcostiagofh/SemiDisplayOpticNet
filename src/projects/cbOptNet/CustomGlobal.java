package projects.cbOptNet;

import java.util.ArrayList;
import java.util.Random;

import projects.cbOptNet.nodes.timers.TriggerNodeOperation;
import projects.cbOptNet.nodes.nodeImplementations.CBNetController;
import projects.opticalNet.nodes.nodeImplementations.NetworkNode;
import projects.defaultProject.DataCollection;
import projects.defaultProject.RequestQueue;

import sinalgo.configuration.Configuration;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.Tuple;

public class CustomGlobal extends AbstractCustomGlobal {

    /* Final Condition */
    public long MAX_REQ;
    private long temp_reqs;

    /* Simulation */
    public int numberOfNodes = 128;
    public int switchSize = 256;
    public CBNetController controller = null;
    public ArrayList<NetworkNode> netNodes = new ArrayList<>();

    public RequestQueue requestQueue;

    /* Control Execution */
    public static boolean isSequencial = true;
    public static boolean mustGenerateSplay = true;

    public Random random = Tools.getRandomNumberGenerator();
    public double lambda = 0.05;

    DataCollection data = DataCollection.getInstance();

    @Override
    public void preRun () {
        String input = "";
        String output = "";

        try {

            if (Configuration.hasParameter("input")) {
                input = Configuration.getStringParameter("input");
            }

            if (Configuration.hasParameter("output")) {
                output = Configuration.getStringParameter("output");
            }

            if (Configuration.hasParameter("mu")) {
                double mu = (double) Configuration.getIntegerParameter("mu");
                lambda = (double) (1 / mu);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Missing configuration parameters");
        }

        /* Set Log Path */
        this.data.setPath(output);

        this.requestQueue = new RequestQueue(input);
        MAX_REQ = this.requestQueue.getNumberOfRequests();

        for (int i = 0; i < this.numberOfNodes; i++) {
            NetworkNode newNetNode = new NetworkNode();
            newNetNode.finishInitializationWithDefaultModels(true);
            netNodes.add(newNetNode);
        }

        this.controller = new CBNetController(
            this.numberOfNodes, this.switchSize, netNodes, this.data
        );
        this.controller.finishInitializationWithDefaultModels(true);

        for (int i = 0; i < this.numberOfNodes; i++) {
            netNodes.get(i).setController(this.controller);
        }

        this.controller.renderTopology(Configuration.dimX, Configuration.dimY);
    }

    @Override
    public void preRound () {
        
        if (mustGenerateSplay && this.requestQueue.hasNextRequest()) {
            mustGenerateSplay = false;
            
            double u = random.nextDouble();
            double x = Math.log(1 - u) / (-lambda);
            x = (int) x;
            if (x <= 0) {
                x = 1;
            }
            
            Tuple<Integer, Integer> r = this.requestQueue.getNextRequest();
            TriggerNodeOperation ted = new TriggerNodeOperation(r.first + 1, r.second + 1);
            ted.startGlobalTimer(x);
            
            this.temp_reqs++;
        }

        System.out.println(this.data.getCompletedRequests() + " " + MAX_REQ);
    }

    @Override
    public boolean hasTerminated () {
        if (this.data.getCompletedRequests() == MAX_REQ) {
            return true;

        }

        return false;
    }

}
