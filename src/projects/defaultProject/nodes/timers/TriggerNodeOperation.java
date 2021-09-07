package projects.defaultProject.nodes.timers;

import projects.defaultProject.nodes.messages.ApplicationMessage;
import projects.defaultProject.nodes.nodeImplementations.ApplicationNode;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

/**
 * TimerExponential
 */
public class TriggerNodeOperation extends Timer {

  public int src;
  public int dst;
  public ApplicationMessage appMsg;

  public TriggerNodeOperation(int src, int dst, ApplicationMessage msg) {
    this.src = src;
    this.dst = dst;
    this.appMsg = msg;
  }

  @Override
  public void fire() {

    ApplicationNode srcnode = (ApplicationNode) Tools.getNodeByID(src);
    srcnode.sendMessage(this.appMsg);
  }

}