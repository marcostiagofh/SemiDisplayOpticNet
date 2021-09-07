package projects.displaynet.nodes.messages.clusterMessages;

import projects.defaultProject.nodes.messages.NetworkMessage;
import projects.displaynet.nodes.tableEntry.Request;
import sinalgo.nodes.messages.Message;

/**
 * RequestCluster
 */
public class RequestClusterMessage extends NetworkMessage implements
    Comparable<RequestClusterMessage> {

  private double priority;
  private boolean isMaster = false;

  private int hopCounter;
  private boolean isFinalNode; // keep track if this node is final node in current request

  public RequestClusterMessage(RequestClusterMessage msg) {
    super(msg.getSource(), msg.getDestination());
    this.priority = msg.getPriority();
    this.hopCounter = msg.getHopCounter();
    this.isFinalNode = false;
    this.isMaster = msg.isMaster();
  }

  public RequestClusterMessage(int src, int dst, int hopCounter, double priority, boolean master) {
    super(src, dst);
    this.priority = priority;
    this.hopCounter = hopCounter;
    this.isFinalNode = false;
    this.isMaster = master;
  }

  public double getPriority() {
    return priority;
  }

  public int getHopCounter() {
    return hopCounter;
  }

  public boolean isFinalNode() {
    return isFinalNode;
  }

  public boolean isMaster() {
    return isMaster;
  }

  public void setPriority(double priority) {
    this.priority = priority;
  }

  public void setHopCounter(int hopCounter) {
    this.hopCounter = hopCounter;
  }

  public void incrementHopCounter() {
    this.hopCounter++;
  }

  public void setFinalNode() {
    this.isFinalNode = true;
  }

  public int getRequesterNode() {
    return isMaster ? this.getSource() : this.getDestination();
  }

  public int getTargetNode() {
    return isMaster ? this.getDestination() : this.getSource();
  }

  @Override
  public Message clone() {
    return this;
  }

  @Override
  public int compareTo(RequestClusterMessage o) {
    int cmpPriority = Double.compare(this.getPriority(), o.getPriority()); // compare the priorities
    int cmpIDs = Integer.compare(o.getRequesterNode(), this.getRequesterNode()); // in tie, use the id of the requester

    if (cmpPriority == 0) {
      if (cmpIDs == 0) {
        // the requester node has higher priority
        return Boolean.compare(o.isMaster(), this.isMaster());
      } else {
        return cmpIDs;
      }
    } else {
      return cmpPriority;
    }
  }

}