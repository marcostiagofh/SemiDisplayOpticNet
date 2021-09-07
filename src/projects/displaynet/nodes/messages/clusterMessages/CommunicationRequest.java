package projects.displaynet.nodes.messages.clusterMessages;

public class CommunicationRequest extends RequestClusterMessage {

  public CommunicationRequest(int src, int dst, int hopCounter, double priority, boolean master) {
    super(src, dst, hopCounter, priority, master);
  }

  public CommunicationRequest(RequestClusterMessage msg) {
    super(msg);
  }
}
