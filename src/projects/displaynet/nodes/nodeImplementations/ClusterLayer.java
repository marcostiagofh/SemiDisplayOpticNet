package projects.displaynet.nodes.nodeImplementations;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import projects.defaultProject.nodes.tableEntry.NodeInfo;
import projects.displaynet.nodes.messages.clusterMessages.AckClusterMessage;
import projects.displaynet.nodes.messages.clusterMessages.AckCommunicationRequest;
import projects.displaynet.nodes.messages.clusterMessages.CommunicationRequest;
import projects.displaynet.nodes.messages.clusterMessages.RequestClusterMessage;
import sinalgo.nodes.messages.Message;

/**
 * ClusterLayer
 */
public abstract class ClusterLayer extends RPCLayer {

  // workaround to compute number of bypass
  public boolean hasClusterGranted;
	
  // set with current splay request of this node
  private RequestClusterMessage currentClusterRequest;

  // this priority queue store all request cluster message received
  private PriorityQueue<RequestClusterMessage> priorityQueueClusterRequest;

  // this queue keeps all acks received due to a request cluster operation
  private Queue<AckClusterMessage> queueAckCluster;

  @Override
  public void init() {
    super.init();
    
    this.hasClusterGranted = false;
    this.currentClusterRequest = null;
    this.priorityQueueClusterRequest = new PriorityQueue<>();
    this.queueAckCluster = new LinkedList<>();
  }

  public void setClusterRequest(int src, int dst, double priority, boolean master) {
    // position is set to 0, first node in the sequence
    this.currentClusterRequest = new RequestClusterMessage(src, dst, 0, priority, master);
  }

  public void clearClusterRequest() {
    this.currentClusterRequest = null;
  }

  public void clearClusterRequestQueue() {
    this.priorityQueueClusterRequest.clear();
  }

  public void clearAckClusterQueue() {
    this.queueAckCluster.clear();
  }

  public void sendCommunicationRequestCluster() {
    // add communication cluster request to buffer
    this.priorityQueueClusterRequest.add(new CommunicationRequest(this.currentClusterRequest));

    // shift the position one hop
    CommunicationRequest msg = new CommunicationRequest(this.currentClusterRequest);
    msg.incrementHopCounter();
    msg.setFinalNode();

    this.sendForwardMessage(msg.getDestination(), msg);
  }

  private void forwardRequestCluster(RequestClusterMessage msg) {
    this.sendToParent(msg);
  }

  /**
   * This method send a cluster request and put the request into the buffer of current node. The
   * hopCounter means how many times the message should be routed.
   */
  public void sendRequestCluster() {
    // add cluster request to buffer
    this.priorityQueueClusterRequest.add(this.currentClusterRequest);

    // shift the position one hop
    RequestClusterMessage msg = new RequestClusterMessage(this.currentClusterRequest);
    msg.incrementHopCounter();

    this.forwardRequestCluster(msg);
  }

  @Override
  public void receiveMessage(Message msg) {
    super.receiveMessage(msg);

    if (msg instanceof CommunicationRequest) {

      CommunicationRequest comRqMessage = (CommunicationRequest) msg;
      this.priorityQueueClusterRequest.add(comRqMessage);

    } else if (msg instanceof RequestClusterMessage) {

      RequestClusterMessage requestMessage = (RequestClusterMessage) msg;
      int hopCounter = requestMessage.getHopCounter();

      /**
       * It is not possible to parent to be the dst because completion is check first
       * and the grandparent being LCA has effect on the type of rotation
       */
      if (hopCounter == 3 || (hopCounter == 2 && ID == requestMessage.getDestination())) {

        requestMessage.setFinalNode();

      } else if (!requestMessage.isFinalNode()) {

        RequestClusterMessage newRequestMessage = new RequestClusterMessage(requestMessage);
        newRequestMessage.incrementHopCounter();

        if (hopCounter == 1 && this.isAncestorOf(requestMessage.getDestination())) {
          newRequestMessage.setFinalNode();
        }

        this.forwardRequestCluster(newRequestMessage);

      }

      // add current request to queue
      this.priorityQueueClusterRequest.add(requestMessage);

    } else if (msg instanceof AckClusterMessage) {

      AckClusterMessage ackMessage = (AckClusterMessage) msg;
      this.queueAckCluster.add(ackMessage);

    }
  }

  /**
   * In this time slot all request message will have arrived
   */
  @Override
  public void clusterPhaseOne() {

    if (!this.priorityQueueClusterRequest.isEmpty()) {
      RequestClusterMessage rq = this.priorityQueueClusterRequest.poll();

      if (rq instanceof CommunicationRequest) {

        NodeInfo info = this.getNodeInfo();
        AckCommunicationRequest ack = new AckCommunicationRequest(rq.getSource(),
            rq.getDestination(),
            rq.getPriority(),
            rq.getHopCounter(), info);

        ack.setFinalNode();

        this.sendForwardMessage(rq.getSource(), ack);

      } else if (this.currentClusterRequest == null || this.currentClusterRequest.compareTo(rq) >= 0
          || (this.isAncestorOf(rq.getSource())
          && this.currentClusterRequest.getDestination() == rq.getSource())) {

        // check if my current request is greater than request received
        // if my current msg is greater than the request received send ack message.
        // The node can send message to itself

        NodeInfo info = this.getNodeInfo();
        AckClusterMessage ack = new AckClusterMessage(rq.getSource(), rq.getDestination(),
            rq.getPriority(),
            rq.getHopCounter(), info);

        if (rq.isFinalNode()) {
          ack.setFinalNode();
        }

        this.sendForwardMessage(rq.getSource(), ack);
      }
    }
  }

  private boolean isCommunicationClusterGranted() {
    return this.queueAckCluster.peek() instanceof AckCommunicationRequest;
  }

  private AckClusterMessage findAckMessageInBufferByPosition(int pos) {
    for (AckClusterMessage m : this.queueAckCluster) {
      if (m.getHopCounter() == pos) {
        return m;
      }
    }

    return null;
  }

  /**
   * This function checks ack buffer to see if ack message form a linear sequence to final node. In
   * case missing one ack message the function return false.
   *
   * @return
   */
  private boolean isClusterGranted() {
    AckClusterMessage m;

    for (int pos = 0; pos <= 3; pos++) {
      m = findAckMessageInBufferByPosition(pos);

      if (m == null) {

        return false;

      } else if (m.isFinalNode()) {

        return true;

      }
    }

    return false;
  }

  private HashMap<String, NodeInfo> getClusterSequenceFromAckBuffer() {
    HashMap<String, NodeInfo> table = new HashMap<>();
    NodeInfo info;

    info = findAckMessageInBufferByPosition(0).getInfo();
    table.put("x", info);

    info = findAckMessageInBufferByPosition(1).getInfo();
    table.put("y", info);

    info = findAckMessageInBufferByPosition(2).getInfo();
    table.put("z", info);

    if (this.queueAckCluster.size() == 4) {
      info = findAckMessageInBufferByPosition(3).getInfo();
      table.put("w", info);
    }

    return table;
  }

  /**
   * In this time slot all ack message will have arrived If the node has sent message requesting
   * cluster formation verify if the permission was granted.
   */
  @Override
  public void clusterPhaseTwo() {
	
	this.hasClusterGranted = false;
	
    // This node has sent request cluster message
    if (!this.queueAckCluster.isEmpty()) {
      if (this.isCommunicationClusterGranted()) {

    	this.hasClusterGranted = true;
        this.communicationClusterCompleted();

      } else if (this.isClusterGranted()) {
    	  
    	this.hasClusterGranted = true;
        this.clusterCompleted(this.getClusterSequenceFromAckBuffer());

      }
    }

    // reset queue
    this.clearClusterRequestQueue();
    this.clearAckClusterQueue();
  }

  public abstract void clusterCompleted(HashMap<String, NodeInfo> cluster);

  public abstract void communicationClusterCompleted();

}