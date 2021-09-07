package projects.defaultProject.nodes.messages;

import projects.displaynet.nodes.tableEntry.Request;

public class AckApplicationMessage extends NetworkMessage {

  private Request request;

  public AckApplicationMessage(int src, int dst) {
    super(src, dst);
  }

  public AckApplicationMessage(ApplicationMessage appMsg) {
    super(appMsg.getDestination(), appMsg.getSource());
  }

  public void setRequest(Request request) {
    this.request = request;
  }

  public Request getRequest() {
    return request;
  }
}
