package projects.displaynet.nodes.messages.clusterMessages;

import projects.defaultProject.nodes.tableEntry.NodeInfo;

public class AckCommunicationRequest extends AckClusterMessage {

  public AckCommunicationRequest(int src, int dst, double priority, int hopCounter,
      NodeInfo info) {
    super(src, dst, priority, hopCounter, info);
  }
}
