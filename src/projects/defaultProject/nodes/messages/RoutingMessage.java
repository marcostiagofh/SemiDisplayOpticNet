package projects.defaultProject.nodes.messages;

import sinalgo.nodes.messages.Message;

/**
 * RoutingMessage
 */
public class RoutingMessage extends NetworkMessage {

  private Message payload;

  public RoutingMessage(int source, int destination) {
    super(source, destination);
    this.payload = null;
  }

  public RoutingMessage(int source, int destination, Message payload) {
    super(source, destination);
    this.payload = payload;
  }

  public Message getPayLoad() {
    return this.payload;
  }

  public void setPayLoad(Message payload) {
    this.payload = payload;
  }

  @Override
  public Message clone() {
    return this;
  }

}