package projects.defaultProject.nodes.messages;

import sinalgo.nodes.messages.Message;

public class NetworkMessage extends Message {

  private int src;
  private int dst;
  private double initialTime;
  private double finalTime;

  public NetworkMessage(int src, int dst) {
    this.src = src;
    this.dst = dst;
  }

  @Override
  public Message clone() {
    return this;
  }

  public int getSource() {
    return src;
  }

  public int getDestination() {
    return dst;
  }

  public double getInitialTime() {
    return initialTime;
  }

  public void setInitialTime(double initialTime) {
    this.initialTime = initialTime;
  }

  public double getFinalTime() {
    return finalTime;
  }

  public void setFinalTime(double finalTime) {
    this.finalTime = finalTime;
  }

}
