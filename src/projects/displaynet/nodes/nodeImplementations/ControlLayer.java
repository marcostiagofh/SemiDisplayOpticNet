package projects.displaynet.nodes.nodeImplementations;

import projects.displaynet.nodes.tableEntry.Request;
import sinalgo.tools.Tools;

/**
 * SplayNetNode
 */
public abstract class ControlLayer extends RotationLayer {

  private enum States {
    PASSIVE, ACTIVE
  }

  // keep current state of the node
  private States state;

  // keeps track of every splay a node participate as source or destination. once
  // a splay is completed, it is assigned null value
  private boolean newSplay;
  private Request activeSplayRequest;

  @Override
  public void init() {
    super.init();

    this.state = States.PASSIVE;
    this.newSplay = false;
    this.activeSplayRequest = null;
  }

  protected Request getActiveSplayRequest() {
    return activeSplayRequest;
  }

  public void startNewSplay(Request rq) {
    this.newSplay = true;
    this.activeSplayRequest = rq;
    this.activeSplayRequest.initialTime = this.getCurrentRound();
  }

  public void resetSplay() {
    this.newSplay = false;
    this.activeSplayRequest = null;
    this.clearClusterRequest();
    this.state = States.PASSIVE;
  }

  @Override
  public void controlStep() {

    /*
     * Update current state in time slot 0
     */
    switch (this.state) {

      case PASSIVE:
        if (this.newSplay) {

          // event
          if (this.activeSplayRequest.isMaster()) {
            this.newSplayStarted(this.activeSplayRequest);
          }

          this.state = States.ACTIVE;
          this.newSplay = false;
          this.setRequest(this.activeSplayRequest.getSrcId(), this.activeSplayRequest.getDstId(),
              this.activeSplayRequest.getPriority(), this.activeSplayRequest.isMaster());
        } else {
          break;
        }

      case ACTIVE:

        if (this.isNeighbor(this.activeSplayRequest.getDstId())) {

          // request communication cluster from cluster layer
          if (this.activeSplayRequest.isMaster()) {
            this.sendCommunicationRequestCluster();
          }

        } else if (!this.isAncestorOf(this.activeSplayRequest.getDstId())) {
          this.tryRotation();
        }

        break;

      default:
        Tools.fatalError("Non-existing splay state");
        break;
    }
  }

  @Override
  public void zigCompleted() { // single rotation
    this.activeSplayRequest.numOfRotations++;
  }

  @Override
  public void rotationCompleted() { // double rotation
    this.activeSplayRequest.numOfRotations++;
    this.activeSplayRequest.numOfRotations++;
  }

  @Override
  public void communicationClusterCompleted() {
    this.connectionEstablished();
  }
  
  @Override
  public void loggingBypass() {
	  if (this.state == States.ACTIVE) {
		  if (!this.hasClusterGranted) {
			  if (this.hasParentChanged()) {
				  //bypass
				  this.getActiveSplayRequest().numOfBypass++;
			  } else {
				  //pause
				  this.getActiveSplayRequest().numOfPauses++;
			  }
		  }
	  }
  }

  public abstract void newSplayStarted(Request currentRequest);

  public abstract void connectionEstablished();

}
