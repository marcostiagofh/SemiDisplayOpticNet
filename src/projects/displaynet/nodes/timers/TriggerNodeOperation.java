package projects.displaynet.nodes.timers;

import projects.defaultProject.nodes.messages.ApplicationMessage;
import projects.displaynet.CustomGlobal;
import projects.displaynet.nodes.nodeImplementations.DiSplayNetApp;
import sinalgo.nodes.timers.Timer;
import sinalgo.tools.Tools;

/**
 * TimerExponential
 */
public class TriggerNodeOperation extends Timer {

  public ApplicationMessage msg;

  public TriggerNodeOperation(ApplicationMessage msg) {
    this.msg = msg;
  }

  @Override
  public void fire() {

    DiSplayNetApp srcnode = (DiSplayNetApp) Tools.getNodeByID(msg.getSource());
    srcnode.sendMessage(msg);

    CustomGlobal.mustGenerateSplay = true;
  }

}