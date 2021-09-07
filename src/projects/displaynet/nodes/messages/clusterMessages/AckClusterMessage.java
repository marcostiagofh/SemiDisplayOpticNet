package projects.displaynet.nodes.messages.clusterMessages;

import projects.defaultProject.nodes.messages.NetworkMessage;
import projects.defaultProject.nodes.tableEntry.NodeInfo;
import sinalgo.nodes.messages.Message;

/**
 * AckCluster
 */
public class AckClusterMessage extends NetworkMessage {

  private double priority;
  private NodeInfo info;

  private int hopCounter;
  private boolean isFinalNode; // keep track if this node is final node in current request

  public AckClusterMessage(int src, int dst, double priority, int position, NodeInfo info) {
    super(src, dst);
    this.priority = priority;
    this.info = info;
    this.hopCounter = position;
  }

  public double getPriority() {
    return priority;
  }

  public NodeInfo getInfo() {
    return info;
  }

  public int getHopCounter() {
    return hopCounter;
  }

  public boolean isFinalNode() {
    return isFinalNode;
  }

  public void setPriority(double priority) {
    this.priority = priority;
  }

  public void setInfo(NodeInfo info) {
    this.info = info;
  }

  public void setHopCounter(int hopCounter) {
    this.hopCounter = hopCounter;
  }

  public void setFinalNode() {
    this.isFinalNode = true;
  }

  @Override
  public Message clone() {
    return this;
  }

}