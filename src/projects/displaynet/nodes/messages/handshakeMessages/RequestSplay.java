package projects.displaynet.nodes.messages.handshakeMessages;

import projects.defaultProject.nodes.messages.NetworkMessage;
import sinalgo.nodes.messages.Message;

/**
 * RequestSplay
 */
public class RequestSplay extends NetworkMessage {

  private double priority;

  public RequestSplay(int src, int dst, double priority) {
    super(src, dst);
    this.priority = priority;
  }

  public double getPriority() {
    return priority;
  }

  public void setPriority(double priority) {
    this.priority = priority;
  }

}