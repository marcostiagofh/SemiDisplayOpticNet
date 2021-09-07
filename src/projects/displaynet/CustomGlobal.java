package projects.displaynet;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Random;
import projects.defaultProject.BalancedTreeTopology;
import projects.defaultProject.DataCollection;
import projects.defaultProject.RequestQueue;
import projects.defaultProject.TreeConstructor;
import projects.defaultProject.nodes.messages.ApplicationMessage;
import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;
import projects.displaynet.nodes.nodeImplementations.DiSplayNetApp;
import projects.displaynet.nodes.timers.TriggerNodeOperation;
import sinalgo.configuration.Configuration;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.runtime.AbstractCustomGlobal;
import sinalgo.tools.Tools;
import sinalgo.tools.Tuple;

public class CustomGlobal extends AbstractCustomGlobal {

  // final condition
  public long MAX_REQ;

  // simulation
  public int numNodes = 30;
  public ArrayList<BinarySearchTreeLayer> tree = null;
  public BinarySearchTreeLayer controlNode = null;
  public TreeConstructor treeTopology = null;
  public RequestQueue requestQueue = null;

  // control execution
  public static boolean isSequencial = false;
  public static boolean mustGenerateSplay = true;

  public Random random = Tools.getRandomNumberGenerator();
  public double lambda = 0.05;

  // LOG
  DataCollection data = DataCollection.getInstance();

  @Override
  public boolean hasTerminated() {
    if (this.data.getCompletedRequests() >= MAX_REQ) {
      DiSplayNetApp node = (DiSplayNetApp) Tools.getNodeByID(1);
      this.data.addTotalTime(node.getCurrentRound());
      this.data.printRotationData();
      this.data.printRoutingData();
      return true;
    }
    return false;
  }

  @Override
  public void preRun() {

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
//        System.out.println(mu);
      }

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Missing configuration parameters");
    }

    // Set Log Path
    this.data.setPath(output);

    /*
     * read input data and configure the simulation
     */
    this.requestQueue = new RequestQueue(input);
    this.numNodes = this.requestQueue.getNumberOfNodes();
    MAX_REQ = this.requestQueue.getNumberOfRequests();

    /*
     * create the nodes and constructs the tree topology
     */
    this.tree = new ArrayList<BinarySearchTreeLayer>();

    for (int i = 0; i < numNodes; i++) {
      DiSplayNetApp n = new DiSplayNetApp();
      n.finishInitializationWithDefaultModels(true);
      this.tree.add(n);
    }

    this.controlNode = new DiSplayNetApp() {
      public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        String text = "ControlNode";
        super.drawNodeAsDiskWithText(g, pt, highlight, text, 10, Color.YELLOW);
      }
    };
    this.controlNode.finishInitializationWithDefaultModels(true);

    this.treeTopology = new BalancedTreeTopology(controlNode, this.tree);
    this.treeTopology.buildTree();
    this.treeTopology.setPositions();

    /*
     * initiate sigma buffers with message
     */
    /*
    while (this.requestQueue.hasNextRequest()) {
      Tuple<Integer, Integer> r = this.requestQueue.getNextRequest();
      DiSplayNetApp node = (DiSplayNetApp) Tools.getNodeByID(r.first);
      node.sendMessage(new ApplicationMessage(r.first, r.second));
    }*/

  }

  @Override
  public void preRound() {
    this.treeTopology.setPositions();

    if (mustGenerateSplay && this.requestQueue.hasNextRequest()) {
      mustGenerateSplay = false;

      double u = random.nextDouble();
      double x = Math.log(1 - u) / (-lambda);
      x = (int) x;
      if (x <= 0) {
        x = 1;
      }

      Tuple<Integer, Integer> r = this.requestQueue.getNextRequest();
      TriggerNodeOperation ted = new TriggerNodeOperation(new ApplicationMessage(r.first, r.second));
      ted.startGlobalTimer(x);
    }
  }
}