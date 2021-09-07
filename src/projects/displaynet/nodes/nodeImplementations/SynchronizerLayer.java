package projects.displaynet.nodes.nodeImplementations;

import projects.defaultProject.nodes.nodeImplementations.BinarySearchTreeLayer;

/**
 * SynchronizerNode Implements timeslots system to coordenate operations. The node initiate the
 * timeslot in 0
 */
public abstract class SynchronizerLayer extends BinarySearchTreeLayer {

  private int MAX_TIMESLOT = 16;
  public int timeslot;
  private long round;

  public int getCurrentTimeSlot() {
    return this.timeslot;
  }

  public long getCurrentRound() {
    return this.round;
  }

  @Override
  public void init() {
    super.init();
    this.timeslot = 0;
  }

  @Override
  public void postStep() {

    switch (this.timeslot) {

      case 0:

      case 1:

      case 2:

      case 3:

      case 4:
        handShakeStep();
        break;

      case 5:
        controlStep();
        break;

      case 8:
        clusterPhaseOne();
        break;

      case 11:
        clusterPhaseTwo();
        break;

      case 14:
        rotationStep();
        break;

      case 15:
        posRound();

        this.round++;
        break;

      default:
        break;
    }

    this.timeslot = (this.timeslot + 1) % MAX_TIMESLOT;
  }

  public abstract void handShakeStep();

  public abstract void controlStep();

  public abstract void clusterPhaseOne();

  public abstract void clusterPhaseTwo();

  public abstract void rotationStep();

  public abstract void posRound();

}